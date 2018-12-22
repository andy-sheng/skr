package com.common.log;

import android.os.Process;

import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.flattener.DefaultFlattener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by linjinbin on 2018/3/20.
 */

public class MyFlattener extends DefaultFlattener {

    //Convert current time to String using specified format
    String format = "yyyy-MM-dd HH:mm:ss.SSS";
    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(format, Locale.US);

    @Override
    public CharSequence flatten(int logLevel, String tag, String message) {
        Date now = new Date();
        String formattedNow = mSimpleDateFormat.format(now);
        return formattedNow
                + '|' + Process.myPid()
                + '|' + Thread.currentThread().getId()
                + '|' + LogLevel.getShortLevelName(logLevel)
                + '|' + tag
                + "| " + message;
    }

    public void setShowyyyyMMdd(boolean b) {
        if (b) {
            String format = "yyyy-MM-dd HH:mm:ss.SSS";
            mSimpleDateFormat = new SimpleDateFormat(format, Locale.US);
        } else {
            String format = "HH:mm:ss.S";
            mSimpleDateFormat = new SimpleDateFormat(format, Locale.US);
        }
    }
}
