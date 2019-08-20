//
// Created by 乐 程 on 23/03/2017.
//

#include <assert.h>
#include <include/log.h>
#include "AudioMixer.h"

#undef LOG_TAG
#define LOG_TAG "AudioMixer"

AudioMixer::AudioMixer() {
    mMainIdx = 0;
    mMainFrameReady = false;
    mMute = false;
    mLeftOutputVolume = 1.0f;
    mRightOutputVolume = 1.0f;
    mBuffer = NULL;
    mBufSize = 0;
    pthread_mutex_init(&mLock, NULL);
    for (int i=0; i<CHN_NUM; i++) {
        mChannelParams[i] = NULL;
        mChannelFifos[i] = NULL;
        mChannelSwrs[i] = NULL;
        mInputVolume[i][0] = 1.0f;
        mInputVolume[i][1] = 1.0f;
        mDelay[i] = 0;
        mDelaySamples[i] = 0;
        mDelayedSamples[i] = 0;
    }

    // for blocking mode
    mBlockingMode = false;
}

AudioMixer::~AudioMixer() {
    pthread_mutex_lock(&mLock);
    for (int i=0; i<CHN_NUM; i++) {
        if (mChannelParams[i]) {
            free(mChannelParams[i]);
            mChannelParams[i] = NULL;
        }
        fifoSwrRelease(i);
    }
    if (mBuffer) {
        free(mBuffer);
        mBuffer = NULL;
    }
    pthread_mutex_unlock(&mLock);
    pthread_mutex_destroy(&mLock);
}

void AudioMixer::fifoSwrInit(int idx) {
    if (mChannelParams[idx] == NULL || idx == mMainIdx) {
        return;
    }
    ChannelParam* scp = mChannelParams[idx];
    ChannelParam* dcp = mChannelParams[mMainIdx];
    int fifoSizeInMs = scp->fifoSizeInMs;
    if (dcp->fifoSizeInMs > scp->fifoSizeInMs) {
        fifoSizeInMs = dcp->fifoSizeInMs;
    }
    mChannelFifos[idx] = fifoInit(dcp->sampleFmt, dcp->sampleRate, dcp->channels,
                                  dcp->bufferSamples, fifoSizeInMs);
    if (scp->sampleRate != dcp->sampleRate || scp->channels != dcp->channels) {
        mChannelSwrs[idx] = ksy_swr_init(scp->sampleRate, scp->channels, AV_SAMPLE_FMT_S16,
                                         dcp->sampleRate, dcp->channels, AV_SAMPLE_FMT_S16);
    }
}

void AudioMixer::fifoSwrRelease(int idx) {
    ChannelFifo* cf = mChannelFifos[idx];
    mChannelFifos[idx] = NULL;
    fifoRelease(cf);
    if (mChannelSwrs[idx]) {
        ksy_swr_release(mChannelSwrs[idx]);
        mChannelSwrs[idx] = NULL;
    }
}

AudioMixer::ChannelFifo* AudioMixer::fifoInit(int sampleFmt, int sampleRate, int channels,
                                              int bufferSamples, int fifoSizeInMs) {
    ChannelFifo* cf = (ChannelFifo*) calloc(1, sizeof(ChannelFifo));
    cf->frameSize = channels * 2;
    cf->fifoSamples = bufferSamples * 4;
    int threshold = sampleRate * fifoSizeInMs / 1000;
    while (cf->fifoSamples < threshold) {
        cf->fifoSamples += bufferSamples;
    }
    LOGD("FIFO size: %d count: %d", cf->fifoSamples, cf->fifoSamples / bufferSamples);
    cf->pBuffer = (uint8_t*) malloc((size_t) cf->fifoSamples * cf->frameSize);
    assert(cf->pBuffer);
    audio_utils_fifo_init(&cf->fifo, (size_t) cf->fifoSamples, (size_t) cf->frameSize, cf->pBuffer);
    pthread_cond_init(&cf->readCond, NULL);
    pthread_cond_init(&cf->writeCond, NULL);
    return cf;
}

