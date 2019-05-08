package com.didichuxing.doraemonkit.config;

import android.content.Context;

import com.common.log.MyLog;
import com.didichuxing.doraemonkit.constant.SharedPrefsKey;
import com.didichuxing.doraemonkit.util.SharedPrefsUtil;

public class CrashCaptureConfig {

    public static boolean isCrashCaptureOpen(Context context) {
//        return SharedPrefsUtil.getBoolean(context, SharedPrefsKey.CRASH_OPEN, MyLog.isDebugLogOpen());
        return SharedPrefsUtil.getBoolean(context, SharedPrefsKey.CRASH_OPEN, true);
    }

    public static void setCrashCaptureOpen(Context context, boolean open) {
        SharedPrefsUtil.putBoolean(context, SharedPrefsKey.CRASH_OPEN, open);
    }
}
