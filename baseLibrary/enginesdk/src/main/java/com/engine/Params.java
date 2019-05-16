package com.engine;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.common.log.MyLog;
import com.common.utils.U;

import java.io.Serializable;
import java.util.HashMap;

import io.agora.rtc.Constants;
import io.agora.rtc.video.VideoEncoderConfiguration;

/**
 * 引擎一些参数配置，在初始化时传入
 * 会影响引擎初始化的一些属性
 * 对于含义不清楚的参数，要看这个参数在哪里使用的
 */
public class Params implements Serializable {
    public static final int CHANNEL_TYPE_COMMUNICATION = Constants.CHANNEL_PROFILE_COMMUNICATION;
    public static final int CHANNEL_TYPE_LIVE_BROADCASTING = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;

    @JSONField(serialize=false)
    private int channelProfile = CHANNEL_TYPE_LIVE_BROADCASTING;
    @JSONField(serialize=false)
    private Scene scene = Scene.audiotest;
    // 是否使用唱吧的引擎
    @JSONField(serialize=false)
    private boolean useCbEngine = false;
    @JSONField(serialize=false)
    private boolean enableVideo = false;
    @JSONField(serialize=false)
    private boolean enableAudio = true;
    @JSONField(serialize=false)
    private int localVideoWidth = 360; //本地视频的分辨率，会影响对端获取的流大小，确保是2的倍数
    @JSONField(serialize=false)
    private int localVideoHeight = 640;
    @JSONField(serialize=false)
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
    @JSONField(serialize=false)
    private int bitrate = VideoEncoderConfiguration.STANDARD_BITRATE;

    /**
     * 解释下 ORIENTATION_MODE_ADAPTIVE
     * io.agora.rtc.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE =(0)
     * （默认）该模式下 SDK 输出的视频方向与采集到的视频方向一致。接收端会根据收到的视频旋转信息对视频进行旋转。该模式适用于接收端可以调整视频方向的场景
     * <p>
     * 如果采集的视频是横屏模式，则输出的视频也是横屏模式
     * 如果采集的视频是竖屏模式，则输出的视频也是竖屏模式
     * <p>
     * 注意注意，会影响FistDDecode的 width height 以及 rotation
     */
    @JSONField(serialize=false)
    private VideoEncoderConfiguration.ORIENTATION_MODE orientationMode = VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;

    @JSONField(serialize=false)
    private boolean enableAudioQualityIndication = true;// 开启音量提示

    @JSONField(serialize=false)
    private int volumeIndicationInterval = 300; // 是谁在说话提示最小间隔
    @JSONField(serialize=false)
    private int volumeIndicationSmooth = 3; // 平滑程度

    @JSONField(serialize=false)
    private boolean mixMusicPlaying = false; // 混音在播放中
    @JSONField(serialize=false)
    private boolean cameraAutoFocusFaceModeEnabled = true;// 相机自动对焦开启

    private double localVoicePitch = 1.0; // 音调
    private int bandFrequency = 0; // 子带频率
    private int bandGain = 0;// 子带增益
    private HashMap<Integer, Integer> localVoiceReverb = new HashMap<>(); // 存混响参数

    private int audioMixingVolume = 50; // 混音音量 0-100，默认是100
    private boolean enableInEarMonitoring = false;// 耳返
    private int earMonitoringVolume = 80; // 耳返音量
    private int playbackSignalVolume = 100;// 0-400 默认100，最多放大4倍
    private int recordingSignalVolume = 200;// 0-400 默认100，最多放大4倍
    private AudioEffect styleEnum = AudioEffect.none;// 混响style

