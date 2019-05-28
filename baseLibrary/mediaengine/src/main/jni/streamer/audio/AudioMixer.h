//
// Created by 乐 程 on 23/03/2017.
//

#ifndef KSYSTREAMERANDROIDSDK_AUDIOMIXER_H
#define KSYSTREAMERANDROIDSDK_AUDIOMIXER_H

#define __STDC_CONSTANT_MACROS
#include "AudioFilterBase.h"
extern "C" {
#include "audio_utils_fifo.h"
#include "audio_resample.h"
};

class AudioMixer : public AudioFilterBase {
public:
    AudioMixer();
    ~AudioMixer();

    void setMainIdx(int idx) {mMainIdx = idx;}
    void setInputVolume(int idx, float vol) {
        mInputVolume[idx][0] = vol;
        mInputVolume[idx][1] = vol;
    }
    void setInputVolume(int idx, float leftVol, float rightVol) {
        mInputVolume[idx][0] = leftVol;
        mInputVolume[idx][1] = rightVol;
    }
    void setOutputVolume(float leftVol, float rightVol) {
        mLeftOutputVolume = leftVol;
        mRightOutputVolume = rightVol;
    }
    void setMute(bool mute) {mMute = mute;}
    void setBlockingMode(bool blockingMode) {mBlockingMode = blockingMode;}
    int config(int idx, int sampleFmt, int sampleRate, int channels, int bufferSamples,
               int fifoSizeInMs, bool nativeMode);
    void destroy(int idx);
    int init(int idx, int sampleFmt, int sampleRate, int channels, int bufferSamples);
    int process(int idx, uint8_t* inBuf, int inSize);
    int process(int idx, uint8_t* inBuf, int inSize, bool nativeMode);

private:
    typedef struct ChannelParam {
        int sampleFmt;
        int sampleRate;
        int channels;
        int bufferSamples;
        int fifoSizeInMs;
        int frameSize;
    } ChannelParam;

    typedef struct ChannelFifo {
        audio_utils_fifo fifo;
        uint8_t* pBuffer;
        int fifoSamples;
        int frameSize;
        // for blocking mode
        pthread_cond_t readCond;
        pthread_cond_t writeCond;
    } ChannelFifo;

    static const int CHN_NUM = 8;
    pthread_mutex_t mLock;
    int mMainIdx;
    bool mMainFrameReady;
    bool mMute;
    float mLeftOutputVolume;
    float mRightOutputVolume;
    float mInputVolume[CHN_NUM][2];
    ChannelParam* mChannelParams[CHN_NUM];
    ChannelFifo* mChannelFifos[CHN_NUM];
    KSYSwr* mChannelSwrs[CHN_NUM];
    uint8_t* mBuffer;
    int mBufSize;

    // for blocking mode
    bool mBlockingMode;

    void fifoSwrInit(int idx);
    void fifoSwrRelease(int idx);
    ChannelFifo* fifoInit(int sampleFmt, int sampleRate, int channels, int bufferSamples, int fifoSizeInMs);
    void fifoRelease(ChannelFifo* channelFifo);
    int mixAll(uint8_t* inBuf, int inSize);
    void mix(short* src1, int size1, float leftVol1, float rightVol1, short* src2, int size2,
             float* vol2, int channels);
};


#endif //KSYSTREAMERANDROIDSDK_AUDIOMIXER_H
