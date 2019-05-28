package com.zq.mediaengine.framework;

/**
 * A/V const values definition.
 */
public class AVConst {
    // audio sample format, sync with ffmpeg definition
    public static final int AV_SAMPLE_FMT_U8   = 0;
    public static final int AV_SAMPLE_FMT_S16  = 1;
    public static final int AV_SAMPLE_FMT_S32  = 2;
    public static final int AV_SAMPLE_FMT_FLT  = 3;
    public static final int AV_SAMPLE_FMT_DBL  = 4;
    public static final int AV_SAMPLE_FMT_U8P  = 5;
    public static final int AV_SAMPLE_FMT_S16P = 6;
    public static final int AV_SAMPLE_FMT_S32P = 7;
    public static final int AV_SAMPLE_FMT_FLTP = 8;
    public static final int AV_SAMPLE_FMT_DBLP = 9;

    public static final int PIX_FMT_NONE = 0x00;
    public static final int PIX_FMT_NV21 = 0x01;
    public static final int PIX_FMT_YV12 = 0x02;
    public static final int PIX_FMT_I420 = 0x03;
    public static final int PIX_FMT_ARGB = 0x04;
    public static final int PIX_FMT_RGBA = 0x05;
    public static final int PIX_FMT_BGR8 = 0x06;

    // codec id
    public static final int CODEC_ID_NONE  = 0x000;
    public static final int CODEC_ID_AVC   = 0x001;
    public static final int CODEC_ID_HEVC  = 0x002;
    public static final int CODEC_ID_GIF   = 0x003;
    public static final int CODEC_ID_MPEG4 = 0x004;
    public static final int CODEC_ID_AAC   = 0x100;

    // codec profile
    public static final int PROFILE_AAC_LOW = 1;
    public static final int PROFILE_AAC_HE = 4;
    public static final int PROFILE_AAC_HE_V2 = 28;

    // A/V frame additional info
    public static final int FLAG_KEY_FRAME = 1;
    public static final int FLAG_CODEC_CONFIG = 1<<1;
    public static final int FLAG_END_OF_STREAM = 1<<2;
    public static final int FLAG_P_FRAME = 1<<3;
    public static final int FLAG_B_FRAME = 1<<4;
    public static final int FLAG_FLUSH_OF_STREAM = 1<<5;
    public static final int FLAG_FLUSH_DECODER = 1<<6;
    /**
     * Audio native module destroying
     */
    public static final int FLAG_DETACH_NATIVE_MODULE = 0x10000;

    // media type definition
    public static final int MEDIA_TYPE_UNKNOWN = 0;
    public static final int MEDIA_TYPE_AUDIO = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static int getBytesPerSample(int sampleFmt) {
        switch (sampleFmt) {
            case AV_SAMPLE_FMT_U8:
            case AV_SAMPLE_FMT_U8P:
                return 1;
            case AV_SAMPLE_FMT_S16:
            case AV_SAMPLE_FMT_S16P:
                return 2;
            case AV_SAMPLE_FMT_S32:
            case AV_SAMPLE_FMT_S32P:
            case AV_SAMPLE_FMT_FLT:
            case AV_SAMPLE_FMT_FLTP:
                return 4;
            case AV_SAMPLE_FMT_DBL:
            case AV_SAMPLE_FMT_DBLP:
                return 8;
            default:
                return 2;
        }
    }
}
