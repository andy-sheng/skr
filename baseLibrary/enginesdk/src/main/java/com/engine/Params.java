package com.engine;

import io.agora.rtc.Constants;

/**
 * 引擎一些参数配置，在初始化时传入
 * 会影响引擎初始化的一些属性
 */
public class Params {
    public static final int CHANNEL_TYPE_COMMUNICATION = Constants.CHANNEL_PROFILE_COMMUNICATION;
    public static final int CHANNEL_TYPE_LIVE_BROADCASTING = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;

    private int channelProfile = CHANNEL_TYPE_LIVE_BROADCASTING;
    // 是否使用唱吧的引擎
    private boolean useCbEngine = true;
    private boolean enableVideo = false;

    public int getChannelProfile() {
        return channelProfile;
    }

    public void setChannelProfile(int channelProfile) {
        this.channelProfile = channelProfile;
    }

    public boolean isUseCbEngine() {
        return useCbEngine;
    }

    public void setUseCbEngine(boolean useCbEngine) {
        this.useCbEngine = useCbEngine;
    }

    public boolean isEnableVideo() {
        return enableVideo;
    }

    public void setEnableVideo(boolean enableVideo) {
        this.enableVideo = enableVideo;
    }

    public static Params.Builder newBuilder(int channelProfile) {
        return new Builder().setChannelProfile(channelProfile);
    }

    public static class Builder {
        Params mParams = new Params();

        Builder() {
        }

        public Builder setChannelProfile(int channelProfile) {
            mParams.setChannelProfile(channelProfile);
            return this;
        }

        public Builder setUseCbEngine(boolean useCbEngine) {
            mParams.setUseCbEngine(useCbEngine);
            return this;
        }

        public Builder setEnableVideo(boolean enableVideo) {
            mParams.setEnableVideo(enableVideo);
            return this;
        }

        public Params build() {
            return mParams;
        }
    }
}
