package com.common.base;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.common.utils.CustomHandlerThread;

/**
 * 增加后台启动函数
 */
public class InitManager {

    static CustomHandlerThread mCustomHandlerThread = new CustomHandlerThread("InitManager") {
        @Override
        protected void processMessage(Message var1) {

        }
    };

    public static void initBackground(Runnable runnable) {
        mCustomHandlerThread.post(runnable);
    }

    public static void initMainThread(Runnable runnable, long delay) {
        if (delay <= 0) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(runnable, delay);
        }
    }
}
