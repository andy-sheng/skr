package com.zq.mediaengine.framework;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;

/**
 * Encode params for audio.
 */
public class AudioCodecFormat {
    /**
     * Audio sample format, see AVConst.AV_SAMPLE_FMT_XXX
     */
    public int sampleFmt;
    /**
     * Audio encode codec id, see AVConst.CODEC_ID_XXX
     */
    public int codecId;
    /**
     * Audio encode profile, see AVConst.PROFILE_XXX
     */
    public int profile;
    /**
     * Audio sample rate in Hz
     */
    public int sampleRate;
    /**
     * Audio channel numbers, 1 for mono, 2 for stereo
     */
    public int channels;
    /**
     * Audio encode bitrate in bps
     */
    public int bitrate;
    /**
     * ptr of AVCodecParameters
     */
    public long avCodecParPtr;

    public AudioCodecFormat(int codecId, int sampleFmt, int sampleRate,
                            int channels, int bitrate) {
        this.codecId = codecId;
        if (codecId == AVConst.CODEC_ID_AAC) {
            this.profile = AVConst.PROFILE_AAC_LOW;
        }
        this.sampleFmt = sampleFmt;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.bitrate = bitrate;
        this.avCodecParPtr = 0;
    }

    public AudioCodecFormat(AudioCodecFormat format) {
        codecId = format.codecId;
        profile = format.profile;
        sampleFmt = format.sampleFmt;
        sampleRate = format.sampleRate;
        channels = format.channels;
        bitrate = format.bitrate;
        avCodecParPtr = format.avCodecParPtr;
    }

    public MediaFormat toMediaFormat() {
        String mime;
        switch (codecId) {
            case AVConst.CODEC_ID_AAC:
                mime = "audio/mp4a-latm";
                break;
            default:
                throw new IllegalArgumentException("Only aac supported");
        }

        int audioProfile = profile;
        if (audioProfile == AVConst.PROFILE_AAC_HE_V2 && channels == 1) {
            audioProfile = AVConst.PROFILE_AAC_HE;
        }
        int profile;
        switch (audioProfile) {
            case AVConst.PROFILE_AAC_HE:
                profile = MediaCodecInfo.CodecProfileLevel.AACObjectHE;
                break;
            case AVConst.PROFILE_AAC_HE_V2:
                profile = MediaCodecInfo.CodecProfileLevel.AACObjectHE_PS;
                break;
            case AVConst.PROFILE_AAC_LOW:
            default:
                profile = MediaCodecInfo.CodecProfileLevel.AACObjectLC;
                break;
        }

        MediaFormat mediaFormat = MediaFormat.createAudioFormat(mime, sampleRate, channels);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, profile);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384);

        return mediaFormat;
    }
}
