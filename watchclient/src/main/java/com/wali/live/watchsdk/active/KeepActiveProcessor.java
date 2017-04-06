package com.wali.live.watchsdk.active;

import android.os.Process;

import com.base.log.MyLog;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lan on 17/3/30.
 */
public class KeepActiveProcessor {
    private static final String TAG = KeepActiveProcessor.class.getSimpleName();

    private static final int ACTIVE_TYPE = 0x800;

    private static final int STATUS_RUNNING = 1;
    private static final int STATUS_STOP = 2;

    private static ExecutorService sExecutorService = (ExecutorService) Executors.newSingleThreadExecutor();

    public static void keepActive() {
        sExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Class clazz = Class.forName("com.miui.whetstone.WhetstoneManager");
                    Method method = clazz.getDeclaredMethod("updateActiveProcessStatus", int.class, int.class, int.class, int.class);
                    method.invoke(clazz, Process.myUid(), Process.myPid(), ACTIVE_TYPE, STATUS_RUNNING);
                    MyLog.d(TAG, "keepActive success, type=" + Integer.toHexString(ACTIVE_TYPE));
                } catch (Exception e) {
                    MyLog.e(TAG, e);
                }
            }
        });
    }

    public static void stopActive() {
        sExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Class clazz = Class.forName("com.miui.whetstone.WhetstoneManager");
                    Method method = clazz.getDeclaredMethod("updateActiveProcessStatus", int.class, int.class, int.class, int.class);
                    method.invoke(clazz, Process.myUid(), Process.myPid(), ACTIVE_TYPE, STATUS_STOP);
                    MyLog.d(TAG, "stopActive success, type=" + Integer.toHexString(ACTIVE_TYPE));
                } catch (Exception e) {
                    MyLog.e(TAG, e);
                }
            }
        });
    }
}