    @JSONField(serialize=false)
    private boolean cameraTorchOn = false; // 闪光灯常亮
    @JSONField(serialize=false)
    private int selfUid; // 本人在引擎中的id
    @JSONField(serialize=false)
    private boolean enableSpeakerphone = false;// 开启扬声器
    @JSONField(serialize=false)
    private boolean allRemoteAudioStreamsMute = false;// 禁其他音频流
    @JSONField(serialize=false)
    private boolean localAudioStreamMute = false; // 本地音频流禁止
    @JSONField(serialize=false)
    private boolean localVideoStreamMute = false;// 本地视频流禁止
    @JSONField(serialize=false)
    private boolean allRemoteVideoStreamsMute = false;// 拒接所有其他视频流
    @JSONField(serialize=false)
    private String mMixMusicFilePath;// 伴奏路径
    @JSONField(serialize=false)
    private String mMidiPath;// midi打分文件路径
    @JSONField(serialize=false)
    private long mMixMusicBeginOffset;// midi文件起始偏移量
    @JSONField(serialize=false)
    private int mCurrentMusicTs;// 当前伴奏的偏移
    @JSONField(serialize=false)
    private long mRecordCurrentMusicTsTs;// 记录当前伴奏偏移的物理时间戳，在什么时间记录了这次偏移
    @JSONField(serialize=false)
    private boolean mLrcHasStart; // 歌词是否开始
    @JSONField(serialize=false)
    private long mJoinRoomBeginTs; // 开始加入房间的时间
    @JSONField(serialize=false)
    private String mRecordingFromCallbackSavePath;// 保存录音的路径
    @JSONField(serialize=false)
    private boolean mIsAnchor;
    @JSONField(serialize=false)
    private boolean mGrabSingNoAcc; // 一唱到底清唱模式

    public static Builder newBuilder(int channelProfile) {
        return new Builder().setChannelProfile(channelProfile);
    }

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

    public boolean isCameraAutoFocusFaceModeEnabled() {
        return cameraAutoFocusFaceModeEnabled;
    }

    public void setCameraAutoFocusFaceModeEnabled(boolean cameraAutoFocusFaceModeEnabled) {
        this.cameraAutoFocusFaceModeEnabled = cameraAutoFocusFaceModeEnabled;
    }

    public double getLocalVoicePitch() {
        return localVoicePitch;
    }

    public void setLocalVoicePitch(double localVoicePitch) {
        this.localVoicePitch = localVoicePitch;
    }

    public int getBandFrequency() {
        return bandFrequency;
    }

    public void setBandFrequency(int bandFrequency) {
        this.bandFrequency = bandFrequency;
    }

    public int getBandGain() {
        return bandGain;
    }

    public void setBandGain(int bandGain) {
        this.bandGain = bandGain;
    }

    public boolean isMixMusicPlaying() {
        return mixMusicPlaying;
    }

    public void setMixMusicPlaying(boolean mixMusicPlaying) {
        this.mixMusicPlaying = mixMusicPlaying;
    }

    public int getAudioMixingVolume() {
        return audioMixingVolume;
    }

    public void setAudioMixingVolume(int audioMixingVolume) {
        this.audioMixingVolume = audioMixingVolume;
    }

    public void setEnableInEarMonitoring(boolean enableInEarMonitoring) {
        this.enableInEarMonitoring = enableInEarMonitoring;
    }

    public boolean isEnableInEarMonitoring() {
        return enableInEarMonitoring;
    }

    public void setInEarMonitoringVolume(int inEarMonitoringVolume) {
        earMonitoringVolume = inEarMonitoringVolume;
    }

    public int getInEarMonitoringVolume() {
        return earMonitoringVolume;
    }

    public void setLocalVoiceReverb(int reverbKey, int value) {
        localVoiceReverb.put(reverbKey, value);
    }

    public int getLocalVoiceReverb(int reverbKey) {
        Integer v = localVoiceReverb.get(reverbKey);
        if (v == null) {
            return 0;
        }
        return v;
    }

    public void setAllRemoteAudioStreamsMute(boolean allRemoteAudioStreamsMute) {
        this.allRemoteAudioStreamsMute = allRemoteAudioStreamsMute;
    }

    public boolean isAllRemoteAudioStreamsMute() {
        return allRemoteAudioStreamsMute;
    }

    public void setLocalAudioStreamMute(boolean localAudioStreamMute) {
        this.localAudioStreamMute = localAudioStreamMute;
    }

    public boolean isLocalAudioStreamMute() {
        return localAudioStreamMute;
    }

    public void setCameraTorchOn(boolean cameraTorchOn) {
        this.cameraTorchOn = cameraTorchOn;
    }

    public boolean getCameraTorchOn() {
        return cameraTorchOn;
    }

    public void setLocalVideoStreamMute(boolean localVideoStreamMute) {
        this.localVideoStreamMute = localVideoStreamMute;
    }

    public boolean getLocalVideoStreamMute() {
        return localVideoStreamMute;
    }

