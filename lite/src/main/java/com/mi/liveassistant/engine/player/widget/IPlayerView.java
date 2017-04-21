package com.mi.liveassistant.engine.player.widget;

import android.view.View;

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

    void onPause();

    void onResume();

    void onDestroy();

    void onConfigurationChanged();
}
