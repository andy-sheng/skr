package com.base.log;

import android.util.Log;

import com.base.log.logger.LogAdapter;
import com.mi.milink.sdk.base.debug.TraceLevel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by linjinbin on 2016/11/10.
 */

public class CustomLogAdapter implements LogAdapter {

    static Logger log = LoggerFactory.getLogger(CustomLogAdapter.class);

//    static CustomHandlerThread sHandlerThread = new CustomHandlerThread("CustomLogAdapter") {
//
//        @Override
//        protected void processMessage(Message message) {
//
//        }
//    };

    @Override
    public void d(final String tag, final String message) {
        if ((MyLog.getCurrentLogLevel() & TraceLevel.DEBUG) > 0) {
            log.debug(message);
            Log.d(tag, message);
        }
    }

    @Override
    public void e(final String tag, final String message) {
        if ((MyLog.getCurrentLogLevel() & TraceLevel.ERROR) > 0) {

            log.error(message);
            Log.e(tag, message);
        }
    }

    @Override
    public void w(final String tag, final String message) {
        if ((MyLog.getCurrentLogLevel() & TraceLevel.WARN) > 0) {

            log.warn(message);
            Log.w(tag, message);
        }
    }

    @Override
    public void i(final String tag, final String message) {
        if ((MyLog.getCurrentLogLevel() & TraceLevel.INFO) > 0) {

            log.info(message);
            Log.i(tag, message);
        }
    }

    @Override
    public void v(final String tag, final String message) {
        if ((MyLog.getCurrentLogLevel() & TraceLevel.VERBOSE) > 0) {

            log.debug(message);
            Log.d(tag, message);
        }
    }

    @Override
    public void wtf(final String tag, final String message) {
        if ((MyLog.getCurrentLogLevel() & TraceLevel.WARN) > 0) {

            log.warn(message);
            Log.w(tag, message);
        }
    }
}
