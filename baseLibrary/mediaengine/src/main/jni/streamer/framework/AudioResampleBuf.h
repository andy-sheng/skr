//
// Created by 昝晓飞 on 16/8/3.
//

#ifndef KSYSTREAMERANDROID_AUDIORESAMPLEBUF_H
#define KSYSTREAMERANDROID_AUDIORESAMPLEBUF_H
#include <stdint.h>
extern "C" {
#define __STDC_LIMIT_MACROS

#define __STDC_FORMAT_MACROS

#define __STDC_CONSTANT_MACROS
#ifdef _STDINT_H
#undef _STDINT_H
#endif
#ifndef   UINT64_C
#define   UINT64_C(value)__CONCAT(value,ULL)
#endif
}

class AudioResampleBuf {
public:
    struct SwrContext* swrCtx;
    uint8_t* inBuf;
    uint8_t** outBuf;
    int inBufSize;
    int outBufSize;
    int aoutMaxSamples;
    int inSampleBytes;

    //be set from java
    int inSampleRate;
    int inChnNum;
    int inSampleFmt;

    int outChnNum;
    int outSampleRate;
    int outSampleFmt;
    AudioResampleBuf():
        swrCtx(NULL),
        inBuf(NULL),
        outBuf(NULL),
        inBufSize(0),
        outBufSize(0),
        aoutMaxSamples(0),
        inSampleBytes(0),
        inSampleRate(0),
        inChnNum(0),
        outSampleRate(0),
        outChnNum(0)
    {
    }
    AudioResampleBuf(int inSampleRate, int inChnNum, int inSampleFmt,
                     int outSampleRate, int outChnNum, int outSampleFmt):
        swrCtx(NULL),
        inBuf(NULL),
        outBuf(NULL),
        inBufSize(0),
        outBufSize(0),
        aoutMaxSamples(0),
        inSampleBytes(0)
    {
        this->inSampleRate = inSampleRate;
        this->inChnNum = inChnNum;
        this->inSampleFmt = inSampleFmt;

        this->outSampleRate = outSampleRate;
        this->outChnNum = outChnNum;
        this->outSampleFmt = outSampleFmt;
    }
};
#endif //KSYSTREAMERANDROID_AUDIORESAMPLEBUF_H