    public void setAllRemoteVideoStreamsMute(boolean allRemoteVideoStreamsMute) {
        this.allRemoteVideoStreamsMute = allRemoteVideoStreamsMute;
    }

    public boolean getAllRemoteVideoStreamsMute() {
        return allRemoteVideoStreamsMute;
    }

    public void setPlaybackSignalVolume(int playbackSignalVolume) {
        this.playbackSignalVolume = playbackSignalVolume;
    }

    public int getPlaybackSignalVolume() {
        return playbackSignalVolume;
    }

    public void setRecordingSignalVolume(int recordingSignalVolume) {
        this.recordingSignalVolume = recordingSignalVolume;
    }

    public int getRecordingSignalVolume() {
        return recordingSignalVolume;
    }

    public void setSelfUid(int selfUid) {
        this.selfUid = selfUid;
    }

    public int getSelfUid() {
        return selfUid;
    }

    public AudioEffect getStyleEnum() {
        return styleEnum;
    }

    public void setStyleEnum(AudioEffect styleEnum) {
        this.styleEnum = styleEnum;
    }

    public boolean isEnableSpeakerphone() {
        return enableSpeakerphone;
    }

    public void setEnableSpeakerphone(boolean enableSpeakerphone) {
        this.enableSpeakerphone = enableSpeakerphone;
    }

    public void setMixMusicFilePath(String mixMusicFilePath) {
        mMixMusicFilePath = mixMusicFilePath;
    }

    public String getMixMusicFilePath() {
        return mMixMusicFilePath;
    }

    public int getEarMonitoringVolume() {
        return earMonitoringVolume;
    }

    public void setEarMonitoringVolume(int earMonitoringVolume) {
        this.earMonitoringVolume = earMonitoringVolume;
    }

    public HashMap<Integer, Integer> getLocalVoiceReverb() {
        return localVoiceReverb;
    }

    public void setLocalVoiceReverb(HashMap<Integer, Integer> localVoiceReverb) {
        this.localVoiceReverb = localVoiceReverb;
    }

    public boolean isCameraTorchOn() {
        return cameraTorchOn;
    }

    public boolean isLocalVideoStreamMute() {
        return localVideoStreamMute;
    }

    public boolean isAllRemoteVideoStreamsMute() {
        return allRemoteVideoStreamsMute;
    }

    public void setMidiPath(String midiPath) {
        mMidiPath = midiPath;
    }

    public String getMidiPath() {
        return mMidiPath;
    }

    public void setMixMusicBeginOffset(long mixMusicBeginOffset) {
        mMixMusicBeginOffset = mixMusicBeginOffset;
    }

    public long getMixMusicBeginOffset() {
        return mMixMusicBeginOffset;
    }

    public void setCurrentMusicTs(int currentMusicTs) {
        mCurrentMusicTs = currentMusicTs;
    }

    public int getCurrentMusicTs() {
        return mCurrentMusicTs;
    }

    public void setRecordCurrentMusicTsTs(long recordCurrentMusicTsTs) {
        mRecordCurrentMusicTsTs = recordCurrentMusicTsTs;
    }

    public long getRecordCurrentMusicTsTs() {
        return mRecordCurrentMusicTsTs;
    }

    public void setLrcHasStart(boolean lrcHasStart) {
        mLrcHasStart = lrcHasStart;
    }

    public boolean getLrcHasStart() {
        return mLrcHasStart;
    }

    public void setJoinRoomBeginTs(long joinRoomBeginTs) {
        mJoinRoomBeginTs = joinRoomBeginTs;
    }

    public long getJoinRoomBeginTs() {
        return mJoinRoomBeginTs;
    }

    public void setRecordingFromCallbackSavePath(String recordingFromCallbackSavePath) {
        mRecordingFromCallbackSavePath = recordingFromCallbackSavePath;
    }

