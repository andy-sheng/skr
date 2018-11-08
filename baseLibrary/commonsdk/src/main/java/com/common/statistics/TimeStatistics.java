package com.common.statistics;

import android.os.Looper;

import com.common.log.MyLog;

import java.util.HashMap;

/**
 * 注意这个方法，会在 gradle.properties 开启
 * methodStatisticsEnable=true 后，在编译期间通过transform class时
 * 通过 javassist 注入到 class 字节码中。用来统计方法执行时间
 * 贼猛
 */
public class TimeStatistics {
    public final static String TAG = "TimeStatistics";


    public final static int sDt = 100;

    static HashMap<String, Long> mTimeMap = new HashMap<>();

    /**
     * TimeStatistics 是否打开的标记位
     * Gradle 脚本 会在编译时 改
     */
    public static boolean getSwitch() {
        return false;
    }

    public static void setBeginTime(String className, String methodName) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            return;
        }
        mTimeMap.put(className + "_" + methodName, System.currentTimeMillis());
    }

    public static void setEndTime(String className, String methodName) {
        String key = className + "_" + methodName;
        Long ts = mTimeMap.get(key);
        if (ts != null) {
            long now = System.currentTimeMillis();
            long dt = now - ts;
            if (dt > sDt) {
                MyLog.d(TAG, className + " " + methodName + " dt:" + dt);
            }
        }
    }
}
