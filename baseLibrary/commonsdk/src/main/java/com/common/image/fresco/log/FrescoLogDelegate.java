package com.common.image.fresco.log;

import android.util.Log;

import com.common.log.MyLog;
import com.facebook.common.logging.LoggingDelegate;

/**
 * Created by anping on 16-9-5.
 */
public class FrescoLogDelegate implements LoggingDelegate {

    private int mMinimumLoggingLevel = Log.ERROR;

    private boolean sSwitch = true;

    private String TAG;

    public FrescoLogDelegate(String tag) {
        this.TAG = tag;
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
            MyLog.v(TAG, " " + tag + " : " + msg);
        }
    }

    @Override
    public void v(String tag, String msg, Throwable tr) {
        if (sSwitch) {
            MyLog.w(TAG, " " + tag + " : " + msg, tr);
        }
    }

    @Override
    public void d(String tag, String msg) {
        if (sSwitch) {
            MyLog.d(TAG, " " + tag + " : " + msg);
        }
    }

    @Override
    public void d(String tag, String msg, Throwable tr) {
        if (sSwitch) {
            MyLog.d(TAG, " " + tag + " : " + msg);
        }
    }

    @Override
    public void i(String tag, String msg) {
        if (sSwitch) {
            MyLog.i(TAG, " " + tag + " : " + msg);
        }
    }

    @Override
    public void i(String tag, String msg, Throwable tr) {
        if (sSwitch) {
            MyLog.i(TAG, tr);
        }
    }

    @Override
    public void w(String tag, String msg) {
        if (sSwitch) {
            MyLog.w(TAG, " " + tag + " : " + msg);
        }
    }

    @Override
    public void w(String tag, String msg, Throwable tr) {
        if (sSwitch) {
            MyLog.w(TAG, " " + tag + " : " + msg, tr);
        }
    }

    @Override
    public void e(String tag, String msg) {
        if (sSwitch) {
            MyLog.e(TAG, " " + tag + " : " + msg);
        }
    }

    @Override
    public void e(String tag, String msg, Throwable tr) {
        if (sSwitch) {
            MyLog.e(TAG, " " + tag + " : " + msg, tr);
        }
    }

    @Override
    public void wtf(String tag, String msg) {
        if (sSwitch) {
            MyLog.w(TAG, " " + tag + " : " + msg);
        }
    }

    @Override
    public void wtf(String tag, String msg, Throwable tr) {
        if (sSwitch) {
            MyLog.w(TAG, " " + tag + " : " + msg, tr);
        }
    }

    @Override
    public void log(int priority, String tag, String msg) {
        if (sSwitch) {
            MyLog.d(TAG, " " + tag + " : " + msg);
        }
    }
}
