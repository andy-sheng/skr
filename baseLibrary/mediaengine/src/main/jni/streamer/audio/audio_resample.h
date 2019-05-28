#ifndef __AUDIO_RESAMPLE_H__
#define __AUDIO_RESAMPLE_H__

#include <stdint.h>
#include <libavutil/samplefmt.h>
#ifndef __cplusplus
#include <stdbool.h>
#else
extern "C" {
#endif

typedef struct KSYSwr KSYSwr;

KSYSwr *ksy_swr_init(int in_samplerate, int in_chnnum, int in_samplefmt,
                     int out_samplerate, int out_chnnum, int out_samplefmt);

int ksy_swr_convert(KSYSwr *s, uint8_t ***out, uint8_t **in, int in_size);

int ksy_swr_get_delay(KSYSwr *s);

void ksy_swr_release(KSYSwr *s);

#ifdef __cplusplus
}
#endif

#endif

