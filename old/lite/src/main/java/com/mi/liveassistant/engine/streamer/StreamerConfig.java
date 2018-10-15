package com.mi.liveassistant.engine.streamer;

/**
 * Created by chenyong on 16/5/23.
 */
public class StreamerConfig {

    int mSampleAudioRateInHz;
    int mFrameRate;
    int mMaxAverageVideoBitrate;
    int mMinAverageVideoBitrate;
    int mTargetWidth;
    int mTargetHeight;
    boolean mAutoAdjustBitrate;
    int beautyLevel = 0;
    boolean mMuteAudio;
    boolean mIsReverse;

    /**
     * 直播配置Builder设置类
     *
     * @param builder
     */
    public StreamerConfig(Builder builder) {
        mSampleAudioRateInHz = builder.mSampleAudioRateInHz;
        mFrameRate = builder.mFrameRate;
        mMaxAverageVideoBitrate = builder.mMaxAverageVideoBitrate;
        mMinAverageVideoBitrate = builder.mMinAverageVideoBitrate;
        mTargetWidth = builder.mTargetWidth;
        mTargetHeight = builder.mTargetHeight;
        mAutoAdjustBitrate = builder.mAutoAdjustBitrate;
        beautyLevel = builder.beautyLevel;
        mMuteAudio = builder.mMuteAudio;
        mIsReverse = builder.mIsReverse;
    }

    /**
     * 直播配置Builder类
     */
    public static class Builder {
        private int mSampleAudioRateInHz = RecorderConstants.DEFAULT_SAMPLE_RATE;
        private int mFrameRate = RecorderConstants.DEFAULT_FRAME_RATE;
        private int mMaxAverageVideoBitrate = RecorderConstants.DEFAULT_MAX_VIDEO_BITRATE;
        private int mMinAverageVideoBitrate = RecorderConstants.DEFAULT_MIN_VIDEO_BITRATE;
        private int mTargetWidth = RecorderConstants.TARGET_VIDEO_WIDTH;
        private int mTargetHeight = RecorderConstants.TARGET_VIDEO_HEIGHT;
        private boolean mAutoAdjustBitrate = true;
        private boolean mMuteAudio = false;
        private int beautyLevel = 0;
        private boolean mIsReverse = false;

        public StreamerConfig build() {
            return new StreamerConfig(this);
        }

        public int getSampleAudioRateInHz() {
            return mSampleAudioRateInHz;
        }

        /**
         * 设置音频采样率
         *
         * @param sampleAudioRateInHz 音频采样率
         */
        public Builder setSampleAudioRateInHz(int sampleAudioRateInHz) {
            mSampleAudioRateInHz = sampleAudioRateInHz;
            return this;
        }

        public int getFrameRate() {
            return mFrameRate;
        }

        public void setMuteAudio(boolean mute_audio) {
            this.mMuteAudio = mute_audio;
        }

        /**
         * 设置视频帧率
         *
         * @param frameRate 视频帧率
         */
        public void setFrameRate(int frameRate) {
            mFrameRate = frameRate;
        }

        public int getMaxAverageVideoBitrate() {
            return mMaxAverageVideoBitrate;
        }

        /**
         * 设置视频码率
         *
         * @param videoBitrate 视频码率
         */
        public Builder setMaxAverageVideoBitrate(int videoBitrate) {
            mMaxAverageVideoBitrate = videoBitrate;
            return this;
        }

        public int getMinAverageVideoBitrate() {
            return mMinAverageVideoBitrate;
        }

        /**
         * 设置视频码率
         *
         * @param videoBitrate 视频码率
         */
        public Builder setMinAverageVideoBitrate(int videoBitrate) {
            mMinAverageVideoBitrate = videoBitrate;
            return this;
        }

        public int getTargetWidth() {
            return mTargetWidth;
        }

        public Builder setTargetWidth(int targetWidth) {
            this.mTargetWidth = targetWidth;
            return this;
        }

        public int getTargetHeight() {
            return mTargetHeight;
        }

        public Builder setTargetHeight(int targetHeight) {
            this.mTargetHeight = targetHeight;
            return this;
        }

        public boolean isAutoAdjustBitrate() {
            return mAutoAdjustBitrate;
        }

        public void setAutoAdjustBitrate(boolean mAutoAdjustBitrate) {
            this.mAutoAdjustBitrate = mAutoAdjustBitrate;
        }

        public int getBeautyLevel() {
            return beautyLevel;
        }

        public void setBeautyLevel(int beautyLevel) {
            this.beautyLevel = beautyLevel;
        }

        public boolean isPictureReverse() {
            return mIsReverse;
        }

        public void setPictureReverse(boolean isReverse) {
            mIsReverse = isReverse;
        }
    }

    public int getSampleAudioRateInHz() {
        return mSampleAudioRateInHz;
    }

    /**
     * 设置音频采样率
     *
     * @param sampleAudioRateInHz 音频采样率
     */
    public void setSampleAudioRateInHz(int sampleAudioRateInHz) {
        mSampleAudioRateInHz = sampleAudioRateInHz;
    }

    public int getFrameRate() {
        return mFrameRate;
    }

    /**
     * 设置视频帧率
     *
     * @param frameRate 视频帧率
     */
    public void setFrameRate(int frameRate) {
        mFrameRate = frameRate;
    }

    public int getMaxAverageVideoBitrate() {
        return mMaxAverageVideoBitrate;
    }

    /**
     * 设置视频码率，单位为Kbps
     *
     * @param videoBitrate 视频码率
     */
    public void setMaxAverageVideoBitrate(int videoBitrate) {
        mMaxAverageVideoBitrate = videoBitrate;
    }

    public int getMinAverageVideoBitrate() {
        return mMinAverageVideoBitrate;
    }

    /**
     * 设置视频码率，单位为Kbps
     *
     * @param videoBitrate 视频码率
     */
    public void setMinAverageVideoBitrate(int videoBitrate) {
        mMinAverageVideoBitrate = videoBitrate;
    }

    public void setMuteAudio(boolean muteAudio) {
        this.mMuteAudio = muteAudio;
    }

    public int getTargetWidth() {
        return mTargetWidth;
    }

    public void setTargetWidth(int targetWidth) {
        this.mTargetWidth = targetWidth;
    }

    public int getTargetHeight() {
        return mTargetHeight;
    }

    public void setTargetHeight(int targetHeight) {
        this.mTargetHeight = targetHeight;
    }

    public boolean isMuteAudio() {
        return mMuteAudio;
    }

    public boolean isAutoAdjustBitrate() {
        return mAutoAdjustBitrate;
    }

    /**
     * 是否开启自适应码率
     *
     * @param mAutoAdjustBitrate true为开启，false为关闭
     */
    public void setAutoAdjustBitrate(boolean mAutoAdjustBitrate) {
        this.mAutoAdjustBitrate = mAutoAdjustBitrate;
    }

    public int getBeautyLevel() {
        return beautyLevel;
    }

    public void setBeautyLevel(int beautyLevel) {
        this.beautyLevel = beautyLevel;
    }

    public boolean isPictureReverse() {
        return mIsReverse;
    }

    public void setPictureReverse(boolean isReverse) {
        mIsReverse = isReverse;
    }
}
