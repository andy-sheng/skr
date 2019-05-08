#ifndef __AVCONST_H__
#define __AVCONST_H__

#define FRAME_FLAG_KEY      0x01
#define FRAME_FLAG_CONFIG   0x02
#define FRAME_FLAG_EOS      0x04

#define FRAME_TYPE_AUDIO    0x1
#define FRAME_TYPE_VIDEO    0x2

enum {
    PIX_FMT_OPAQUE,
    PIX_FMT_NV21,
    PIX_FMT_YV12,
    PIX_FMT_I420,
    PIX_FMT_ARGB,
    PIX_FMT_RGBA,
};

enum {
    CODEC_ID_NONE,
    // video codec
    CODEC_ID_AVC,
    CODEC_ID_HEVC,
    CODEC_ID_GIF,
    // audio codec
    CODEC_ID_AAC = 0x100,
};

#endif  // __AVCONST_H__
