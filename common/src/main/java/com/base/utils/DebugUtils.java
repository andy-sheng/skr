package com.base.utils;

/**
 * Created by lan on 15-9-17.
 */
public class DebugUtils {

    // 获取当前thread的调用栈
    public static String getStackTraceOfCurrentThread() {
        Thread thread = Thread.currentThread();
        if (null != thread) {
            StackTraceElement[] stackArray = thread.getStackTrace();
            return StringUtils.join(stackArray, "\n");
        }
        return null;
    }
}
