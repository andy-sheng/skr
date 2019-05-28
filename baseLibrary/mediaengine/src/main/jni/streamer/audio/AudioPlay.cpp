//
// Created by 乐 程 on 17/03/2017.
//

#include <assert.h>
#include <string.h>
#include <include/log.h>
#include "AudioPlay.h"
#include "thread_util.h"

#undef LOG_TAG
#define LOG_TAG "AudioPlay"

AudioPlay::AudioPlay():
    mMute(false),
    mNonBlock(false),
    mTuneLatency(false),
    mFirstFrame(true),
    mBuffer(NULL),
    mFifoBuffer(NULL),
    mWriteCond(NULL),
    mState(STATE_IDLE),
    mStart(false){
    memset(&mSLPlayer, 0, sizeof(mSLPlayer));
}

AudioPlay::~AudioPlay() {
    release();
}

int AudioPlay::config(int sampleFmt, int sampleRate, int channels, int bufferSamples, int fifoSizeInMs) {
    // destroy previous instance
    release();

    if (sampleFmt != SAMPLE_FMT_S16) {
        LOGE("AudioPlay only support SAMPLE_FMT_S16!");
        return -1;
    }

    mSampleFmt = sampleFmt;
    mSampleRate = sampleRate;
    mChannels = channels;
    mBufferSamples = bufferSamples;
    mFifoSizeInMs = fifoSizeInMs;
    mFrameSize = 2 * channels;

    mLastWriteTime = 0;
    mWriteInterval = mBufferSamples * 1000000 / mSampleRate;
    mLastLogTime = 0;

    //create engineObject and engineEngine
    if(createEngine() != SL_RESULT_SUCCESS) {
        LOGE("[AudioPlay] CreateEngine failed");
        destroyEngine();
        return -1;
    }

    if(openPlayer() != SL_RESULT_SUCCESS) {
        LOGE("[AudioPlay] openPlayer failed");
        closePlayer();
        destroyEngine();
        return -1;
    }

    mBuffer = (uint8_t*) malloc((size_t) (bufferSamples * mFrameSize));
    assert(mBuffer);

    int threshold = mSampleRate * mFifoSizeInMs / 1000;
    mFifoSamples = mBufferSamples * 3;
    while (mFifoSamples < threshold) {
        mFifoSamples += mBufferSamples;
    }
    LOGD("FIFO size: %d count: %d", mFifoSamples, mFifoSamples / mBufferSamples);
    mFifoBuffer = (uint8_t*) malloc((size_t) (mFifoSamples * mFrameSize));
    assert(mFifoBuffer);
    audio_utils_fifo_init(&mFifo, (size_t) mFifoSamples, (size_t) mFrameSize, mFifoBuffer);
    mWriteCond = createThreadLock();
    waitThreadLock(mWriteCond);

    mState = STATE_INITIALIZED;

    if (mStart) {
        if (startPlayer() != SL_RESULT_SUCCESS) {
            LOGE("Auto start player failed!");
            return -1;
        }
        if (mMute) {
            mutePlayer(mMute);
        }
    }
    return 0;
}

void AudioPlay::setMute(bool mute) {
    mMute = mute;
    if (mState == STATE_PLAYING || mState == STATE_PAUSE) {
        mutePlayer(mute);
    }
}

int AudioPlay::start() {
    LOGD("start in state: %d", mState);
    if (mState == STATE_INITIALIZED) {
        if (startPlayer() != SL_RESULT_SUCCESS) {
            return -1;
        }
        if (mMute) {
            mutePlayer(mMute);
        }
    }
    mStart = true;
    return 0;
}

int AudioPlay::pause() {
    LOGD("pause in state: %d", mState);
    if (mState == STATE_PLAYING) {
        if (pausePlayer() != SL_RESULT_SUCCESS) {
            return -1;
        }
    }
    return 0;
}

int AudioPlay::resume() {
    LOGD("resume in state: %d", mState);
    if (mState == STATE_PAUSE) {
        if (resumePlayer() != SL_RESULT_SUCCESS) {
            return -1;
        }
    }
    return 0;
}