void AudioMixer::fifoRelease(AudioMixer::ChannelFifo *cf) {
    if (cf) {
        // signal blocking read/write
        pthread_cond_signal(&cf->readCond);
        pthread_cond_signal(&cf->writeCond);
        // unlock and destroy condition
        pthread_mutex_unlock(&mLock);
        pthread_cond_destroy(&cf->readCond);
        pthread_cond_destroy(&cf->writeCond);

        // lock again and continue
        pthread_mutex_lock(&mLock);
        audio_utils_fifo_deinit(&cf->fifo);
        if (cf->pBuffer) {
            free(cf->pBuffer);
            cf->pBuffer = NULL;
        }
        free(cf);
    }
}

int AudioMixer::config(int idx, int sampleFmt, int sampleRate, int channels, int bufferSamples,
                       int fifoSizeInMs, bool nativeMode) {
    if (idx < 0 || idx >= CHN_NUM) {
        return -1;
    }
    if (sampleFmt != SAMPLE_FMT_S16) {
        LOGE("AudioMixer only support SAMPLE_FMT_S16!");
        return -1;
    }

    pthread_mutex_lock(&mLock);
    ChannelParam* cp = mChannelParams[idx];
    if (cp == NULL) {
        cp = (ChannelParam *) calloc(1, sizeof(ChannelParam));
    } else if (sampleFmt == cp->sampleFmt &&
                sampleRate == cp->sampleRate &&
                channels == cp->channels) {
        pthread_mutex_unlock(&mLock);
        return 0;
    }
    cp->sampleFmt = sampleFmt;
    cp->sampleRate = sampleRate;
    cp->channels = channels;
    cp->bufferSamples = bufferSamples;
    cp->fifoSizeInMs = fifoSizeInMs;
    cp->frameSize = channels * 2;
    mChannelParams[idx] = cp;
    LOGD("config: idx=%d sampleFmt=%d sampleRate=%d channels=%d bufferSamples=%d fifoSizeInMs=%d",
         idx, sampleFmt, sampleRate, channels, bufferSamples, fifoSizeInMs);

    if (idx == mMainIdx) {
        mMainFrameReady = false;
        for (int i=0; i<CHN_NUM; i++) {
            fifoSwrRelease(i);
            fifoSwrInit(i);
            mDelayedSamples[i] = 0;
            mDelaySamples[i] = mDelay[i] * cp->sampleRate / 1000;
        }

        // init attached filter if needed
        if (nativeMode) {
            filterInit(cp->sampleFmt, cp->sampleRate, cp->channels, cp->bufferSamples);
        }
    } else if (mChannelParams[mMainIdx]) {
        fifoSwrRelease(idx);
        fifoSwrInit(idx);
        mDelayedSamples[idx] = 0;
    }
    pthread_mutex_unlock(&mLock);
    return 0;
}

void AudioMixer::setDelay(int idx, int64_t delay) {
    mDelay[idx] = delay;

    pthread_mutex_lock(&mLock);
    ChannelParam* cp = mChannelParams[0];
    if (cp) {
        mDelaySamples[idx] = delay * cp->sampleRate / 1000;
    }
    pthread_mutex_unlock(&mLock);
}

void AudioMixer::destroy(int idx) {
    if (idx < 0 || idx >= CHN_NUM) {
        return;
    }

    LOGD("destroy %d", idx);
    pthread_mutex_lock(&mLock);
    if (mChannelParams[idx]) {
        free(mChannelParams[idx]);
        mChannelParams[idx] = NULL;
    }
    if (idx == mMainIdx) {
        for (int i=0; i<CHN_NUM; i++) {
            fifoSwrRelease(i);
        }
    } else {
        fifoSwrRelease(idx);
    }
    pthread_mutex_unlock(&mLock);
}

int AudioMixer::init(int idx, int sampleFmt, int sampleRate, int channels, int bufferSamples) {
    return config(idx, sampleFmt, sampleRate, channels, bufferSamples, 160, true);
}

int AudioMixer::process(int idx, uint8_t *inBuf, int inSize) {
    return process(idx, inBuf, inSize, true);
}

