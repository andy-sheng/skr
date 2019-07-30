package com.common.player;

import android.view.Surface;

/**
 * Created by yangli on 2017/9/20.
 */
public interface IPlayer {


    int GRAVITY_FIT_WITH_CROPPING = 2;
    int GRAVITY_FIT = 1;

    int MEDIA_INFO_UNKNOWN = 1;
    int MEDIA_INFO_BUFFERING_START = 1000;
    int MEDIA_INFO_BUFFERING_END = 1001;
    int MEDIA_ERROR_CONNECT_SERVER_FAILED = -2000;

    void setCallback(IPlayerCallback callback);

    int getVideoWidth();

    int getVideoHeight();

    long getDuration();

    long getCurrentPosition();

    boolean isPlaying();

    /**
     *
     * @param view
     * @param gravity
     * @param width
     * @param height
     */
    void setGravity(Object view, int gravity, int width, int height);

    void shiftUp(float ratio, float min_layer_ratio, float max_layer_ratio, float mix_frame_ratio, float max_frame_ratio);

    void setMuteAudio(boolean isMute);

    void setVolume(float volume);

    void setVolume(float volume,boolean set);

    void setSurface(Surface surface);

    boolean startPlay(String path); //true 是重头播，false 为继续播

    void startPlayPcm(String path, int channels, int sampleRate, int byteRate);
//    void prepare(boolean realTime);

//    void start();

    void pause();

    void resume();

    void stop();

    void reset();

    void release();

    void seekTo(long msec);

    void reconnect();

    float getVolume();

    void setDecreaseVolumeEnd(boolean b);

    /**
     * 是否监听进度
     * @param b
     */
//    void setMonitorProgress(boolean b);
}
