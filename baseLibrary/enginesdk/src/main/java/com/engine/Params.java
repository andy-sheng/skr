package com.engine;

import io.agora.rtc.Constants;
import io.agora.rtc.video.VideoEncoderConfiguration;

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
    private boolean enableVideo = true;
    private boolean enableAudio = true;
    private int localVideoWidth = 360; //本地视频的分辨率，会影响对端获取的流大小，确保是2的倍数
    private int localVideoHeight = 640;
    private VideoEncoderConfiguration.FRAME_RATE rateFps = VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24; // 帧率，取值范围为 [1,7,10,15,24,30]
    /**
     * STANDARD_BITRATE
     * final int io.agora.rtc.video.VideoEncoderConfiguration.STANDARD_BITRATE = 0
     * static
     * 标准码率模式。该模式下，视频在通信和直播模式下的码率有所不同：
     * <p>
     * 通信模式下，码率与基准码率一致
     * 直播模式下，码率对照基准码率翻倍
     * ◆ COMPATIBLE_BITRATE
     * final int io.agora.rtc.video.VideoEncoderConfiguration.COMPATIBLE_BITRATE = -1
     * static
     * 适配码率模式。该模式下，视频在通信和直播模式下的码率均与基准码率一致。直播下如果选择该模式，可能会导致帧率低于设置的值
     */
    private int bitrate = VideoEncoderConfiguration.STANDARD_BITRATE;

    /**
     * 解释下 ORIENTATION_MODE_ADAPTIVE
     * io.agora.rtc.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE =(0)
     * （默认）该模式下 SDK 输出的视频方向与采集到的视频方向一致。接收端会根据收到的视频旋转信息对视频进行旋转。该模式适用于接收端可以调整视频方向的场景
     * <p>
     * 如果采集的视频是横屏模式，则输出的视频也是横屏模式
     * 如果采集的视频是竖屏模式，则输出的视频也是竖屏模式
     */
    private VideoEncoderConfiguration.ORIENTATION_MODE orientationMode = VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE;

    private Constants.AudioProfile audioProfile = Constants.AudioProfile.MUSIC_HIGH_QUALITY_STEREO; // 默认 最牛逼的 音乐编码 双声道 192kbps

    private Constants.AudioScenario audioScenario = Constants.AudioScenario.SHOWROOM; // 秀场场景

    private boolean enableAudioQualityIndication = true;

    private int volumeIndicationInterval = 300;

    private int volumeIndicationSmooth = 3;

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

    public int getLocalVideoWidth() {
        return localVideoWidth;
    }

    public void setLocalVideoWidth(int localVideoWidth) {
        this.localVideoWidth = localVideoWidth;
    }

    public int getLocalVideoHeight() {
        return localVideoHeight;
    }

    public void setLocalVideoHeight(int localVideoHeight) {
        this.localVideoHeight = localVideoHeight;
    }

    public VideoEncoderConfiguration.FRAME_RATE getRateFps() {
        return rateFps;
    }

    public void setRateFps(VideoEncoderConfiguration.FRAME_RATE rateFps) {
        this.rateFps = rateFps;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public VideoEncoderConfiguration.ORIENTATION_MODE getOrientationMode() {
        return orientationMode;
    }

    public void setOrientationMode(VideoEncoderConfiguration.ORIENTATION_MODE orientationMode) {
        this.orientationMode = orientationMode;
    }

    public boolean isEnableAudio() {
        return enableAudio;
    }

    public void setEnableAudio(boolean enableAudio) {
        this.enableAudio = enableAudio;
    }

    public Constants.AudioProfile getAudioProfile() {
        return audioProfile;
    }

    public void setAudioProfile(Constants.AudioProfile audioProfile) {
        this.audioProfile = audioProfile;
    }

    public Constants.AudioScenario getAudioScenario() {
        return audioScenario;
    }

    public void setAudioScenario(Constants.AudioScenario audioScenario) {
        this.audioScenario = audioScenario;
    }

    public boolean isEnableAudioQualityIndication() {
        return enableAudioQualityIndication;
    }

    public void setEnableAudioQualityIndication(boolean enableAudioQualityIndication) {
        this.enableAudioQualityIndication = enableAudioQualityIndication;
    }

    public int getVolumeIndicationInterval() {
        return volumeIndicationInterval;
    }

    public void setVolumeIndicationInterval(int volumeIndicationInterval) {
        this.volumeIndicationInterval = volumeIndicationInterval;
    }

    public int getVolumeIndicationSmooth() {
        return volumeIndicationSmooth;
    }

    public void setVolumeIndicationSmooth(int volumeIndicationSmooth) {
        this.volumeIndicationSmooth = volumeIndicationSmooth;
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

        public Builder setLocalVideoWidth(int localVideoWidth) {
            mParams.setLocalVideoWidth(localVideoWidth);
            return this;
        }

        public Builder setLocalVideoHeight(int localVideoHeight) {
            mParams.setLocalVideoHeight(localVideoHeight);
            return this;
        }

        public Builder setRateFps(VideoEncoderConfiguration.FRAME_RATE rateFps) {
            mParams.setRateFps(rateFps);
            return this;
        }

        public Builder setBitrate(int bitrate) {
            mParams.setBitrate(bitrate);
            return this;
        }

        public Builder setOrientationMode(VideoEncoderConfiguration.ORIENTATION_MODE orientationMode) {
            mParams.setOrientationMode(orientationMode);
            return this;
        }

        public Builder setEnableAudio(boolean enableAudio) {
            mParams.setEnableAudio(enableAudio);
            return this;
        }

        public Builder setAudioProfile(Constants.AudioProfile audioProfile) {
            mParams.setAudioProfile(audioProfile);
            return this;
        }

        public Builder setAudioScenario(Constants.AudioScenario audioScenario) {
            mParams.setAudioScenario(audioScenario);
            return this;
        }


        public Builder setEnableAudioQualityIndication(boolean enableAudioQualityIndication) {
            mParams.setEnableAudioQualityIndication(enableAudioQualityIndication);
            return this;
        }


        public Builder setVolumeIndicationInterval(int volumeIndicationInterval) {
            this.setVolumeIndicationInterval(volumeIndicationInterval);
            return this;
        }


        public Builder setVolumeIndicationSmooth(int volumeIndicationSmooth) {
            this.setVolumeIndicationSmooth(volumeIndicationSmooth);
            return this;
        }

        public Params build() {
            return mParams;
        }
    }
}