int AudioPlay::stop() {
    LOGD("stop");
    mStart = false;
    if ((mState != STATE_PLAYING) && (mState != STATE_PAUSE)) {
        return 0;
    }

    // set the player's state to stopped
    SLresult result = (*mSLPlayer.playerPlay)->SetPlayState(mSLPlayer.playerPlay,
                                                            SL_PLAYSTATE_STOPPED);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("[stop] SetPlayState failed:%d", (int) result);
        return -1;
    }

    mState = STATE_INITIALIZED;
    notifyThreadLock(mWriteCond);
    return 0;
}

int AudioPlay::write(uint8_t *inBuf, int inSize, bool nonBlock) {
    if (mState != STATE_PLAYING) {
        return filterProcess(mSampleFmt, mSampleRate, mChannels, mBufferSamples, inBuf, inSize);
    }

    mNonBlock = nonBlock;
    int inSamples = inSize / mFrameSize;
    int len = audio_utils_fifo_write(&mFifo, inBuf, (size_t) inSamples);
    if (nonBlock) {
        if (len < inSamples) {
            LOGD("fifo full, only write %d samples", len);
        }
        return filterProcess(mSampleFmt, mSampleRate, mChannels, mBufferSamples, inBuf, inSize);
    }
    while (len < inSamples) {
        waitThreadLock(mWriteCond);
        if (mState != STATE_PLAYING) {
            LOGD("write aborted!");
            break;
        }
        len += audio_utils_fifo_write(&mFifo, inBuf + len * mFrameSize,
                                      (size_t) (inSamples - len));
    }
    return len * mFrameSize;
}

int AudioPlay::init(int idx, int sampleFmt, int sampleRate, int channels, int bufferSamples) {
    filterInit(sampleFmt, sampleRate, channels, bufferSamples);
    return config(sampleFmt, sampleRate, channels, bufferSamples);
}

int AudioPlay::process(int idx, uint8_t *inBuf, int inSize) {
    return write(inBuf, inSize, true);
}

/*
 * Aquire current timestamp in milliseconds
 */
static inline int64_t getNsTimestamp() {
    struct timespec stamp;
    clock_gettime(CLOCK_MONOTONIC, &stamp);
    int64_t nsec = (int64_t) stamp.tv_sec*1000000000LL + stamp.tv_nsec;
    return nsec;
}

void AudioPlay::bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
    AudioPlay *thiz = (AudioPlay *) context;
    size_t size = (size_t) (thiz->mBufferSamples * thiz->mFrameSize);
    int64_t now = getNsTimestamp() / 1000;

    // Audio play jitter detect
    int readCount = 1;
    if (thiz->mLastWriteTime != 0 && thiz->mTuneLatency) {
        int64_t diff = now - thiz->mLastWriteTime;
        if (diff >= thiz->mWriteInterval * 7 / 4) {
            readCount = (int) ((diff + thiz->mWriteInterval / 4) / thiz->mWriteInterval);
            LOGW("write jitter: %d, dequeue count: %d", (int) diff, readCount);
        }
    }
    thiz->mLastWriteTime = now;

    // send data to next filter in blocking mode here
    if (!thiz->mFirstFrame && !thiz->mNonBlock) {
        thiz->filterProcess(thiz->mSampleFmt, thiz->mSampleRate, thiz->mChannels,
                            thiz->mBufferSamples, thiz->mBuffer, size);
    }

    int len;
    memset(thiz->mBuffer, 0, size);
    do {
        len = audio_utils_fifo_read(&thiz->mFifo, thiz->mBuffer, (size_t) thiz->mBufferSamples);
        if (len < thiz->mBufferSamples) {
            LOGD("[AudioPlay][Play] fifo empty, enqueue %d samples", len);
        }
    } while((thiz->mFirstFrame || --readCount) && len == thiz->mBufferSamples);
    thiz->mFirstFrame = false;

    SLresult result = (*thiz->mSLPlayer.playerBufferQueue)->Enqueue(
            thiz->mSLPlayer.playerBufferQueue, thiz->mBuffer, size);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("[AudioPlay][Play] Enqueue failed:%d",(int)result);
    }

    // measure fifo size
    if (thiz->mNonBlock && now - thiz->mLastLogTime >= 5000000) {
        LOGD("fifo remain: %d", (int) audio_utils_fifo_get_remain(&thiz->mFifo));
        thiz->mLastLogTime = now;
    }

    notifyThreadLock(thiz->mWriteCond);
}

