package com.common.log;

import com.common.base.BuildConfig;
import com.common.utils.U;
import com.tencent.mars.xlog.Log;
import com.tencent.mars.xlog.Xlog;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MyLog {
    private static AtomicInteger sCodeGenerator = new AtomicInteger(1);
    private final static HashMap<Integer, Long> sStartTimes = new HashMap<>();
    private final static HashMap<Integer, String> sActionNames = new HashMap<>();

    static boolean sHasInit = false;
    static boolean sForceOpenFlag = false;

    public final static String TAG = "SKRER";

    public static void init() {
        if (!sHasInit) {
            //存放的路径
            System.loadLibrary("c++_shared");
            System.loadLibrary("marsxlog");
            sForceOpenFlag = U.getPreferenceUtils().getSettingBoolean("key_forceOpenFlag", false);
            Log.e("MyLog", "forceOpenFlag:" + sForceOpenFlag);
            String cachePath = U.app().getFilesDir() + "/xlog";
            String logPath = U.getAppInfoUtils().getSubDirPath("logs");

            if (isDebugLogOpen() || U.getChannelUtils().isStaging()) {
                Xlog.appenderOpen(Xlog.LEVEL_ALL, Xlog.AppednerModeAsync, cachePath, logPath, U.getAppInfoUtils().getPackageName(), 0, "");
                Xlog.setConsoleLogOpen(true);
            } else {
                Xlog.appenderOpen(Xlog.LEVEL_ERROR, Xlog.AppednerModeAsync, cachePath, logPath, U.getAppInfoUtils().getPackageName(), 0, "");
                Xlog.setConsoleLogOpen(false);
            }
            com.tencent.mars.xlog.Log.setLogImp(new Xlog());
            com.tencent.mars.xlog.Log.appenderFlush(false);
        }
        sHasInit = true;
    }

    // ------------------------------------------------------------------------------
    // 日志打印方法
    // ------------------------------------------------------------------------------
    public static final void v(String msg) {
        if (!sHasInit) {
            android.util.Log.v(TAG, msg);
            return;
        }
        Log.v(TAG, msg);
    }

    public static final void d(String msg) {
        if (!sHasInit) {
            android.util.Log.d(TAG, msg);
            return;
        }
        Log.d(TAG, msg);
    }

    public static final void w(String msg) {
        if (!sHasInit) {
            android.util.Log.w(TAG, msg);
            return;
        }
        Log.w(TAG, msg);
    }


    public static final void e(String msg) {
        if (!sHasInit) {
            android.util.Log.e(TAG, msg);
            return;
        }
        Log.e(TAG, msg);
    }

    /*分割*/

    public static final void v(String tag, String msg) {
        if (!sHasInit) {
            android.util.Log.v(tag, msg);
            return;
        }
        Log.v(tag, msg);
    }

    public static final void d(String tag, String msg) {
        if (!sHasInit) {
            android.util.Log.d(tag, msg);
            return;
        }
        Log.d(tag, msg);
    }


    public static final void w(String tag, String msg) {
        if (!sHasInit) {
            android.util.Log.w(tag, msg);
            return;
        }
        Log.w(tag, msg);
    }


    public static final void i(String tag, String msg) {
        if (!sHasInit) {
            android.util.Log.i(tag, msg);
            return;
        }
        Log.i(tag, msg);
    }

    public static final void e(String tag, String msg) {
        if (!sHasInit) {
            android.util.Log.e(tag, msg);
            return;
        }
        Log.e(tag, msg);
    }

    /*分割*/


    public static final void d(String tag, String msg, Throwable tr) {
        if (!sHasInit) {
            android.util.Log.d(tag, msg, tr);
            return;
        }
        Log.d(tag, android.util.Log.getStackTraceString(tr));
    }

    public static final void d(String tag, Throwable tr) {
        if (!sHasInit) {
            android.util.Log.d(tag, "", tr);
            return;
        }
        Log.d(tag, android.util.Log.getStackTraceString(tr));
    }

    public static final void d(Throwable tr) {
        if (!sHasInit) {
            android.util.Log.d(TAG, "", tr);
            return;
        }
        Log.d(TAG, android.util.Log.getStackTraceString(tr));
    }

    public static final void e(String tag, String msg, Throwable tr) {
        if (!sHasInit) {
            android.util.Log.e(tag, msg, tr);
            return;
        }
        Log.e(tag, android.util.Log.getStackTraceString(tr));
    }

    public static final void e(String tag, Throwable tr) {
        if (!sHasInit) {
            android.util.Log.e(tag, "", tr);
            return;
        }
        Log.e(tag, android.util.Log.getStackTraceString(tr));
    }

    public static final void e(Throwable tr) {
        if (!sHasInit) {
            android.util.Log.e(TAG, "", tr);
            return;
        }
        Log.e(TAG, android.util.Log.getStackTraceString(tr));
    }

    public static final void flushLog() {
        if (!sHasInit) {
            return;
        }
        Log.appenderFlush(true);
    }

    public static void closeLog() {
        if (!sHasInit) {
            return;
        }
        Log.appenderClose();
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

    public static long pe(Integer code) {
        if (!sStartTimes.containsKey(code)) {
            return -1;
        }
        long startTime = sStartTimes.remove(code);
        String action = sActionNames.remove(code);
        long time = System.currentTimeMillis() - startTime;
        w(action + " ends in " + time + " ms");
        return time;
    }

    public static void setForceOpenFlag(boolean flag) {
        sForceOpenFlag = flag;
        U.getPreferenceUtils().setSettingBoolean("key_forceOpenFlag", flag);
//        if (sForceOpenFlag) {
//            ScreenLogPrinter.getInstance().onDebugOpenFlagChange(true);
//        } else {
//            ScreenLogPrinter.getInstance().onDebugOpenFlagChange(isDebugLogOpen());
//        }
    }

    public static boolean getForceOpenFlag() {
        return sForceOpenFlag;
    }

    public static boolean isDebugLogOpen() {
//        return BuildConfig.DEBUG || sForceOpenFlag || U.getChannelUtils().isStaging();
        return BuildConfig.DEBUG || sForceOpenFlag;
    }
}