int AudioMixer::process(int idx, uint8_t *inBuf, int inSize, bool nativeMode) {
    // LOGD("process idx=%d inBuf=%p inSize=%d nativeMode=%d", idx, inBuf, inSize, nativeMode);
    int result = inSize;
    pthread_mutex_lock(&mLock);
    // destroyed
    if (mChannelParams[idx] == NULL) {
        LOGD("mixer %d params destroyed, break process", idx);
        goto Quit;
    }
    if (idx == mMainIdx) {
        mMainFrameReady = true;
        if (mixAll(inBuf, inSize) < 0) {
            goto Quit;
        }
        if (mMute) {
            memset(inBuf, 0, (size_t) inSize);
        }
        // do filter if needed
        if (nativeMode) {
            ChannelParam *cp = mChannelParams[mMainIdx];
            result = filterProcess(cp->sampleFmt, cp->sampleRate, cp->channels, cp->bufferSamples,
                                   inBuf, inSize);
        }
    } else {
        ChannelFifo* cf = mChannelFifos[idx];
        KSYSwr* swr = mChannelSwrs[idx];
        if (mMainFrameReady && cf) {
            int size = 0;
            uint8_t* buf = NULL;
            if (swr) {
                uint8_t **ppBuf = NULL;
                size = ksy_swr_convert(swr, &ppBuf, &inBuf, inSize);
                buf = ppBuf[0];
                if (size <= 0) {
                    LOGE("mixer %d resample failed, err=%d", idx, size);
                }
            }
            if (buf == NULL) {
                buf = inBuf;
                size = inSize;
            }

            // handle negative delay
            int64_t delayOffSamples = mDelaySamples[idx] - mDelayedSamples[idx];
            if (mBlockingMode && delayOffSamples < 0) {
                ChannelParam* cp = mChannelParams[mMainIdx];
                int64_t delayOffSize = -delayOffSamples * cp->frameSize;
                LOGD("idx: %d delayOffSamples: %" PRId64 " delayOffSize: %" PRId64 " size: %d", idx, delayOffSamples, delayOffSize, size);
                if (delayOffSize < size) {
                    buf += delayOffSize;
                    size -= (int) delayOffSize;
                    mDelayedSamples[idx] += delayOffSamples;
                    LOGD("idx: %d delay caught up, remain size: %d", idx, size);
                } else {
                    mDelayedSamples[idx] -= size / cp->frameSize;
                    LOGD("idx: %d drop data to catch up delay", idx);
                    goto Quit;
                }
            }

            int frameSize = cf->frameSize;
            int samples = size / frameSize;
            do {
                int ret = audio_utils_fifo_write(&cf->fifo, buf, (size_t) samples);
                if (mBlockingMode && ret > 0) {
                    pthread_cond_signal(&cf->readCond);
                }
                buf += ret * frameSize;
                samples -= ret;
                if (mBlockingMode && samples > 0) {
                    pthread_cond_wait(&cf->writeCond, &mLock);
                    // fifo destroyed
                    if (mChannelFifos[idx] == NULL) {
                        LOGD("mixer %d fifo destroyed, break write", idx);
                        goto Quit;
                    }
                } else {
                    break;
                }
            } while (true);
            if (samples > 0) {
                LOGD("mixer %d fifo full, try to write %d, remain %d",
                     idx, size, samples * frameSize);
            }
        }
    }
    Quit:
    pthread_mutex_unlock(&mLock);
    return result;
}

