//
// Created by 乐 程 on 16/03/2017.
//

#ifndef KSYSTREAMERANDROIDSDK_AUDIORECORD_H
#define KSYSTREAMERANDROIDSDK_AUDIORECORD_H

#include <stdint.h>
#include <SLES/OpenSLES_Android.h>
#include <pthread.h>
#include "AudioFilterBase.h"
#include "audio_utils_fifo.h"

class AudioRecord : public AudioBase {
public:
    AudioRecord(int sampleRate, int channels, int bufferSamples);
    ~AudioRecord();

    int start();
    int stop();

    void setVolume(float volume) {mVolume = volume;}

    int read(uint8_t* buf, int size);
    int getState();

    void setEnableLatencyTest(bool enable) {mLatencyTest = enable;}

    static const int STATE_IDLE = 0;
    static const int STATE_INITIALIZED = 1;
    static const int STATE_RECORDING = 2;

private:
    typedef struct SLRecord {
        // engine interfaces
        SLObjectItf engineObject;
        SLEngineItf engineEngine;

        // recorder interfaces
        SLObjectItf recorderObject;
        SLRecordItf recorderRecord;
        SLAndroidSimpleBufferQueueItf recorderBufferQueue;
    } SLRecord;

    // this callback handler is called every time a buffer finishes recording
    static void bqRecorderCallback(SLAndroidSimpleBufferQueueItf bq, void *context);

    SLresult createEngine();
    void destroyEngine();
    SLresult openRecord();
    void closeRecord();

    // record params
    int mSampleRate;
    int mChannels;
    int mBufferSamples;
    int mFrameSize;
    uint8_t* mBuffer;

    int mState;
    SLRecord mSLRecord;
    float mVolume;

    audio_utils_fifo mFifo;
    uint8_t* mFifoBuffer;
    // sample count
    int mFifoSize;
    void* mReadCond;

    // latency measure
    bool mLatencyTest;
    int64_t mStartTime;
};

#endif //KSYSTREAMERANDROIDSDK_AUDIORECORD_H
