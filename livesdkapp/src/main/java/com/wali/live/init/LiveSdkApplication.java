package com.wali.live.init;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.base.global.GlobalData;
import com.base.log.MyLog;

/**
 * Created by lan on 16/7/20.
 *
 * @description 第三方应用可以继承WatchSdkApplication，或者直接使用InitManager
 */
public class LiveSdkApplication extends Application {
    private static final String TAG = LiveSdkApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        InitManager.init(this);
        GlobalData.initialize(this);
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            MultiDex.install(this);
        } catch (Exception e) {
            MyLog.e(e);
        }
    }

}
