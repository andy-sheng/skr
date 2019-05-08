package com.zq.mediaengine.framework;

/**
 * Configuration of video encoder.
 */

public class VideoEncodeConfig {
    /**
     * Pixel format of input video frame, see AVConst.PIX_FMT_XXX
     */
    public int pixFmt;
    /**
     * Video codec id, see AVConst.CODEC_ID_XXX
     */
    public int codecId;
    /**
     * Video encode profile, see AVConst.PROFILE_XXX
     */
    public int profile;
    /**
     * Width of input video frame
     */
    public int width;
    /**
     * Height of input video frame
     */
    public int height;
    /**
     * Video bitrate in bps
     */
    public int bitrate;
    /**
     * Video frame rate
     */
    public float frameRate;
    /**
     * Video encode key frame interval
     */
    public float iFrameInterval;

    public VideoEncodeConfig(int codecId, int width, int height, int bitrate) {
        this.pixFmt = AVConst.PIX_FMT_I420;
        this.codecId = codecId;
        if (codecId == AVConst.CODEC_ID_AVC) {
            this.profile = AVConst.PROFILE_H264_HIGH;
        } else if (codecId == AVConst.CODEC_ID_HEVC) {
            this.profile = AVConst.PROFILE_H265_MAIN;
        }
        this.width = width;
        this.height = height;
        this.bitrate = bitrate;
        this.frameRate = 15.0f;
        this.iFrameInterval = 3.0f;
    }

    public VideoEncodeConfig(VideoEncodeConfig config) {
        this.pixFmt = config.pixFmt;
        this.codecId = config.codecId;
        this.profile = config.profile;
        this.width = config.width;
        this.height = config.height;
        this.bitrate = config.bitrate;
        this.frameRate = config.frameRate;
        this.iFrameInterval = config.iFrameInterval;
    }
}
