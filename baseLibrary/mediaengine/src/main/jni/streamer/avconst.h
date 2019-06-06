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
    CODEC_ID_MPEG4,
    // audio codec
    CODEC_ID_AAC = 0x100,
};

enum {
    ///< unsigned 8 bits
    SAMPLE_FMT_U8,
    ///< signed 16 bits
    SAMPLE_FMT_S16,
    ///< signed 32 bits
    SAMPLE_FMT_S32,
    ///< float
    SAMPLE_FMT_FLT,
    ///< double
    SAMPLE_FMT_DBL,

    ///< unsigned 8 bits, planar
    SAMPLE_FMT_U8P,
    ///< signed 16 bits, planar
    SAMPLE_FMT_S16P,
    ///< signed 32 bits, planar
    SAMPLE_FMT_S32P,
    ///< float, planar
    SAMPLE_FMT_FLTP,
    ///< double, planar
    SAMPLE_FMT_DBLP,
};

inline int getBytesPerSample(int sampleFmt) {
    switch (sampleFmt) {
        case SAMPLE_FMT_U8:
        case SAMPLE_FMT_U8P:
            return 1;
        case SAMPLE_FMT_S16:
        case SAMPLE_FMT_S16P:
            return 2;
        case SAMPLE_FMT_S32:
        case SAMPLE_FMT_S32P:
        case SAMPLE_FMT_FLT:
        case SAMPLE_FMT_FLTP:
            return 4;
        case SAMPLE_FMT_DBL:
        case SAMPLE_FMT_DBLP:
            return 8;
        default:
            return 2;
    }
}

#endif  // __AVCONST_H__
