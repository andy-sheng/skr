//
// Created by 乐 程 on 15/03/2017.
//

#ifndef KSYSTREAMERANDROIDSDK_AUDIOFILTERBASE_H
#define KSYSTREAMERANDROIDSDK_AUDIOFILTERBASE_H

#include <stdint.h>
#include <pthread.h>
#include "audio_utils_fifo.h"
#include "avconst.h"

class AudioFilterBase;

class AudioBase {
public:
    AudioBase();
    virtual ~AudioBase();

    virtual AudioFilterBase* setFilter(int idx, AudioFilterBase *filter);
    virtual AudioFilterBase* getFilter();

protected:
    int mFilterIdx;
    bool mFilterInited;
    AudioFilterBase* mFilter;
    pthread_mutex_t mFilterLock;
};

class AudioFilterBase : public AudioBase {
private:
    bool mStopped;
    int mFrameSize;
    audio_utils_fifo mFifo;
    uint8_t* mFifoBuffer;
    int mFifoSize;
    void* mReadCond;

    void initFifo(int sampleFmt, int sampleRate, int channels);
    void destroyFifo();

public:
    AudioFilterBase();
    virtual ~AudioFilterBase();

    /**
     * Attach this filter to dst module
     *
     * @param dst dst audio module to attach to
     */
    virtual void attachTo(int idx, AudioBase* dst, bool detach);

    /**
     * Init this audio filter in this index.
     *
     * @param idx           index to init
     * @param sampleRate    sample rate in Hz
     * @param channels      channels of 1 or 2
     * @param bufferSamples sample number of every process
     */
    virtual int init(int idx, int sampleFmt, int sampleRate, int channels, int bufferSamples) = 0;

    /**
     * Process audio data in this index.
     *
     * @param idx
     * @param inBuf
     * @param inSize
     * @return 0 for success, negtive value for error
     */
    virtual int process(int idx, uint8_t* inBuf, int inSize) = 0;

    virtual int read(uint8_t* buf, int size);

    virtual int filterInit(int sampleFmt, int sampleRate, int channels, int bufferSamples);

    virtual int filterProcess(int sampleFmt, int sampleRate, int channels,
                              int bufferSamples, uint8_t* inBuf, int inSize);
};

#endif //KSYSTREAMERANDROIDSDK_AUDIOFILTERBASE_H
