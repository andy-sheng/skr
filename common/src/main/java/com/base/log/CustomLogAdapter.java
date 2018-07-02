package com.base.log;

import android.util.Log;

import com.base.common.BuildConfig;
import com.base.log.logger.LogAdapter;
import com.mi.milink.sdk.base.debug.TraceLevel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by linjinbin on 2016/11/10.
 */

public class CustomLogAdapter implements LogAdapter {

    static Logger log = LoggerFactory.getLogger(CustomLogAdapter.class);

    @Override
    public void d(final String tag, final String message) {
        if ((MyLog.getCurrentLogLevel() & TraceLevel.DEBUG) > 0) {
            Log.d(tag, message);
        }
        if ((MyLog.getCurrentFileLogLevel() & TraceLevel.DEBUG) > 0) {
            log.debug(message);
        }
    }

    @Override
    public void e(final String tag, final String message) {
        if ((MyLog.getCurrentLogLevel() & TraceLevel.ERROR) > 0) {
            Log.e(tag, message);
        }
        if ((MyLog.getCurrentFileLogLevel() & TraceLevel.ERROR) > 0) {
            log.error(message);
        }
    }

    @Override
    public void w(final String tag, final String message) {
        if ((MyLog.getCurrentLogLevel() & TraceLevel.WARN) > 0) {
            Log.w(tag, message);
        }
        if ((MyLog.getCurrentFileLogLevel() & TraceLevel.WARN) > 0) {
            log.warn(message);
        }
    }

    @Override
    public void i(final String tag, final String message) {
        if ((MyLog.getCurrentLogLevel() & TraceLevel.INFO) > 0) {
            Log.i(tag, message);
        }
        if ((MyLog.getCurrentFileLogLevel() & TraceLevel.INFO) > 0) {
            log.info(message);
        }
    }

    @Override
    public void v(final String tag, final String message) {
        if ((MyLog.getCurrentLogLevel() & TraceLevel.VERBOSE) > 0) {
            Log.v(tag, message);
        }
        if ((MyLog.getCurrentFileLogLevel() & TraceLevel.VERBOSE) > 0) {
            log.debug(message);
        }
    }

    @Override
    public void wtf(final String tag, final String message) {
        if ((MyLog.getCurrentLogLevel() & TraceLevel.WARN) > 0) {
            Log.w(tag, message);
        }
        if ((MyLog.getCurrentFileLogLevel() & TraceLevel.WARN) > 0) {
            log.warn(message);
        }
    }
}
