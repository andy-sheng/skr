package com.base.image.fresco.log;

import android.util.Log;

import com.base.common.BuildConfig;
import com.facebook.common.logging.LoggingDelegate;
import com.base.log.MyLog;

/**
 * Created by anping on 16-9-5.
 */
public class LiveFrescoDelegate implements LoggingDelegate {

    public static final LiveFrescoDelegate sInstance = new LiveFrescoDelegate();

    private static final String TAG = "fresco";

    private String mApplicationTag = "unknown";

    private int mMinimumLoggingLevel = Log.WARN;

    private static boolean sSwitch = BuildConfig.DEBUG;

    public static LiveFrescoDelegate getInstance() {
        return sInstance;
    }

    private LiveFrescoDelegate() {
    }

    @Override
    public void setMinimumLoggingLevel(int level) {
        mMinimumLoggingLevel = level;
    }

    @Override
    public int getMinimumLoggingLevel() {
        return mMinimumLoggingLevel;
    }

    @Override
    public boolean isLoggable(int level) {
        return mMinimumLoggingLevel <= level;
    }

    @Override
    public void v(String tag, String msg) {
        if (sSwitch) {
            MyLog.v(TAG + " " + tag + " : " + msg);
        }
    }

    @Override
    public void v(String tag, String msg, Throwable tr) {
        if (sSwitch) {
            MyLog.v(TAG + " " + tag + " : " + msg, tr);
        }
    }

    @Override
    public void d(String tag, String msg) {
        if (sSwitch) {
            MyLog.d(TAG + " " + tag + " : " + msg);
        }
    }

    @Override
    public void d(String tag, String msg, Throwable tr) {
        if (sSwitch) {
            MyLog.d(TAG + " " + tag + " : " + msg);
        }
    }

    @Override
    public void i(String tag, String msg) {
        if (sSwitch) {
            MyLog.i(TAG + " " + tag + " : " + msg);
        }
    }

    @Override
    public void i(String tag, String msg, Throwable tr) {
        if (sSwitch) {
            MyLog.i(TAG + " " + tag + " : " + msg, tr);
        }
    }

    @Override
    public void w(String tag, String msg) {
        if (sSwitch) {
            MyLog.w(TAG + " " + tag + " : " + msg);
        }
    }

    @Override
    public void w(String tag, String msg, Throwable tr) {
        if (sSwitch) {
            MyLog.w(TAG + " " + tag + " : " + msg, tr);
        }
    }

    @Override
    public void e(String tag, String msg) {
        if (sSwitch) {
            MyLog.e(TAG + " " + tag + " : " + msg);
        }
    }

    @Override
    public void e(String tag, String msg, Throwable tr) {
        if (sSwitch) {
            MyLog.e(TAG + " " + tag + " : " + msg, tr);
        }
    }

    @Override
    public void wtf(String tag, String msg) {
        if (sSwitch) {
            MyLog.w(TAG + " " + tag + " : " + msg);
        }
    }

    @Override
    public void wtf(String tag, String msg, Throwable tr) {
        if (sSwitch) {
            MyLog.w(TAG + " " + tag + " : " + msg, tr);
        }
    }

    @Override
    public void log(int priority, String tag, String msg) {
        if (sSwitch) {
            MyLog.d(TAG + " " + tag + " : " + msg);
        }
    }
}
