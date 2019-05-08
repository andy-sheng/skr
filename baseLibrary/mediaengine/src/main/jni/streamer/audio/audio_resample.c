#include "audio_resample.h"
#include <libavutil/mem.h>
#include <libavutil/channel_layout.h>
#include <libswresample/swresample.h>
#include <assert.h>
#include <log.h>
#include <libavutil/opt.h>

struct KSYSwr {
    SwrContext* swrCtx;
    
    uint8_t** aoutData;
    int aoutMaxSamples;
    int aoutDataSize;

    int inSampleRate;
    int inChnNum;
    int inSampleBytes;
    
    int outChnNum;
    int outSampleRate;
    int outSampleFmt;
};

static uint64_t chnNumToLayout(int chnNum)
{
    if (chnNum == 1)
        return AV_CH_LAYOUT_MONO;
    else
        return AV_CH_LAYOUT_STEREO;
}

KSYSwr* ksy_swr_init(int in_samplerate, int in_chnnum, int in_samplefmt,
                     int out_samplerate, int out_chnnum, int out_samplefmt)
{
    KSYSwr* ctx = calloc(1, sizeof(KSYSwr));
    if (ctx == NULL) {
        return NULL;
    }
    
    SwrContext* swrCtx = swr_alloc();
    if (swrCtx == NULL) {
        free(ctx);
        return NULL;
    }

    uint64_t inChLayout = chnNumToLayout(in_chnnum);
    uint64_t outChLayout = chnNumToLayout(out_chnnum);
    
    /* set options */
    av_opt_set_int(swrCtx, "in_channel_layout",    inChLayout,     0);
    av_opt_set_int(swrCtx, "in_sample_rate",       in_samplerate,  0);
    av_opt_set_sample_fmt(swrCtx, "in_sample_fmt", in_samplefmt,    0);

    
    av_opt_set_int(swrCtx, "out_channel_layout",    outChLayout,   0);
    av_opt_set_int(swrCtx, "out_sample_rate",       out_samplerate,0);
    av_opt_set_sample_fmt(swrCtx, "out_sample_fmt", out_samplefmt,  0);

    int ret = swr_init(swrCtx);
    if (ret < 0) {
        LOGE("init resample context failed");
        free(ctx);
        swr_free(&swrCtx);
        return NULL;
    }

    int dstSamples;
    ctx->aoutMaxSamples = dstSamples = (int) av_rescale_rnd(2048, out_samplerate,
                                                            in_samplerate, AV_ROUND_UP);
    ret = av_samples_alloc_array_and_samples(&ctx->aoutData, &ctx->aoutDataSize, out_chnnum,
                                             dstSamples, out_samplefmt, 0);
    if (ret < 0) {
        LOGE("av_samples_alloc_array_and_samples Failed");
        free(ctx);
        swr_free(&swrCtx);
        return NULL;
    }

    ctx->swrCtx = swrCtx;
    ctx->inSampleRate = in_samplerate;
    ctx->inChnNum = in_chnnum;
    ctx->inSampleBytes = av_get_bytes_per_sample(in_samplefmt);
    ctx->outSampleRate = out_samplerate;
    ctx->outChnNum = out_chnnum;
    ctx->outSampleFmt = out_samplefmt;

    return ctx;
}

int ksy_swr_convert(KSYSwr* s, uint8_t** out, uint8_t* in, int in_size)
{
    if (s == NULL)
        return 0;
    int ret = 0;
    int inSamples = in_size / s->inChnNum / s->inSampleBytes;
    int dstSamples = (int) av_rescale_rnd(swr_get_delay(s->swrCtx, s->inSampleRate) + inSamples,
                                          s->outSampleRate, s->inSampleRate, AV_ROUND_UP);
    if (dstSamples > s->aoutMaxSamples) {
        LOGD("realloc aout buffer size");
        av_freep(&s->aoutData[0]);
        ret = av_samples_alloc(s->aoutData, &s->aoutDataSize, s->outChnNum,
                               dstSamples, s->outSampleFmt, 1);
        if (ret < 0) {
            LOGE("realloc aout buffer size failed");
            s->aoutMaxSamples = 0;
            *out = NULL;
            return 0;
        }
        s->aoutMaxSamples = dstSamples;
    }

    ret = swr_convert(s->swrCtx, s->aoutData, dstSamples, (const uint8_t **)&in, inSamples);
    if (ret < 0) {
        LOGE("Error while resampling audio");
        *out = NULL;
        return 0;
    }

    int size = av_samples_get_buffer_size(&s->aoutDataSize, s->outChnNum, ret, s->outSampleFmt, 1);
    *out = s->aoutData[0];
    return size;
}

int ksy_swr_get_delay(KSYSwr* s) {
    if (s == NULL) {
        return 0;
    }
    return (int) swr_get_delay(s->swrCtx, s->outSampleRate);
}

void ksy_swr_release(KSYSwr* s)
{
    if (s == NULL)
        return;
    
    if (s->swrCtx) {
        if (s->aoutData)
            av_freep(&s->aoutData[0]);
        av_freep(&s->aoutData);
        swr_free(&s->swrCtx);
    }
    free(s);
}

