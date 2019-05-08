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
        mInSampleRate(0),
        mInChannels(0),
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

int AudioResample::setOutputFormat(int sampleRate, int channels) {
    pthread_mutex_lock(&mLock);
    mOutSampleRate = sampleRate;
    mOutChannels = channels;
    pthread_mutex_unlock(&mLock);
    return 0;
}

int AudioResample::config(int sampleFormat, int sampleRate, int channels) {
    int ret = 0;
    pthread_mutex_lock(&mLock);
    mInSampleRate = sampleRate;
    mInChannels = channels;
    if (mSwr) {
        ksy_swr_release(mSwr);
        mSwr = NULL;
    }
    if (sampleFormat != AV_SAMPLE_FMT_S16 || mInSampleRate != mOutSampleRate || mInChannels !=
                                                                                 mOutChannels) {
        mSwr = ksy_swr_init(mInSampleRate, mInChannels, sampleFormat,
                            mOutSampleRate, mOutChannels, AV_SAMPLE_FMT_S16);
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
        ret = ksy_swr_convert(mSwr, out, in, in_size);
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

int AudioResample::init(int idx, int sampleRate, int channels, int bufferSamples) {
    int ret = config(AV_SAMPLE_FMT_S16, sampleRate, channels);
    if (!ret) {
        ret = filterInit(mOutSampleRate, mOutChannels, mOutBufferSamples);
    }
    return ret;
}

int AudioResample::process(int idx, uint8_t *inBuf, int inSize) {
    int ret = 0;
    uint8_t *outBuf = NULL;
    int outSize = resample(&outBuf, inBuf, inSize);
    if (outBuf && outSize > 0) {
        ret = filterProcess(mOutSampleRate, mOutChannels, mOutBufferSamples, outBuf, outSize);
    } else {
        LOGE("resample %d data failed!", inSize);
        ret = -1;
    }
    return ret;
}
