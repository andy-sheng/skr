package com.mi.live.engine.player.widget;

import android.view.ViewGroup;

/**
 * Created by chenyong on 2016/11/16.
 */
public interface IPlayerTextureView {

    IPlayerPresenter getVideoPlayerPresenter();

    void setVideoTransMode(int mode);

    ViewGroup.LayoutParams getLayoutParams();

    void setLayoutParams(ViewGroup.LayoutParams params);
}
