//
// Created by 乐 程 on 22/05/2017.
//

#include "AudioResample.h"
#include <libavutil/samplefmt.h>
#include <include/log.h>

#undef LOG_TAG
#define LOG_TAG "AudioResample"

AudioResample::AudioResample():
        mSwr(NULL),
        mInSampleFmt(0),
        mInSampleRate(0),
        mInChannels(0),
        mOutSampleFmt(0),
        mOutSampleRate(0),
        mOutChannels(0),
        mOutBufferSamples(1024) {
    pthread_mutex_init(&mLock, NULL);
}

AudioResample::~AudioResample() {
    pthread_mutex_lock(&mLock);
    if (mSwr) {
        ksy_swr_release(mSwr);
        mSwr = NULL;
    }
    pthread_mutex_unlock(&mLock);
    pthread_mutex_destroy(&mLock);
}

int AudioResample::setOutputFormat(int sampleFmt, int sampleRate, int channels) {
    pthread_mutex_lock(&mLock);
    mOutSampleFmt = sampleFmt;
    mOutSampleRate = sampleRate;
    mOutChannels = channels;
    pthread_mutex_unlock(&mLock);
    return 0;
}

int AudioResample::config(int sampleFormat, int sampleRate, int channels) {
    int ret = 0;
    pthread_mutex_lock(&mLock);
    mInSampleFmt = sampleFormat;
    mInSampleRate = sampleRate;
    mInChannels = channels;
    if (mSwr) {
        ksy_swr_release(mSwr);
        mSwr = NULL;
    }
    if (mInSampleFmt != mOutSampleFmt || mInSampleRate != mOutSampleRate ||
        mInChannels != mOutChannels) {
        mSwr = ksy_swr_init(mInSampleRate, mInChannels, mInSampleFmt,
                            mOutSampleRate, mOutChannels, mOutSampleFmt);
        if (!mSwr) {
            LOGE("create audio resample failed!");
            ret = -1;
        }
    }
    pthread_mutex_unlock(&mLock);
    return ret;
}

int AudioResample::resample(uint8_t **out, uint8_t *in, int in_size) {
    int ret = 0;
    pthread_mutex_lock(&mLock);
    if (mSwr) {
        uint8_t **ppOut = NULL;
        ret = ksy_swr_convert(mSwr, &out, &in, in_size);
        if (ppOut) {
            *out = ppOut[0];
        }
    } else {
        *out = in;
        ret = in_size;
    }
    pthread_mutex_unlock(&mLock);
    return ret;
}

int AudioResample::getDelay() {
    int delay = 0;
    pthread_mutex_lock(&mLock);
    if (mSwr) {
        delay = ksy_swr_get_delay(mSwr);
    }
    pthread_mutex_unlock(&mLock);
    return delay;
}

int AudioResample::init(int idx, int sampleFmt, int sampleRate, int channels, int bufferSamples) {
    int ret = config(sampleFmt, sampleRate, channels);
    if (!ret) {
        ret = filterInit(mOutSampleFmt, mOutSampleRate, mOutChannels, mOutBufferSamples);
    }
    return ret;
}

int AudioResample::process(int idx, uint8_t *inBuf, int inSize) {
    int ret = 0;
    uint8_t *outBuf = NULL;
    int outSize = resample(&outBuf, inBuf, inSize);
    if (outBuf && outSize > 0) {
        ret = filterProcess(mOutSampleFmt, mOutSampleRate, mOutChannels, mOutBufferSamples,
                            outBuf, outSize);
    } else {
        LOGE("resample %d data failed!", inSize);
        ret = -1;
    }
    return ret;
}
