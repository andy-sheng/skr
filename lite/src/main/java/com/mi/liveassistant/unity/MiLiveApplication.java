package com.mi.liveassistant.unity;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.support.annotation.Keep;
import android.util.Log;

import com.mi.liveassistant.common.global.GlobalData;
import com.mi.liveassistant.init.InitManager;

import java.util.List;

/**
 * Created by yangli on 2017/5/2.
 *
 * @module Unity直播Application类
 */
@Keep
public class MiLiveApplication extends Application {
    private static final String TAG = "MiLiveApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        GlobalData.setApplication(this);
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
        InitManager.init(this);
    }
}
