package com.wali.live.video.widget.player;

import android.view.View;

import com.wali.live.video.player.presenter.IPlayerPresenter;

/**
 * Created by chenyong on 2016/10/11.
 */

public interface IPlayerView {

    void setVisibility(int visibility);

    int getVisibility();

    void setTranslationY(float translationY);

    void setOnTouchListener(View.OnTouchListener l);

    IPlayerPresenter getPlayerPresenter();

    int getRotateBtnBottomMargin();

    void onResume();

    void onPause();

    void onDestroy();

    void onConfigurationChanged();

    void setVideoTransMode(int mode);
}
