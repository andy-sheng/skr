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
    public static final String TAG = "Params";
    public static final String PREF_KEY_TOKEN_ENABLE = "key_agora_token_enable";
    public static final int CHANNEL_TYPE_COMMUNICATION = Constants.CHANNEL_PROFILE_COMMUNICATION;
    public static final int CHANNEL_TYPE_LIVE_BROADCASTING = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;

    @JSONField(serialize = false)
    private int channelProfile = CHANNEL_TYPE_LIVE_BROADCASTING;
    @JSONField(serialize = false)
    private Scene scene = Scene.audiotest;
    @JSONField(serialize = false)
    private boolean enableVideo = false; // 视频是否可用
    @JSONField(serialize = false)
    private boolean enableAudio = true; // 音频是否可用
    @JSONField(serialize = false)
    private boolean useExternalVideo = true; // 自采集视频
    @JSONField(serialize = false)
    private boolean useExternalAudioRecord = true; // 自定义音频录制
    @JSONField(serialize = false)
    private boolean useLocalAPM = false; // 自采集时是否使用本地的APM处理模块，而不是声网的
    @JSONField(serialize = false)
    private int localVideoWidth = 360; //本地视频的分辨率，会影响对端获取的流大小，确保是2的倍数
    @JSONField(serialize = false)
    private int localVideoHeight = 640;
    @JSONField(serialize = false)
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
    @JSONField(serialize = false)
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
    @JSONField(serialize = false)
    private VideoEncoderConfiguration.ORIENTATION_MODE orientationMode = VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;

    @JSONField(serialize = false)
    private boolean enableAudioQualityIndication = true;// 开启音量提示

    @JSONField(serialize = false)
    private int volumeIndicationInterval = 300; // 是谁在说话提示最小间隔
    @JSONField(serialize = false)
    private int volumeIndicationSmooth = 3; // 平滑程度

    @JSONField(serialize = false)
    private boolean mixMusicPlaying = false; // 混音在播放中
    @JSONField(serialize = false)
    private boolean cameraAutoFocusFaceModeEnabled = true;// 相机自动对焦开启

    private double localVoicePitch = 1.0; // 音调
    private int bandFrequency = 0; // 子带频率
    private int bandGain = 0;// 子带增益
    private HashMap<Integer, Integer> localVoiceReverb = new HashMap<>(); // 存混响参数

    private int audioMixingPlayoutVolume = 60; // 混音音量本地音量
    private int audioMixingPublishVolume = 60; // 推出去的音量大小
    private boolean enableInEarMonitoring = false;// 耳返
    private int earMonitoringVolume = 100; // 耳返音量
    @JSONField(serialize = false)
    private int playbackSignalVolume = 100;// 0-400 默认100，最多放大4倍
    private int recordingSignalVolume = 200;// 0-400 默认100，最多放大4倍
    private AudioEffect styleEnum = AudioEffect.none;// 混响style

    @JSONField(serialize = false)
    private EngineConfigFromServer configFromServer =  EngineConfigFromServer.getDefault();

    @JSONField(serialize = false)
    private boolean enableAudioPreviewLatencyTest = false;
    @JSONField(serialize = false)
    private boolean enableAudioMixLatencyTest = false;

    @JSONField(serialize = false)
    private boolean cameraTorchOn = false; // 闪光灯常亮
    @JSONField(serialize = false)
    private int selfUid; // 本人在引擎中的id
    @JSONField(serialize = false)
    private boolean enableSpeakerphone = true;// 开启扬声器
    @JSONField(serialize = false)
    private boolean allRemoteAudioStreamsMute = false;// 禁其他音频流
    @JSONField(serialize = false)
    private boolean localAudioStreamMute = false; // 本地音频流禁止
    @JSONField(serialize = false)
    private boolean localVideoStreamMute = false;// 本地视频流禁止
    @JSONField(serialize = false)
    private boolean allRemoteVideoStreamsMute = false;// 拒接所有其他视频流
    @JSONField(serialize = false)
    private String mixMusicFilePath;// 伴奏路径
    @JSONField(serialize = false)
    private String midiPath;// midi打分文件路径
    @JSONField(serialize = false)
    private long mixMusicBeginOffset;// midi文件起始偏移量
    @JSONField(serialize = false)
    private int currentMusicTs;// 当前伴奏的偏移
    @JSONField(serialize = false)
    private long recordCurrentMusicTsTs;// 记录当前伴奏偏移的物理时间戳，在什么时间记录了这次偏移
    @JSONField(serialize = false)
    private boolean lrcHasStart; // 歌词是否开始
    @JSONField(serialize = false)
    private long joinRoomBeginTs; // 开始加入房间的时间
    @JSONField(serialize = false)
    private String recordingForDebugSavePath;// 保存录音的路径
    @JSONField(serialize = false)
    private boolean isAnchor; // 是否是主播
    @JSONField(serialize = false)
    private boolean grabSingNoAcc; // 一唱到底清唱模式
    @JSONField(serialize = false)
    private boolean recordingForBusi; // 是否在录制
    @JSONField(serialize = false)
    private boolean recordingForFeedback; // 是否在录制 用与用户反馈
    @JSONField(serialize = false)
    private int audioSampleRate = 44100; // 输出的音频采样率
    @JSONField(serialize = false)
    private int audioChannels = 2; // 输出的音频声道数
    @JSONField(serialize = false)
    private boolean joinChannelSuccess = false; // 加入频道成功

    private int noSticker = -1;//  贴纸文件名
    private int noFilter = -1;// 滤镜文件名
    private float intensityFilter = 0.7f;// 滤镜强度
    private float intensityThinFace = 0.36f;// 瘦脸强度
    private float intensityBigEye = 0.40f;// 大眼强度

    private float intensityMeibai = 0.63f;// 美白强度
    private float intensityMopi = 0.64f;// 磨皮强度
    private float intensityRuihua = 0.26f;// 锐化强度
    private float intensityChunse = 0.2f;// 唇色强度
    private float intensitySaihong = 0.2f;// 腮红强度

    public static Builder newBuilder(int channelProfile) {
        return new Builder().setChannelProfile(channelProfile);
    }

    public int getAudioBitrate() {
        return audioChannels == 1 ? 64 * 1000 : 128 * 1000;
    }

    public int getChannelProfile() {
        return channelProfile;
    }

    public void setChannelProfile(int channelProfile) {
        this.channelProfile = channelProfile;
    }

    public boolean isUseExternalAudio() {
        return configFromServer.useExternalAudio;
    }

    public boolean isUseExternalVideo() {
        return useExternalVideo;
    }

    public void setUseExternalVideo(boolean useExternalVideo) {
        this.useExternalVideo = useExternalVideo;
    }

    public boolean isUseExternalAudioRecord() {
        return useExternalAudioRecord;
    }

    public void setUseExternalAudioRecord(boolean useExternalAudioRecord) {
        this.useExternalAudioRecord = useExternalAudioRecord;
    }

    public boolean isUseLocalAPM() {
        return useLocalAPM;
    }

    public void setUseLocalAPM(boolean useLocalAPM) {
        this.useLocalAPM = useLocalAPM;
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

    public int getAudioMixingPlayoutVolume() {
        return audioMixingPlayoutVolume;
    }

    public void setAudioMixingPlayoutVolume(int audioMixingPlayoutVolume) {
        this.audioMixingPlayoutVolume = audioMixingPlayoutVolume;
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
        this.mixMusicFilePath = mixMusicFilePath;
    }

    public String getMixMusicFilePath() {
        return this.mixMusicFilePath;
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
        this.midiPath = midiPath;
    }

    public String getMidiPath() {
        return this.midiPath;
    }

    public void setMixMusicBeginOffset(long mixMusicBeginOffset) {
        this.mixMusicBeginOffset = mixMusicBeginOffset;
    }

    public long getMixMusicBeginOffset() {
        return this.mixMusicBeginOffset;
    }

    public void setCurrentMusicTs(int currentMusicTs) {
        this.currentMusicTs = currentMusicTs;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }


    public int getCurrentMusicTs() {
        return currentMusicTs;
    }

    public long getRecordCurrentMusicTsTs() {
        return recordCurrentMusicTsTs;
    }

    public void setRecordCurrentMusicTsTs(long recordCurrentMusicTsTs) {
        this.recordCurrentMusicTsTs = recordCurrentMusicTsTs;
    }

    public boolean isLrcHasStart() {
        return lrcHasStart;
    }

    public void setLrcHasStart(boolean lrcHasStart) {
        this.lrcHasStart = lrcHasStart;
    }

    public long getJoinRoomBeginTs() {
        return joinRoomBeginTs;
    }

    public void setJoinRoomBeginTs(long joinRoomBeginTs) {
        this.joinRoomBeginTs = joinRoomBeginTs;
    }

    public String getRecordingForDebugSavePath() {
        return recordingForDebugSavePath;
    }

    public void setRecordingForDebugSavePath(String recordingForDebugSavePath) {
        this.recordingForDebugSavePath = recordingForDebugSavePath;
    }

    public boolean isAnchor() {
        return isAnchor;
    }

    public void setAnchor(boolean anchor) {
        isAnchor = anchor;
    }

    public boolean isGrabSingNoAcc() {
        return grabSingNoAcc;
    }

    public void setGrabSingNoAcc(boolean grabSingNoAcc) {
        this.grabSingNoAcc = grabSingNoAcc;
    }

    public boolean isRecordingForBusi() {
        return recordingForBusi;
    }

    public void setRecordingForBusi(boolean recordingForBusi) {
        this.recordingForBusi = recordingForBusi;
    }

    public boolean isRecordingForFeedback() {
        return recordingForFeedback;
    }

    public void setRecordingForFeedback(boolean recordingForFeedback) {
        this.recordingForFeedback = recordingForFeedback;
    }

    public int getAudioSampleRate() {
        return audioSampleRate;
    }

    public void setAudioSampleRate(int audioSampleRate) {
        this.audioSampleRate = audioSampleRate;
    }

    public int getAudioChannels() {
        return audioChannels;
    }

    public void setAudioChannels(int audioChannels) {
        this.audioChannels = audioChannels;
    }

    public boolean isJoinChannelSuccess() {
        return joinChannelSuccess;
    }

    public void setJoinChannelSuccess(boolean joinChannelSuccess) {
        this.joinChannelSuccess = joinChannelSuccess;
    }

    public int getNoSticker() {
        return noSticker;
    }

    public void setNoSticker(int noSticker) {
        this.noSticker = noSticker;
    }

    public int getNoFilter() {
        return noFilter;
    }

    public void setNoFilter(int noFilter) {
        this.noFilter = noFilter;
    }

    public float getIntensityFilter() {
        return intensityFilter;
    }

    public void setIntensityFilter(float intensityFilter) {
        this.intensityFilter = intensityFilter;
    }

    public float getIntensityThinFace() {
        return intensityThinFace;
    }

    public void setIntensityThinFace(float intensityThinFace) {
        this.intensityThinFace = intensityThinFace;
    }

    public float getIntensityBigEye() {
        return intensityBigEye;
    }

    public void setIntensityBigEye(float intensityBigEye) {
        this.intensityBigEye = intensityBigEye;
    }

    public float getIntensityMeibai() {
        return intensityMeibai;
    }

    public void setIntensityMeibai(float intensityMeibai) {
        this.intensityMeibai = intensityMeibai;
    }

    public float getIntensityMopi() {
        return intensityMopi;
    }

    public void setIntensityMopi(float intensityMopi) {
        this.intensityMopi = intensityMopi;
    }

    public float getIntensityRuihua() {
        return intensityRuihua;
    }

    public void setIntensityRuihua(float intensityRuihua) {
        this.intensityRuihua = intensityRuihua;
    }

    public float getIntensityChunse() {
        return intensityChunse;
    }

    public void setIntensityChunse(float intensityChunse) {
        this.intensityChunse = intensityChunse;
    }

    public float getIntensitySaihong() {
        return intensitySaihong;
    }

    public void setIntensitySaihong(float intensitySaihong) {
        this.intensitySaihong = intensitySaihong;
    }

    // 工具方法，获取歌曲播放的实际时间戳
    public long getAccTs() {
        long accTs = 0;
        if (isMixMusicPlaying() && isLrcHasStart()) {
            accTs = getCurrentMusicTs() + getMixMusicBeginOffset() +
                    (System.currentTimeMillis() - getRecordCurrentMusicTsTs());
        }
        return accTs;
    }

    public int getAudioMixingPublishVolume() {
        return audioMixingPublishVolume;
    }

    public void setAudioMixingPublishVolume(int audioMixingPublishVolume) {
        this.audioMixingPublishVolume = audioMixingPublishVolume;
    }

    public boolean isEnableAudioLowLatency() {
        return configFromServer.enableAudioLowLatency;
    }


    public int getAccMixingLatencyOnSpeaker() {
        return configFromServer.accMixingLatencyOnSpeaker;
    }

    public int getAccMixingLatencyOnHeadset() {
        return configFromServer.accMixingLatencyOnHeadset;
    }

    public boolean isEnableAudioPreview() {
        return configFromServer.enableAudioPreview;
    }

    public boolean isEnableAudioPreviewLatencyTest() {
        return enableAudioPreviewLatencyTest;
    }

    public void setEnableAudioMixLatencyTest(boolean enableAudioMixLatencyTest) {
        this.enableAudioMixLatencyTest = enableAudioMixLatencyTest;
    }

    public boolean isEnableAudioMixLatencyTest() {
        return enableAudioMixLatencyTest;
    }

    public void setEnableAudioLowLatency(boolean enable) {
        configFromServer.setEnableAudioLowLatency(enable);
    }

    public void setEnableAudioPreview(boolean enable) {
        configFromServer.setEnableAudioPreview(enable);
    }

    public void setEnableAudioPreviewLatencyTest(boolean enable) {
        enableAudioPreviewLatencyTest = enable;
    }

    public void setUseExternalAudio(boolean b) {
        configFromServer.setUseExternalAudio(b);
    }

    public static class Builder {
        Params mParams = new Params();

        Builder() {
        }

        public Builder setChannelProfile(int channelProfile) {
            mParams.setChannelProfile(channelProfile);
            return this;
        }

        public Builder setUseExternalAudio(boolean useExternalAudio) {
            mParams.configFromServer.setUseExternalAudio(useExternalAudio);
            return this;
        }

        public Builder setUseExternalVideo(boolean useExternalVideo) {
            mParams.setUseExternalVideo(useExternalVideo);
            return this;
        }

        public Builder setUseExternalAudioRecord(boolean useExternalAudioRecord) {
            mParams.setUseExternalAudioRecord(useExternalAudioRecord);
            return this;
        }

        public Builder setUseLocalAPM(boolean useLocalAPM) {
            mParams.setUseLocalAPM(useLocalAPM);
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
            mParams.setAudioMixingPlayoutVolume(audioMixingVolume);
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

        public Builder setAudioSampleRate(int sampleRate) {
            mParams.setAudioSampleRate(sampleRate);
            return this;
        }

        public Builder setAudioChannels(int channels) {
            mParams.setAudioChannels(channels);
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
            MyLog.w(TAG, "save2Pref " + s);
            U.getPreferenceUtils().setSettingString("engine_pref_params4", s);
            //params.getConfigFromServer().save2Pref();
        }
    }

    /**
     * 得到偏好的引擎设置，一般是在练歌房训练出来的
     *
     * @return
     */
    public static Params getFromPref() {
        String s = U.getPreferenceUtils().getSettingString("engine_pref_params4", "");
        MyLog.w(TAG, "getFromPref " + s);
        Params params;
        if (!TextUtils.isEmpty(s)) {
            params = JSON.parseObject(s, Params.class);
        } else {
            params = Params.newBuilder(Params.CHANNEL_TYPE_LIVE_BROADCASTING)
                    .setEnableVideo(true)
                    .setEnableAudio(true)
                    .setUseExternalAudio(true)
                    .setUseExternalVideo(true)
                    .setUseExternalAudioRecord(true)
                    .setEnableInEarMonitoring(true)
                    .setStyleEnum(AudioEffect.none)
                    .build();
        }
//        params.setAudioMixingVolume(0);
        return params;
    }

    public enum Scene {
        rank, grab, voice, audiotest, doubleChat
    }

    public enum AudioEffect {
        none, liuxing, kongling, ktv, rock
    }
}