    public String getRecordingFromCallbackSavePath() {
        return mRecordingFromCallbackSavePath;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public void setAnchor(boolean anchor) {
        mIsAnchor = anchor;
    }

    public boolean isAnchor() {
        return mIsAnchor;
    }

    public void setGrabSingNoAcc(boolean grabSingNoAcc) {
        mGrabSingNoAcc = grabSingNoAcc;
    }

    public boolean isGrabSingNoAcc() {
        return mGrabSingNoAcc;
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

        public Builder setEnableAudioQualityIndication(boolean enableAudioQualityIndication) {
            mParams.setEnableAudioQualityIndication(enableAudioQualityIndication);
            return this;
        }

        public Builder setVolumeIndicationInterval(int volumeIndicationInterval) {
            mParams.setVolumeIndicationInterval(volumeIndicationInterval);
            return this;
        }

        public Builder setVolumeIndicationSmooth(int volumeIndicationSmooth) {
            mParams.setVolumeIndicationSmooth(volumeIndicationSmooth);
            return this;
        }

        public Builder setCameraAutoFocusFaceModeEnabled(boolean cameraAutoFocusFaceModeEnabled) {
            mParams.setCameraAutoFocusFaceModeEnabled(cameraAutoFocusFaceModeEnabled);
            return this;
        }

        public Builder setLocalVoicePitch(double localVoicePitch) {
            mParams.setLocalVoicePitch(localVoicePitch);
            return this;
        }

        public Builder setBandFrequency(int bandFrequency) {
            mParams.setBandFrequency(bandFrequency);
            return this;
        }

        public Builder setBandGain(int bandGain) {
            mParams.setBandGain(bandGain);
            return this;
        }

        public Builder setAudioMixingVolume(int audioMixingVolume) {
            mParams.setAudioMixingVolume(audioMixingVolume);
            return this;
        }

        public Builder setEnableInEarMonitoring(boolean enableInEarMonitoring) {
            mParams.setEnableInEarMonitoring(enableInEarMonitoring);
            return this;
        }

        public Builder setInEarMonitoringVolume(int inEarMonitoringVolume) {
            mParams.setInEarMonitoringVolume(inEarMonitoringVolume);
            return this;
        }

        public Builder setAllRemoteAudioStreamsMute(boolean allRemoteAudioStreamsMute) {
            mParams.setAllRemoteAudioStreamsMute(allRemoteAudioStreamsMute);
            return this;
        }

        public Builder setLocalVoiceReverb(int reverbKey, int value) {
            mParams.setLocalVoiceReverb(reverbKey, value);
            return this;
        }

        public Builder setLocalAudioStreamMute(boolean localAudioStreamMute) {
            mParams.setLocalAudioStreamMute(localAudioStreamMute);
            return this;
        }

        public Builder setPlaybackSignalVolume(int playbackSignalVolume) {
            mParams.setPlaybackSignalVolume(playbackSignalVolume);
            return this;
        }

        public Builder setRecordingSignalVolume(int recordingSignalVolume) {
            mParams.setRecordingSignalVolume(recordingSignalVolume);
            return this;
        }

        public Builder setEnableSpeakerphone(boolean enableSpeakerphone) {
            mParams.setEnableSpeakerphone(enableSpeakerphone);
            return this;
        }

        public Builder setStyleEnum(AudioEffect styleEnum) {
            mParams.setStyleEnum(styleEnum);
            return this;
        }

        public Builder setScene(Scene scene) {
            mParams.setScene(scene);
            return this;
        }

        public Params build() {
            return mParams;
        }
    }

    /**
     * 存起引擎的偏好的信息到Pref
     *
     * @param params
     */
    public static void save2Pref(Params params) {
        if (params != null) {
            String s = JSON.toJSONString(params);
            MyLog.w(EngineManager.TAG, "save2Pref " + s);
            U.getPreferenceUtils().setSettingString("engine_pref_params3", s);
        }
    }

    /**
     * 得到偏好的引擎设置，一般是在练歌房训练出来的
     *
     * @return
     */
    public static Params getFromPref() {
        String s = U.getPreferenceUtils().getSettingString("engine_pref_params3", "");
        MyLog.w(EngineManager.TAG, "getFromPref " + s);
        Params params;
        if (!TextUtils.isEmpty(s)) {
            params = JSON.parseObject(s, Params.class);
        } else {
            params = Params.newBuilder(Params.CHANNEL_TYPE_LIVE_BROADCASTING)
                    .setEnableVideo(false)
                    .setEnableAudio(true)
                    .setUseCbEngine(false)
                    .setStyleEnum(AudioEffect.none)
                    .build();
        }
//        params.setAudioMixingVolume(0);
        return params;
    }

    public enum Scene {
        rank, grab, voice, audiotest
    }

    public enum AudioEffect {
        none, dianyin, kongling, ktv, rock
    }
}
