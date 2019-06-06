//
// Created by 乐 程 on 22/05/2017.
//

#ifndef KSYSTREAMERANDROIDSDK_AUDIORESAMPLE_H
#define KSYSTREAMERANDROIDSDK_AUDIORESAMPLE_H

#define __STDC_CONSTANT_MACROS
#include "AudioFilterBase.h"
extern "C" {
#include "audio_resample.h"
}

class AudioResample : public AudioFilterBase {
public:
    AudioResample();
    ~AudioResample();

    // config output format, must be set before config
    int setOutputFormat(int sampleFmt, int sampleRate, int channels, bool useDiffMemory);
    // config input format and init resample instance
    int config(int sampleFormat, int sampleRate, int channels);
    // do resample
    int resample(uint8_t** out, uint8_t* in, int in_size);
    // get delay in output samples
    int getDelay();

    int init(int idx, int sampleFmt, int sampleRate, int channels, int bufferSamples);
    int process(int idx, uint8_t* inBuf, int inSize);

private:
    KSYSwr* mSwr;
    pthread_mutex_t mLock;
    int mInSampleFmt;
    int mInSampleRate;
    int mInChannels;
    int mOutSampleFmt;
    int mOutSampleRate;
    int mOutChannels;
    int mOutBufferSamples;

    bool mUseDiffMemory;
    uint8_t *mOutBuf;
    int mOutBufSize;
};


#endif //KSYSTREAMERANDROIDSDK_AUDIORESAMPLE_H
