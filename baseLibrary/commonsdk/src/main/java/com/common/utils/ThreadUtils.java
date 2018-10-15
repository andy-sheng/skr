package com.common.utils;

import android.os.Looper;

/**
 * 通过U.getThreadUtils获得
 */
public class ThreadUtils {

    ThreadUtils() {
    }

    public void ensureUiThread() {
        if (!isUiThread()) {
            throw new IllegalStateException("ensureUiThread: thread check failed");
        }
    }

    public void ensureNotUiThread() {
        if (isUiThread()) {
            throw new IllegalStateException("ensureNonUiThread: thread check failed");
        }
    }

    public boolean isUiThread() {
        final Looper myLooper = Looper.myLooper();
        final Looper mainLooper = Looper.getMainLooper(); // never null

        return mainLooper.equals(myLooper);
    }
}

