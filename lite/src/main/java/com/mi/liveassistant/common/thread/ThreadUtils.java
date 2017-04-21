package com.mi.liveassistant.common.thread;

import android.os.Looper;

/**
 * Created by linjinbin on 15/2/10.
 */
public class ThreadUtils {
    public static void ensureUiThread() {
        if (!ThreadUtils.isUiThread()) {
            throw new IllegalStateException("ensureUiThread: thread check failed");
        }
    }

    public static void ensureNonUiThread() {
        if (ThreadUtils.isUiThread()) {
            throw new IllegalStateException("ensureNonUiThread: thread check failed");
        }
    }

    public static boolean isUiThread() {
        final Looper myLooper = Looper.myLooper();
        final Looper mainLooper = Looper.getMainLooper(); // never null

        return mainLooper.equals(myLooper);
    }
}

