package com.wali.live.sdk.litedemo.base.application;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.mi.liveassistant.init.InitManager;

import java.util.List;

/**
 * Created by lan on 16/1/17.
 */
public class BaseApplication extends Application {
    private static final String TAG = BaseApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        int pid = android.os.Process.myPid();
        Log.d(TAG, "onCreate = " + pid);

        List<RunningAppProcessInfo> processInfoList =
                ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
        boolean isCoreProcess = false;
        for (RunningAppProcessInfo info : processInfoList) {
            if (getPackageName().equals(info.processName) && info.pid == pid) {
                isCoreProcess = true;
                break;
            }
        }

        if (isCoreProcess) {
            initialize();
        }
        InitManager.init(this);
    }

    private void initialize() {
        Fresco.initialize(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            MultiDex.install(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
