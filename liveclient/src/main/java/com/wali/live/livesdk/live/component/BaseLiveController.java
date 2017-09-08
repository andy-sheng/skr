package com.wali.live.livesdk.live.component;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.base.activity.BaseSdkActivity;
import com.base.fragment.FragmentDataListener;
import com.wali.live.component.BaseSdkController;
import com.wali.live.component.BaseSdkView;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 直播组件控制器基类
 */
public abstract class BaseLiveController extends BaseSdkController {

    /**
     * 创建Streamer
     */
    public abstract void createStreamer(BaseSdkActivity activity, View surfaceView, int clarity, boolean isMute, Intent intent);

    public void onStartLive() {
    }

    public void onStopLive(boolean wasKicked) {
    }

    public void onResumeStream() {
    }

    public void onPauseStream() {
    }

    public void onActivityResumed() {
    }

    public void onActivityPaused() {
    }

    public void onActivityStopped() {
    }

}
