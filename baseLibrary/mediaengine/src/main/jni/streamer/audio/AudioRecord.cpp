//
// Created by 乐 程 on 16/03/2017.
//

#include <assert.h>
#include <string.h>
#include "AudioRecord.h"
#include "log.h"
#include "thread_util.h"

#undef LOG_TAG
#define LOG_TAG "AudioRecord"

AudioRecord::AudioRecord(int sampleRate, int channels, int bufferSamples):
    AudioBase(),
    mBuffer(NULL),
    mState(STATE_IDLE),
    mVolume(1.0f),
    mLatencyTest(false),
    mStartTime(0) {
    mSampleRate = sampleRate;
    mChannels = channels;
    mBufferSamples = bufferSamples;
    mFrameSize = 2 * channels;
    memset(&mSLRecord, 0, sizeof(mSLRecord));

    //create engineObject and engineEngine
    if(createEngine() != SL_RESULT_SUCCESS) {
        LOGE("[AudioRecord] CreateEngine failed");
        destroyEngine();
        return;
    }

    if(openRecord() != SL_RESULT_SUCCESS) {
        LOGE("[AudioRecord] RecOpen failed");
        closeRecord();
        destroyEngine();
        return;
    }

    mBuffer = (uint8_t*) malloc((size_t) (bufferSamples * mFrameSize));
    assert(mBuffer);
    pthread_mutex_init(&mFilterLock, NULL);

    int threshold = mSampleRate * 200 / 1000;     // 200ms
    mFifoSize = mBufferSamples * 2;
    while (mFifoSize < threshold) {
        mFifoSize += mBufferSamples * 2;
    }
    LOGD("FIFO size: %d count: %d", mFifoSize, mFifoSize / mBufferSamples);
    mFifoBuffer = (uint8_t*) malloc((size_t) (mFifoSize * mFrameSize));
    assert(mFifoBuffer);
    audio_utils_fifo_init(&mFifo, (size_t) mFifoSize, (size_t) mFrameSize, mFifoBuffer);
    mReadCond = createThreadLock();
    waitThreadLock(mReadCond);

    mState = STATE_INITIALIZED;
}

AudioRecord::~AudioRecord() {
    closeRecord();
    destroyEngine();

    // release buffer
    if (mBuffer) {
        free(mBuffer);
        mBuffer = NULL;
    }
    pthread_mutex_destroy(&mFilterLock);

    // destroy fifo
    audio_utils_fifo_deinit(&mFifo);
    if (mFifoBuffer) {
        free(mFifoBuffer);
    }
    destroyThreadLock(mReadCond);
}

int AudioRecord::getState() {
    return mState;
}

int AudioRecord::start() {
    if (mState != STATE_INITIALIZED) {
        LOGE("start called on invalid state %d", mState);
        return -1;
    }

    LOGD("start");
    size_t size = (size_t) (mBufferSamples * mFrameSize);
    memset(mBuffer, 0, size);
    SLresult result = (*mSLRecord.recorderBufferQueue)->Enqueue(mSLRecord.recorderBufferQueue,
                                                                mBuffer, size);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("[start] Enqueue failed:%d", (int) result);
        //return -1;
    }

    result = (*mSLRecord.recorderRecord)->SetRecordState(mSLRecord.recorderRecord,
                                                         SL_RECORDSTATE_RECORDING);

    if (result != SL_RESULT_SUCCESS) {
        LOGE("[start] SetRecordState failed:%d", (int) result);
        return -1;
    }
    mState = STATE_RECORDING;

    pthread_mutex_lock(&mFilterLock);
    if (mFilter && !mFilterInited) {
        mFilter->init(mFilterIdx, SAMPLE_FMT_S16, mSampleRate, mChannels, mBufferSamples);
        mFilterInited = true;
    }
    pthread_mutex_unlock(&mFilterLock);
    return 0;
}

int AudioRecord::stop() {
    if (mState != STATE_RECORDING) {
        LOGE("stop called on invalid state %d", mState);
        return -1;
    }

    LOGD("stop");
    SLresult result = (*mSLRecord.recorderRecord)->SetRecordState(mSLRecord.recorderRecord,
                                                                  SL_RECORDSTATE_STOPPED);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("[stop] SetRecordState failed:%d", (int) result);
        //return -1;
    }

    result = (*mSLRecord.recorderBufferQueue)->Clear(mSLRecord.recorderBufferQueue);
    if (result != SL_RESULT_SUCCESS) {
        LOGE("[stop] Clear buffer queue failed:%d", (int) result);
        return -1;
    }
    mState = STATE_INITIALIZED;
    notifyThreadLock(mReadCond);

    return 0;
}

