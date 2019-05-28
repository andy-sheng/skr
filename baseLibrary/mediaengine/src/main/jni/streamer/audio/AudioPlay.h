//
// Created by 乐 程 on 17/03/2017.
//

#ifndef KSYSTREAMERANDROIDSDK_AUDIOPLAY_H
#define KSYSTREAMERANDROIDSDK_AUDIOPLAY_H

#include <SLES/OpenSLES_Android.h>
#include "AudioFilterBase.h"
#include "audio_utils_fifo.h"

class AudioPlay : public AudioFilterBase {
public:
    AudioPlay();
    ~AudioPlay();

    void setMute(bool mute);
    void setTuneLatency(bool tuneLatency) {mTuneLatency = tuneLatency;}
    int config(int sampleFmt, int sampleRate, int channels, int bufferSamples, int fifoSizeInMs = 40);
    int start();
    int stop();
    int pause();
    int resume();
    int write(uint8_t* inBuf, int inSize, bool nonBlock = false);
    void release();

    int init(int idx, int sampleFmt, int sampleRate, int channels, int bufferSamples);
    int process(int idx, uint8_t* inBuf, int inSize);

    static const int STATE_IDLE = 0;
    static const int STATE_INITIALIZED = 1;
    static const int STATE_PLAYING = 2;
    static const int STATE_PAUSE = 3;

private:
    typedef struct SLPlay {
        // engine interfaces
        SLObjectItf engineObject;
        SLEngineItf engineEngine;

        // output mix interface
        SLObjectItf outputMixObject;
        // volume interface
        SLVolumeItf volumeItf;
        // buffer queue player interfaces
        SLObjectItf playerObject;
        SLPlayItf playerPlay;
        SLAndroidSimpleBufferQueueItf playerBufferQueue;
    } SLPlayer;

    static void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context);

    SLresult createEngine();
    void destroyEngine();
    SLresult openPlayer();
    void closePlayer();
    SLresult startPlayer();
    SLresult pausePlayer();
    SLresult resumePlayer();
    SLresult mutePlayer(bool mute);

    int mSampleFmt;
    int mSampleRate;
    int mChannels;
    int mBufferSamples;
    int mFrameSize;
    uint8_t* mBuffer;

    bool mMute;
    int mState;
    bool mStart;
    bool mFirstFrame;
    SLPlayer mSLPlayer;

    bool mNonBlock;
    int mFifoSizeInMs;
    audio_utils_fifo mFifo;
    uint8_t* mFifoBuffer;
    int mFifoSamples;
    void* mWriteCond;

    bool mTuneLatency;
    int64_t mLastWriteTime;
    int64_t mWriteInterval;
    int64_t mLastLogTime;
};

#endif //KSYSTREAMERANDROIDSDK_AUDIOPLAY_H
