package com.wali.live.watchsdk.videodetail.data.engine;

import android.view.Surface;

import com.xiaomi.player.Player;

import java.util.List;

/**
 * Created by yangli on 2017/9/20.
 */
public interface IPlayer {

    int getVideoWidth();

    int getVideoHeight();

    long getDuration();

    long getCurrentPosition();

    long getCurrentStreamPosition();

    boolean isPlaying();

    void setBufferTimeMax(float timeInSecond);

    void setGravity(Player.SurfaceGravity gravity, int width, int height);

    void shiftUp(float ratio);

    void setScreenOnWhilePlaying(boolean screenOn);

    void setMuteAudio(boolean isMute);

    void setSurface(Surface surface);

    void setVideoPath(String path, String host);

    void prepare(boolean realTime);

    void start();

    void pause();

    void stop();

    void reset();

    void release();

    void seekTo(long msec);

    void reconnect();

    void setIpList(List<String> httpIpList, List<String> localIpList);

}
