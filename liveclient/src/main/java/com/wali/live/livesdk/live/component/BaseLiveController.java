package com.wali.live.livesdk.live.component;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.base.fragment.FragmentDataListener;
import com.mi.live.engine.streamer.IStreamer;
import com.wali.live.component.BaseSdkView;
import com.wali.live.component.ComponentController;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 直播组件控制器基类
 */
public abstract class BaseLiveController extends ComponentController {

    /**
     * 进入准备页
     */
    public abstract void enterPreparePage(
            BaseComponentSdkActivity fragmentActivity,
            int requestCode,
            FragmentDataListener listener);

    /**
     * 创建Streamer
     */
    public abstract IStreamer createStreamer(View surfaceView, int clarity, Intent intent);

    /**
     * 创建SdkView
     */
    public abstract BaseSdkView createSdkView(Activity activity);

    public void onStartLive() {
    }

    public void onStopLive() {
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
