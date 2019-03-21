package com.common.floatwindow;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by yhao on 17-12-1.
 * 用于控制悬浮窗显示周期
 * 使用了三种方法针对返回桌面时隐藏悬浮按钮
 * 1.startCount计数，针对back到桌面可以及时隐藏
 * 2.监听home键，从而及时隐藏
 * 3.resumeCount计时，针对一些只执行onPause不执行onStop的奇葩情况
 */

class FloatLifecycle extends BroadcastReceiver implements Application.ActivityLifecycleCallbacks {

    private Class[] activities;
    private boolean showFlag;
    private LifecycleListener mLifecycleListener;


    FloatLifecycle(Context applicationContext, boolean showFlag, Class[] activities, LifecycleListener lifecycleListener) {
        this.showFlag = showFlag;
        this.activities = activities;
        mLifecycleListener = lifecycleListener;
        ((Application) applicationContext).registerActivityLifecycleCallbacks(this);
    }

    public boolean needShow(Activity activity) {
        if (activities == null) {
            return true;
        }
        for (Class a : activities) {
            if (a.isInstance(activity)) {
                return showFlag;
            }
        }
        return !showFlag;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (needShow(activity)) {
            mLifecycleListener.onShow("onActivityResumed");
        } else {
            mLifecycleListener.onHide("onActivityResumed");
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }


    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