SLresult AudioPlay::createEngine() {
    SLresult result;
    // create engine to state SL_OBJECT_STATE_UNREALIZED
    result = slCreateEngine(&(mSLPlayer.engineObject), 0, NULL, 0, NULL, NULL);
    if(result != SL_RESULT_SUCCESS) {
        LOGE("[audio_record][CreateEngine] slCreateEngine failed");
        return result;
    }

    // realize the engine to state SL_OBJECT_STATE_REALIZED
    result = (*mSLPlayer.engineObject)->Realize(mSLPlayer.engineObject, SL_BOOLEAN_FALSE);
    if(result != SL_RESULT_SUCCESS) {
        LOGE("[AudioPlay][CreateEngine] Realize failed");
        return result;
    }

    // get the engine interface, which is needed in order to create other objects
    result = (*mSLPlayer.engineObject)->GetInterface(mSLPlayer.engineObject, SL_IID_ENGINE,
                                                   &(mSLPlayer.engineEngine));
    if(result != SL_RESULT_SUCCESS) {
        LOGE("[AudioPlay][CreateEngine] GetInterface engineEngine failed");
        return result;
    }

    const SLInterfaceID ids[] = {};
    const SLboolean req[] = {};
    result = (*mSLPlayer.engineEngine)->CreateOutputMix(mSLPlayer.engineEngine,
                                                        &mSLPlayer.outputMixObject,
                                                        0, ids, req);
    if(result != SL_RESULT_SUCCESS) {
        LOGE("[AudioPlay][CreateEngine] CreateOutputMix failed");
        return result;
    }

    // realize the output mix
    result = (*mSLPlayer.outputMixObject)->Realize(mSLPlayer.outputMixObject, SL_BOOLEAN_FALSE);
    if(result != SL_RESULT_SUCCESS) {
        LOGE("[AudioPlay][CreateEngine] Realize outputMixObject failed");
        return result;
    }

    return result;
}

void AudioPlay::destroyEngine() {
    // destroy output mix object, and invalidate all associated interfaces
    if (mSLPlayer.outputMixObject != NULL) {
        (*mSLPlayer.outputMixObject)->Destroy(mSLPlayer.outputMixObject);
        mSLPlayer.outputMixObject = NULL;
    }

    // destroy engine object, and invalidate all associated interfaces
    if (mSLPlayer.engineObject != NULL) {
        (*mSLPlayer.engineObject)->Destroy(mSLPlayer.engineObject);
        mSLPlayer.engineObject = NULL;
        mSLPlayer.engineEngine = NULL;
    }
}

SLresult AudioPlay::openPlayer() {
    SLresult result;

    // configure audio source
    SLuint32 speakers;
    if(mChannels > 1) {
        speakers = SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT;
    } else {
        speakers = SL_SPEAKER_FRONT_CENTER;
    }
    SLDataLocator_AndroidSimpleBufferQueue loc_bq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 1};
    SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, (SLuint32) mChannels,
                                   (SLuint32) (mSampleRate * 1000),
                                   SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
                                   speakers, SL_BYTEORDER_LITTLEENDIAN};
    SLDataSource audioSrc = {&loc_bq, &format_pcm};

    // configure audio sink
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, mSLPlayer.outputMixObject};
    SLDataSink audioSnk = {&loc_outmix, NULL};

    // create audio player
    const SLInterfaceID ids[2] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE, SL_IID_VOLUME};
    const SLboolean req[2] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    result = (*mSLPlayer.engineEngine)->CreateAudioPlayer(mSLPlayer.engineEngine,
                                                          &(mSLPlayer.playerObject),
                                                          &audioSrc, &audioSnk,
                                                          2, ids, req);
    if(result != SL_RESULT_SUCCESS) {
        LOGE("[AudioPlay][PlayOpen] CreateAudioPlayer failed");
        return result;
    }

    // realize the player
    result = (*mSLPlayer.playerObject)->Realize(mSLPlayer.playerObject, SL_BOOLEAN_FALSE);
    if(result != SL_RESULT_SUCCESS) {
        LOGE("[AudioPlay][PlayOpen] Realize bqPlayerObject failed");
        return result;
    }

    // get the play interface
    result = (*mSLPlayer.playerObject)->GetInterface(mSLPlayer.playerObject, SL_IID_PLAY,
                                                     &(mSLPlayer.playerPlay));
    if(result != SL_RESULT_SUCCESS) {
        LOGE("[AudioPlay][PlayOpen] GetInterface playerPlay failed");
        return result;
    }

    // get the buffer queue interface
    result = (*mSLPlayer.playerObject)->GetInterface(mSLPlayer.playerObject,
                                                     SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                                     &(mSLPlayer.playerBufferQueue));
    if(result != SL_RESULT_SUCCESS) {
        LOGE("[AudioPlay][PlayOpen] GetInterface bqPlayerBufferQueue failed");
        return result;
    }

    // register callback on the buffer queue
    result = (*mSLPlayer.playerBufferQueue)->RegisterCallback(mSLPlayer.playerBufferQueue,
                                                              bqPlayerCallback, this);
    if(result != SL_RESULT_SUCCESS) {
        LOGE("[AudioPlay][PlayOpen] RegisterCallback failed");
        return result;
    }

    result = (*mSLPlayer.playerObject)->GetInterface(mSLPlayer.playerObject, SL_IID_VOLUME,
                                                     &mSLPlayer.volumeItf);
    if(result != SL_RESULT_SUCCESS) {
        LOGE("[AudioPlay][PlayOpen] GetInterface muteSoloItf failed");
        return result;
    }

    return 0;
}

