package com.wali.live.sdk.litedemo.base.application;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;

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
    }

    private void initialize() {
        Fresco.initialize(this);
    }
}
