package com.wali.live.video.player;

import android.view.SurfaceHolder;

/**
 * Created by linjinbin on 16/7/7.
 */
public interface IVideoView {
    void setVideoLayout(boolean isLandscape);

    void adjustVideoLayout(boolean isLandscape);

    void onSetVideoURICompleted();

    SurfaceHolder getSurfaceHolder();
}
