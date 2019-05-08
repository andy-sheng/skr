package com.zq.mediaengine.framework;

/**
 * Configuration of audio encoder.
 */

public class AudioEncodeConfig {
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

    public AudioEncodeConfig(int sampleFmt, int codecId, int sampleRate, int channels, int bitrate) {
        this.sampleFmt = sampleFmt;
        this.codecId = codecId;
        if (codecId == AVConst.CODEC_ID_AAC) {
            this.profile = AVConst.PROFILE_AAC_LOW;
        }
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.bitrate = bitrate;
    }

    public AudioEncodeConfig(AudioEncodeConfig config) {
        this.sampleFmt = config.sampleFmt;
        this.codecId = config.codecId;
        this.profile = config.profile;
        this.sampleRate = config.sampleRate;
        this.channels = config.channels;
        this.bitrate = config.bitrate;
    }
}
