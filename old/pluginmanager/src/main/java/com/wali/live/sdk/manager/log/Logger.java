package com.wali.live.sdk.manager.log;

import android.util.Log;

/**
 * Created by lan on 17/2/21.
 */
public class Logger {
    private static boolean sEnabled = false;

    private Logger() {
    }

    public static void setEnabled(boolean enabled) {
        sEnabled = enabled;
    }

    public static void v(String tag, String msg) {
        if (sEnabled) {
            Log.v(tag, msg);
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (sEnabled) {
            Log.v(tag, msg, tr);
        }
    }

    public static void d(String tag, String msg) {
        if (sEnabled) {
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (sEnabled) {
            Log.d(tag, msg, tr);
        }
    }

    public static void i(String tag, String msg) {
        if (sEnabled) {
            Log.i(tag, msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (sEnabled) {
            Log.i(tag, msg, tr);
        }
    }

    public static void w(String tag, String msg) {
        if (sEnabled) {
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (sEnabled) {
            Log.w(tag, msg, tr);
        }
    }

    public static void w(String tag, Throwable tr) {
        if (sEnabled) {
            Log.w(tag, tr);
        }
    }

    public static void e(String tag, String msg) {
        if (sEnabled) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (sEnabled) {
            Log.e(tag, msg, tr);
        }
    }
}
