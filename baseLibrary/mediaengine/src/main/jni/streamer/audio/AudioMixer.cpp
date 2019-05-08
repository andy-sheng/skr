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
    mOutputVolume = 1.0f;
    mBuffer = NULL;
    mBufSize = 0;
    pthread_mutex_init(&mLock, NULL);
    for (int i=0; i<CHN_NUM; i++) {
        mChannelParams[i] = NULL;
        mChannelFifos[i] = NULL;
        mChannelSwrs[i] = NULL;
        mInputVolume[i] = 1.0f;
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
    mChannelFifos[idx] = fifoInit(dcp->sampleRate, dcp->channels, dcp->bufferSamples, fifoSizeInMs);
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

AudioMixer::ChannelFifo* AudioMixer::fifoInit(int sampleRate, int channels, int bufferSamples,
                                              int fifoSizeInMs) {
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

int AudioMixer::config(int idx, int sampleRate, int channels, int bufferSamples,
                       int fifoSizeInMs, bool nativeMode) {
    if (idx < 0 || idx >= CHN_NUM) {
        return -1;
    }

    pthread_mutex_lock(&mLock);
    ChannelParam* cp = mChannelParams[idx];
    if (cp == NULL) {
        cp = (ChannelParam *) calloc(1, sizeof(ChannelParam));
    }
    cp->sampleRate = sampleRate;
    cp->channels = channels;
    cp->bufferSamples = bufferSamples;
    cp->fifoSizeInMs = fifoSizeInMs;
    cp->frameSize = channels * 2;
    mChannelParams[idx] = cp;
    LOGD("config: idx=%d sampleRate=%d channels=%d bufferSamples=%d fifoSizeInMs=%d",
         idx, sampleRate, channels, bufferSamples, fifoSizeInMs);

    if (idx == mMainIdx) {
        mMainFrameReady = false;
        for (int i=0; i<CHN_NUM; i++) {
            fifoSwrRelease(i);
            fifoSwrInit(i);
        }

        // init attached filter if needed
        if (nativeMode) {
            filterInit(cp->sampleRate, cp->channels, cp->bufferSamples);
        }
    } else if (mChannelParams[mMainIdx]) {
        fifoSwrRelease(idx);
        fifoSwrInit(idx);
    }
    pthread_mutex_unlock(&mLock);
    return 0;
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

int AudioMixer::init(int idx, int sampleRate, int channels, int bufferSamples) {
    return config(idx, sampleRate, channels, bufferSamples, 160, true);
}

int AudioMixer::process(int idx, uint8_t *inBuf, int inSize) {
    process(idx, inBuf, inSize, true);
}

int AudioMixer::process(int idx, uint8_t *inBuf, int inSize, bool nativeMode) {
    int result = inSize;
    pthread_mutex_lock(&mLock);
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
            result = filterProcess(cp->sampleRate, cp->channels, cp->bufferSamples, inBuf, inSize);
        }
    } else {
        ChannelFifo* cf = mChannelFifos[idx];
        KSYSwr* swr = mChannelSwrs[idx];
        if (mMainFrameReady && cf) {
            int size = 0;
            uint8_t* buf = NULL;
            if (swr) {
                size = ksy_swr_convert(swr, &buf, inBuf, inSize);
                if (size <= 0) {
                    LOGE("mixer %d resample failed, err=%d", idx, size);
                }
            }
            if (buf == NULL) {
                buf = inBuf;
                size = inSize;
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
    float mainVol = mInputVolume[mMainIdx];
    int frameSize = mChannelParams[mMainIdx]->frameSize;
    // set volume to main source input
    if(mainVol != 1.0f) {
        short* data = (short*) inBuf;
        int size = inSize / sizeof(short);
        for (int i = 0; i < size; i++) {
            data[i] = av_clip_int16((int) (data[i] * mainVol));
        }
    }
    mainVol = 1.0f;
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
            int samples = inSize / frameSize;
            uint8_t* buf = mBuffer;
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
                     i, inSize, samples * frameSize);
            }
            mix((short *) inBuf, inSize / sizeof(short), mainVol, (short *) mBuffer,
                (inSize - samples * frameSize) / sizeof(short), mInputVolume[i]);
        }
    }
    if (mOutputVolume != 1.0f) {
        short* data = (short*) inBuf;
        int size = inSize / sizeof(short);
        for (int i = 0; i < size; i++) {
            data[i] = av_clip_int16((int) (data[i] * mOutputVolume));
        }
    }
    return 0;
}

void AudioMixer::mix(short *src1, int size1, float vol1, short *src2, int size2, float vol2) {
    int size = (size1 < size2) ? size1 : size2;
    for (int i=0; i<size; i++) {
        src1[i] = av_clip_int16((int) (src1[i] * vol1 + src2[i] * vol2));
    }
}
