package com.wali.live.modulechannel.util.video;

/**
 * Created by zhaomin on 17-10-30.
 */

public interface IVideoHolder {

    boolean isPlaying();

    int getVisibleVideoPercent();

    int getPostion();

    void onResume();

    void onPause();

    void pause();

    void play();

    void destroy();
}
