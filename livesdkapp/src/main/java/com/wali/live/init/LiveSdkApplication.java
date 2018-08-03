package com.wali.live.init;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDex;

import com.base.log.MyLog;
import com.base.utils.Constants;
import com.mi.liveassistant.BuildConfig;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.wali.live.watchsdk.init.InitManager;
import com.wali.live.watchsdk.statistics.AliveRegularUploadTask;

/**
 * Created by lan on 16/7/20.
 *
 * @description 第三方应用可以继承WatchSdkApplication，或者直接使用InitManager
 */
public class LiveSdkApplication extends Application {
    private static final String TAG = LiveSdkApplication.class.getSimpleName();

    private static RefWatcher sRefWatcher;

    private int mActivityForegrounCount; //onStart +1 onStop -1 0为应用退到后台

    @Override
    public void onCreate() {
        MyLog.w(TAG, "onCreate");
        super.onCreate();
        initializeLeakDetection();
        InitManager.init(this, sRefWatcher);
        registerActivityLifecycle();
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            MultiDex.install(this);
        } catch (Exception e) {
            MyLog.e(e);
        }
    }

    protected void initializeLeakDetection() {
        if (BuildConfig.DEBUG && !Constants.isDebugMiChanel) {
            sRefWatcher = LeakCanary.install(this);
        }
    }

    public static RefWatcher getRefWatcher() {
        return sRefWatcher;
    }

    private void registerActivityLifecycle() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (mActivityForegrounCount == 0){
                    // 第一次出现在前台　开始活跃打点记时
                    AliveRegularUploadTask.sInstance.startUpload();
                }
                mActivityForegrounCount++;
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                mActivityForegrounCount--;
                if (mActivityForegrounCount == 0){
                   // 停止活跃打点记时
                    AliveRegularUploadTask.sInstance.stopUpload();
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }
}
