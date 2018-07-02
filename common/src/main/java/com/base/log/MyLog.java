package com.base.log;


import android.text.TextUtils;

import com.base.log.logger.Logger;
import com.mi.milink.sdk.base.debug.TraceLevel;
import com.mi.milink.sdk.client.ipc.ClientLog;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by MK on 15-3-2.
 */
public class MyLog {
    private final static long KEEP_LOG_PERIOD = 4 * 24 * 60 * 60 * 1000;//4天

    private final static HashMap<Integer, Long> sStartTimes = new HashMap<>();

    private final static HashMap<Integer, String> sActionNames = new HashMap<>();
    private static java.lang.String LOGTAG = "MI_LIVE";

    private static AtomicInteger sCodeGenerator = new AtomicInteger(1);

    private static int sCurrentLogLevel = TraceLevel.ERROR;             //当前的日志级别
    private static int sCurrentFileLogLevel = TraceLevel.ERROR;             //当前的日志级别
    // ------------------------------------------------------------------------------
    // 日志打印方法
    // ------------------------------------------------------------------------------
    public static final void v(String msg) {
        Logger.v(msg);
    }

    public static final void v(String msg, Throwable tr) {
        Logger.e(tr, msg);
    }

    public static final void d(String msg) {
        try {
            Logger.d(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void d(String msg, Throwable tr) {
        try {
            Logger.e(tr, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void i(String msg) {
        try {
            Logger.i(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void i(String msg, Throwable tr) {
        try {
            Logger.e(tr, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void w(String msg) {
        try {
            Logger.w(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void i(String tag, String msg) {
        Logger.i(tag + ": " + msg);
    }

    public static final void v(String tag, String msg) {
        Logger.v(tag + ": " + msg);
    }

    public static final void d(String tag, String msg) {
        Logger.d(tag + ": " + msg);
    }

    public static final void w(String tag, String msg) {
        Logger.w(tag + ": " + msg);
    }

    public static final void w(String msg, Throwable tr) {
        Logger.e(tr, msg);
    }

    public static final void e(String tag, String msg) {
        Logger.e(tag + ": " + msg);
    }

    public static final void e(String msg) {
        Logger.e(msg);
    }

    public static final void e(String msg, Throwable tr) {
        Logger.e(tr, msg);
    }

    public static final void e(String tag, String msg, Throwable tr) {
        Logger.e(tr, tag + ": " + msg);
    }

    public static final void d(String tag, String msg, Throwable tr) {
        Logger.e(tr, tag + ": " + msg);
    }

    public static final void w(String tag, String msg, Throwable tr) {
        Logger.e(tr, tag + ": " + msg);
    }

    public static final void e(Throwable tr) {
        Logger.e(tr, "");
    }

    /**
     * 设置日志级别
     *
     * @param level
     */
    public static void setLogcatTraceLevel(int level,int fileLogLevel, String logTag) {
        if (level > TraceLevel.ALL || level < 0) {
            level = TraceLevel.ALL;
        }
        if (!TextUtils.isEmpty(logTag)) {
            LOGTAG = logTag;
        }
        Logger.init(LOGTAG)
                .methodCount(1)
                .methodOffset(1)
                .hideThreadInfo()
                .logAdapter(new CustomLogAdapter())
                .singleMode();
        sCurrentLogLevel = level;
        sCurrentFileLogLevel = fileLogLevel;
        ClientLog.setLogcatTraceLevel(level);
    }

    /**
     * 得到当前的日志级别
     *
     * @return
     */
    public static int getCurrentLogLevel() {
        return sCurrentLogLevel;
    }

    /**
     * 得到当前的文件日志级别
     *
     * @return
     */
    public static int getCurrentFileLogLevel() {
        return sCurrentFileLogLevel;
    }
    // ------------------------------------------------------------------------------
    // 性能统计
    // ------------------------------------------------------------------------------
    public static final Integer ps(String action) {
        Integer code = Integer.valueOf(sCodeGenerator.incrementAndGet());
        sStartTimes.put(code, System.currentTimeMillis());
        sActionNames.put(code, action);
        w(action + " starts");
        return code;
    }

    public static void pe(Integer code) {
        if (!sStartTimes.containsKey(code)) {
            return;
        }
        long startTime = sStartTimes.remove(code);
        String action = sActionNames.remove(code);
        long time = System.currentTimeMillis() - startTime;
        w(action + " ends in " + time + " ms");
    }

}
