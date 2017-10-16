package com.wali.live.init;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.base.log.MyLog;
import com.base.utils.Constants;
import com.mi.liveassistant.BuildConfig;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.wali.live.watchsdk.init.InitManager;

/**
 * Created by lan on 16/7/20.
 *
 * @description 第三方应用可以继承WatchSdkApplication，或者直接使用InitManager
 */
public class LiveSdkApplication extends Application {
    private static final String TAG = LiveSdkApplication.class.getSimpleName();

    private static RefWatcher sRefWatcher;

    @Override
    public void onCreate() {
        MyLog.w(TAG, "onCreate");
        super.onCreate();
        initializeLeakDetection();
        InitManager.init(this, sRefWatcher);
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
}
