package com.base.utils.debug;

/**
 * Created by lan on 16/5/31.
 *
 * @author lan
 * @description 堆栈跟踪
 */
public class Tracer {
    /**
     * 返回调用者的名字信息
     */
    public static String getCallerName() {
        StackTraceElement stack[] = Thread.currentThread().getStackTrace();
        // 0: thread: java.lang.Thread
        // 1: current class: Tracer
        // 2: first caller
        // 3: 2's caller
        return stack[4].getClassName();
    }
}