int AudioMixer::mixAll(uint8_t *inBuf, int inSize) {
    float leftMainVol = mInputVolume[mMainIdx][0];
    float rightMainVol = mInputVolume[mMainIdx][1];
    int frameSize = mChannelParams[mMainIdx]->frameSize;
    int sampleRate = mChannelParams[mMainIdx]->sampleRate;
    // set volume to main source input
    if(leftMainVol != 1.0f || rightMainVol != 1.0f) {
        short* data = (short*) inBuf;
        int size = inSize / sizeof(short);
        int step = 1;
        if(mChannelParams[mMainIdx]->channels == 2)  {
            step = 2;
        }
        for (int i = 0; i < size; i = i + step) {
            data[i] = av_clip_int16((int) (data[i] * leftMainVol));
        }
        if(mChannelParams[mMainIdx]->channels == 2) {
            for (int i = 1; i < size; i = i + step) {
                data[i] = av_clip_int16((int) (data[i] * rightMainVol));
            }
        }
    }
    leftMainVol = 1.0f;
    rightMainVol = 1.0f;
    for (int i = 0; i < CHN_NUM; i++) {
        ChannelFifo *cf = mChannelFifos[i];
        if (cf) {
            if (mBuffer && mBufSize < inSize) {
                LOGD("realloc buffer from %d to %d", mBufSize, inSize);
                mBuffer = (uint8_t *) realloc(mBuffer, (size_t) inSize);
                mBufSize = inSize;
            } else if (mBuffer == NULL) {
                mBuffer = (uint8_t *) malloc((size_t) inSize);
                assert(mBuffer);
                mBufSize = inSize;
            }
            memset(mBuffer, 0, (size_t) mBufSize);
            int64_t samples = inSize / frameSize;
            uint8_t* buf = mBuffer;
            // handle positive delay
            int64_t delayOffSamples = mDelaySamples[i] - mDelayedSamples[i];
            if (mBlockingMode && i != mMainIdx && delayOffSamples > 0) {
                int64_t zeroSamples = (delayOffSamples < samples) ? delayOffSamples : samples;
                LOGD("idx: %d delayOffSamples: %" PRId64 " zeroSamples: %" PRId64, i, delayOffSamples, zeroSamples);
                mDelayedSamples[i] += zeroSamples;
                samples -= zeroSamples;
                buf += zeroSamples * frameSize;
            }
            do {
                int ret = audio_utils_fifo_read(&cf->fifo, buf, (size_t) samples);
                if (mBlockingMode && ret > 0) {
                    pthread_cond_signal(&cf->writeCond);
                }
                buf += ret * frameSize;
                samples -= ret;
                if (mBlockingMode && samples > 0) {
                    pthread_cond_wait(&cf->readCond, &mLock);
                    // fifo destroyed
                    if (mChannelFifos[i] == NULL) {
                        LOGD("mixer %d fifo destroyed, break read", i);
                        return -1;
                    }
                } else {
                    break;
                }
            } while (true);
            if (samples > 0) {
                LOGD("mixer %d fifo empty, try to read %d, remain %d",
                     i, inSize, (int)samples * frameSize);
            }
            mix((short *) inBuf, inSize / sizeof(short), leftMainVol, rightMainVol, (short *) mBuffer,
                (inSize - (int)samples * frameSize) / sizeof(short), mInputVolume[i],
                mChannelParams[i]->channels);
        }
    }

    short *data = (short *) inBuf;
    int size = inSize / sizeof(short);
    int step = 1;
    if(mChannelParams[mMainIdx]->channels == 2) {
        step = 2;
    }
    //when mono, this is actually not left channel volume
    if (mLeftOutputVolume != 1.0f) {
        for (int i = 0; i < size; i = i + step) {
            data[i] = av_clip_int16((int) (data[i] * mLeftOutputVolume));
        }
    }

    if(mChannelParams[mMainIdx]->channels > 1) {
        if (mRightOutputVolume != 1.0f) {
            for (int i = 1; i < size; i = i + step) {
                data[i] = av_clip_int16((int) (data[i] * mRightOutputVolume));
            }
        }
    }

    return 0;
}

void AudioMixer::mix(short *src1, int size1, float leftVol1, float rightVol1,
                     short *src2, int size2, float* vol2, int channels) {
    int size = (size1 < size2) ? size1 : size2;
    int step = 1;
    if (channels == 2) {
        step = 2;
    }
    for (int i = 0; i < size; i = i + step) {
        src1[i] = av_clip_int16((int) (src1[i] * leftVol1 + src2[i] * vol2[0]));
    }
    if (channels == 2) {
        for (int i = 1; i < size; i = i + step) {
            src1[i] = av_clip_int16((int) (src1[i] * rightVol1 + src2[i] * vol2[1]));
        }
    }
}