void AudioPlay::closePlayer() {
    // destroy buffer queue audio player object, and invalidate all associated interfaces
    if (mSLPlayer.playerObject != NULL) {
        (*mSLPlayer.playerObject)->Destroy(mSLPlayer.playerObject);
        mSLPlayer.playerObject = NULL;
        mSLPlayer.playerPlay = NULL;
        mSLPlayer.playerBufferQueue = NULL;
    }
}

SLresult AudioPlay::startPlayer() {
    // set player to playing state
    size_t size = (size_t) (mBufferSamples * mFrameSize);
    memset(mBuffer, 0, size);
    SLresult result = (*mSLPlayer.playerBufferQueue)->Enqueue(mSLPlayer.playerBufferQueue,
                                                              mBuffer, size);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("[start] Enqueue failed:%d", (int) result);
        //return result;
    }

    result = (*mSLPlayer.playerPlay)->SetPlayState(mSLPlayer.playerPlay,
                                                   SL_RECORDSTATE_RECORDING);

    if (result != SL_RESULT_SUCCESS) {
        LOGE("[start] SetRecordState failed:%d", (int) result);
    } else {
        mState = STATE_PLAYING;
    }
    mFirstFrame = true;

    return result;
}

SLresult AudioPlay::pausePlayer() {
    SLresult result = (*mSLPlayer.playerPlay)->SetPlayState(mSLPlayer.playerPlay,
                                                            SL_RECORDSTATE_PAUSED);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("[pause] SetRecordState failed:%d", (int) result);
    } else {
        mState = STATE_PAUSE;
    }

    return result;
}

SLresult AudioPlay::resumePlayer() {
	SLresult result = (*mSLPlayer.playerPlay)->SetPlayState(mSLPlayer.playerPlay,
	                                               SL_RECORDSTATE_RECORDING);

    if (result != SL_RESULT_SUCCESS) {
		LOGE("[start] SetRecordState failed:%d", (int) result);
    } else {
		mState = STATE_PLAYING;
    }

    return result;
}

SLresult AudioPlay::mutePlayer(bool mute) {
    if ((mState != STATE_PLAYING) && (mState != STATE_PAUSE)) {
        return SL_RESULT_SUCCESS;
    }

    SLresult result = (*mSLPlayer.volumeItf)->SetMute(mSLPlayer.volumeItf,
                                             mute ? SL_BOOLEAN_TRUE : SL_BOOLEAN_FALSE);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("SetMute %d failed:%d", (int) mute, (int) result);
    }
    return result;
}

void AudioPlay::release() {
    // release audio engine
    closePlayer();
    destroyEngine();

    // release buffer
    if (mBuffer) {
        free(mBuffer);
        mBuffer = NULL;
    }

    // destroy fifo
    audio_utils_fifo_deinit(&mFifo);
    if (mFifoBuffer) {
        free(mFifoBuffer);
        mFifoBuffer = NULL;
    }
    if (mWriteCond) {
        destroyThreadLock(mWriteCond);
        mWriteCond = NULL;
    }
    mState = STATE_IDLE;
}
