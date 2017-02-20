package com.mi.live.engine.streamer;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;

import com.xiaomi.broadcaster.dataStruct.ConnectedServerInfo;
import com.xiaomi.broadcaster.dataStruct.RtmpServerInfo;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by chenyong on 16/4/9.
 */
public interface IStreamer {

    String OPTIMAL_MUSIC_IN_SPEAKER_MODE = "optimal_music_in_speaker_mode";
    String OPTIMAL_VOICE_IN_SPEAKER_MODE = "optimal_voice_in_speaker_mode";
    String OPTIMAL_ONLY_VOICE_IN_SPEAKER_MODE = "optimal_only_voice_in_speaker_mode";
    String OPTIMAL_MUSIC_IN_HEADSET_MODE = "optimal_music_in_headset_mode";
    String OPTIMAL_VOICE_IN_HEADSET_MODE = "optimal_voice_in_headset_mode";
    String OPTIMAL_FEEDBACK_IN_HEADSET_MODE = "optimal_feedback_in_headset_mode";
    String OPTIMAL_INTRINSIC_MUSIC_VOICE_DELAY = "optimal_intrinsic_music_voice_delay";

    void setConfig(StreamerConfig config);

    void setDisplayPreview(View surfaceView);

    void updateUrl(String url);

    void startStream(List<String> ipPortList);

    void startStreamEx(@NonNull RtmpServerInfo[] rtmpServerInfos);

    void stopStream();

    void switchCamera();

    void resume();

    void pause();

    void pauseImmediately();

    void destroy();

    boolean toggleTorch(boolean open);

    boolean startMusic(String path, OnProgressListener listener);

    boolean pauseMusic();

    boolean resumeMusic();

    boolean stopMusic();

    boolean playAtmosphereMusic(String path);

    void setOptimalDefaultParams(JSONObject params);

    void setHeadsetPlugged(boolean isPlugged);

    void setBeautyLevel(int beautyLevel);

    void setMuteAudio(boolean isMute);

    void setMusicVolume(float volume);

    void setVoiceVolume(float volume);

    void setMusicVoiceDeviate(float deviate);

    void setReverbLevel(int level);

    void setMirrorMode(boolean isMirror);

    String getRtmpHostIP();

    ConnectedServerInfo getConnectedServerInfo();

    StreamerConfig getConfig();

    boolean setFocus(float x, float y, float w, float h);

    void startMixVideo(long liveLineId, float x, float y, float width, float height, boolean isSelfLarge);

    void stopMixVideo();

    void startAddExtra(long streamId, float leftX, float leftY, float scaleWidth, float scaleHeight, float displayWidth, float displayHeight, int layer);

    void stopAddExtra(long streamId);

    void putExtraDetailInfo(int width, int height, byte[] data, int stride, int videoType, int frameType, long streamId);

    void putExtraDetailInfoWithTimestamp(int width, int height, byte[] data, int stride, int videoType, int frameType, long streamId, long timestamp);

    void startExtraVideo(long streamId, long audioSource);

    void stopExtraVideo(long streamId);

    boolean startRtmpServerImmediately(int port, String name);

    void stopRtmpServer();

    void removeVideoView(RelativeLayout layout, String uid);

    void showVideoView(RelativeLayout layout, String uid, boolean isTop, boolean isForce);

    void switchRenderWithUid(String uid1, String uid2);

    void setAngle(int deviceAngle, int uiAngle);

    void setAudioType(int type);

    void muteMic();

    void unMuteMic();

    void setIPAudioSource(float volume);

    void handleZoom(double zoomFactor);

    void putExtraAudioFrame(int nSamples, int nBytesPerSample, int nChannels, int samplesPerSec, byte[] data);

    void putExtraAudioFrameWithTimestamp(int nSamples, int nBytesPerSample, int nChannels, int samplesPerSec, byte[] data, long timestamp);

    void loopbackAudio(boolean enable);

    void setVideoFilter(String filter);

    void setVideoFilterIntensity(float filterIntensity);

    void muteIPAudioSource(long streamId);

    void hidePreview();

    void setUpOutputFrameResolution(int width, int height);

    void setVideoMainStream(long streamId, boolean isMainStream);

    void addVideoPreprocessFilter(long filter);

    void removeVideoPreprocessFilter(long filter);

    void setStickerPath(String path);

    void setClientPublicIp(String clientPublicIp);

    long getIPCameraVideoSourceOberver();
}
