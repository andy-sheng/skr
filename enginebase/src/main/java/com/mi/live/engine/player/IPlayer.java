package com.mi.live.engine.player;

import android.view.Surface;
import android.view.View;

import com.xiaomi.player.Player;

import java.util.List;

/**
 * Created by yangli on 2017/9/20.
 */
public interface IPlayer {

    int MEDIA_INFO_UNKNOWN = 1;
    int MEDIA_INFO_BUFFERING_START = 1000;
    int MEDIA_INFO_BUFFERING_END = 1001;

    int MEDIA_ERROR_CONNECT_SERVER_FAILED = -2000;

    void setCallback(IPlayerCallback callback);

    int getVideoWidth();

    int getVideoHeight();

    long getDuration();

    long getCurrentPosition();

    long getCurrentStreamPosition();

    long getCurrentAudioTimestamp();

    void setSpeedUpThreshold(long threshold);

    boolean isPlaying();

    void setBufferTimeMax(float timeInSecond);

    void setGravity(View view, Player.SurfaceGravity gravity, int width, int height);

    void shiftUp(float ratio, float min_layer_ratio, float max_layer_ratio, float mix_frame_ratio, float max_frame_ratio);

    void setScreenOnWhilePlaying(boolean screenOn);

    void setMuteAudio(boolean isMute);

    void setVolume(float volumeL, float volumeR);

    void setSurface(Surface surface);

    void setVideoPath(String path, String host);

    void prepare(boolean realTime);

    void start();

    void pause();

    void resume();

    void stop();

    void reset();

    void release();

    void seekTo(long msec);

    void reconnect();

    void setIpList(List<String> httpIpList, List<String> localIpList);

}