int AudioRecord::read(uint8_t *buf, int size) {
    int count = size / mFrameSize;
    int read = audio_utils_fifo_read(&mFifo, buf, (size_t) count);
    while (read < count) {
        waitThreadLock(mReadCond);
        if (mState != STATE_RECORDING) {
            LOGD("read aborted!");
            break;
        }
        read += audio_utils_fifo_read(&mFifo, (buf + read * mFrameSize), (size_t) (count - read));
    }
    return read * mFrameSize;
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

static inline int16_t clipInt16(int a) {
    if ((a+0x8000) & ~0xFFFF) return (int16_t) ((a >> 31) ^ 0x7FFF);
    else                      return (int16_t) a;
}

void AudioRecord::bqRecorderCallback(SLAndroidSimpleBufferQueueItf bq, void *context)
{
    AudioRecord* thiz = (AudioRecord*) context;

    //LOGD("bqRecorderCallback called");
    if (thiz->mLatencyTest) {
        int64_t now = getNsTimestamp() / 1000;
        short* buf = (short*) thiz->mBuffer;

        // measure latency
        short threshold = SHRT_MAX / 4;
        for (int i=0; i<thiz->mBufferSamples; i++) {
            if (buf[i] >= threshold) {
                int latency = (int) (now - thiz->mStartTime);
                LOGI("Latency measured : %d ms", latency / 1000);
                break;
            }
        }

        memset(thiz->mBuffer, 0, (size_t) (thiz->mBufferSamples * thiz->mFrameSize));
        if ((now - thiz->mStartTime) >= 5000000) {
            thiz->mStartTime = now;
            buf[0] = SHRT_MAX;
        }
    } else if (thiz->mVolume != 1.0f) {
        int16_t *buf = (short*) thiz->mBuffer;
        int count = thiz->mBufferSamples * thiz->mFrameSize / sizeof(int16_t);
        for (int i = 0; i < count; i++) {
            buf[i] = clipInt16((int) (buf[i] * thiz->mVolume));
        }
    }

    // fill cache
    int written = audio_utils_fifo_write(&thiz->mFifo, thiz->mBuffer,
                                         (size_t) thiz->mBufferSamples);
    if (written < thiz->mBufferSamples) {
        LOGW("audio fifo full, %d samples dropped", thiz->mBufferSamples - written);
    }

    int size = thiz->mBufferSamples * thiz->mFrameSize;
    pthread_mutex_lock(&thiz->mFilterLock);
    if (thiz->mFilter) {
        if (!thiz->mFilterInited) {
            thiz->mFilter->init(thiz->mFilterIdx, SAMPLE_FMT_S16, thiz->mSampleRate,
                                thiz->mChannels, thiz->mBufferSamples);
            thiz->mFilterInited = true;
        }
        thiz->mFilter->process(thiz->mFilterIdx, thiz->mBuffer, size);
    }
    pthread_mutex_unlock(&thiz->mFilterLock);

    // queue buffer
    SLresult result = (*thiz->mSLRecord.recorderBufferQueue)->Enqueue(
            thiz->mSLRecord.recorderBufferQueue,
            thiz->mBuffer, (SLuint32) size);
    if(result != SL_RESULT_SUCCESS) {
        LOGE("[bqRecorderCallback] Enqueue failed:%d", (int) result);
    }

    notifyThreadLock(thiz->mReadCond);
}

SLresult AudioRecord::createEngine() {
    SLresult result;
    // create engine to state SL_OBJECT_STATE_UNREALIZED
    result = slCreateEngine(&(mSLRecord.engineObject), 0, NULL, 0, NULL, NULL);
    if(result != SL_RESULT_SUCCESS) {
        LOGE("[audio_record][CreateEngine] slCreateEngine failed");
        return result;
    }

    // realize the engine to state SL_OBJECT_STATE_REALIZED
    result = (*mSLRecord.engineObject)->Realize(mSLRecord.engineObject, SL_BOOLEAN_FALSE);
    if(result != SL_RESULT_SUCCESS) {
        LOGE("[audio_record][CreateEngine] Realize failed");
        return result;
    }

    // get the engine interface, which is needed in order to create other objects
    result = (*mSLRecord.engineObject)->GetInterface(mSLRecord.engineObject, SL_IID_ENGINE,
                                                     &(mSLRecord.engineEngine));
    if(result != SL_RESULT_SUCCESS) {
        LOGE("[audio_record][CreateEngine] GetInterface engineEngine failed");
        return result;
    }

    return result;
}

void AudioRecord::destroyEngine() {
    // destroy engine object, and invalidate all associated interfaces
    if (mSLRecord.engineObject != NULL) {
        (*mSLRecord.engineObject)->Destroy(mSLRecord.engineObject);
        mSLRecord.engineObject = NULL;
        mSLRecord.engineEngine = NULL;
    }
}

SLresult AudioRecord::openRecord() {
    SLresult result;

    // configure audio source
    // OpenSL ES 1.0.1 Specification 9.9.1
    SLDataLocator_IODevice loc_dev = {SL_DATALOCATOR_IODEVICE, SL_IODEVICE_AUDIOINPUT,
                                      SL_DEFAULTDEVICEID_AUDIOINPUT, NULL};
    // OpenSL ES 1.0.1 Specification 9.1.15
    SLDataSource audioSrc = {&loc_dev, NULL};

    // configure audio sink
    SLuint32 speakers;
    if(mChannels > 1) {
        speakers = SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT;
    } else {
        speakers = SL_SPEAKER_FRONT_CENTER;
    }

    SLDataLocator_AndroidSimpleBufferQueue loc_bq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 1};
    //9.1.7 SLDataFormat_PCM
    SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, (SLuint32) mChannels,
                                   (SLuint32) (mSampleRate * 1000),
                                   SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
                                   speakers, SL_BYTEORDER_LITTLEENDIAN};
    SLDataSink audioSnk = {&loc_bq, &format_pcm};

    // create audio recorder
    // (requires the RECORD_AUDIO permission)
    const SLInterfaceID id[1] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
    const SLboolean req[1] = {SL_BOOLEAN_TRUE};
    result = (*mSLRecord.engineEngine)->CreateAudioRecorder(mSLRecord.engineEngine,
                                                            &(mSLRecord.recorderObject),
                                                            &audioSrc, &audioSnk,
                                                            1, id, req);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("[RecOpen] CreateAudioRecorder failed");
        return result;
    }

    // realize the audio recorder
    result = (*mSLRecord.recorderObject)->Realize(mSLRecord.recorderObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("[RecOpen] Realize failed");
        return result;
    }

    // get the record interface
    result = (*mSLRecord.recorderObject)->GetInterface(mSLRecord.recorderObject, SL_IID_RECORD,
                                                       &(mSLRecord.recorderRecord));
    if (SL_RESULT_SUCCESS != result) {
        LOGE("[RecOpen] GetInterface recorderRecord failed");
        return result;
    }

    // get the buffer queue interface
    result = (*mSLRecord.recorderObject)->GetInterface(mSLRecord.recorderObject,
                                                       SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                                       &(mSLRecord.recorderBufferQueue));
    if (SL_RESULT_SUCCESS != result) {
        LOGE("[audio_record][RecOpen] GetInterface recorderBufferQueue failed");
        return result;
    }

    // register callback on the buffer queue
    result = (*mSLRecord.recorderBufferQueue)->RegisterCallback(mSLRecord.recorderBufferQueue,
                                                                bqRecorderCallback, this);
    if (SL_RESULT_SUCCESS != result) {
        LOGE("[audio_record][RecOpen] RegisterCallback failed");
        return result;
    }

    return result;
}

void AudioRecord::closeRecord() {
    // destroy audio recorder object, and invalidate all associated interfaces
    if (mSLRecord.recorderObject != NULL) {
        (*mSLRecord.recorderObject)->Destroy(mSLRecord.recorderObject);
        mSLRecord.recorderObject = NULL;
        mSLRecord.recorderRecord = NULL;
        mSLRecord.recorderBufferQueue = NULL;
    }
}
