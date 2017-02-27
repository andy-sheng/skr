package com.mi.live.engine.player.widget;

import android.view.SurfaceHolder;

/**
 * Created by linjinbin on 16/7/7.
 */
public interface IVideoView {
    void setVideoLayout();

    void adjustVideoLayout(boolean isLandscape);

    void onSetVideoURICompleted();

    SurfaceHolder getSurfaceHolder();
}
