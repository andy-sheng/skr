package com.component.mediaengine.util;

/**
 * Created by sujia on 2017/4/25.
 */
import android.content.Context;

import java.lang.reflect.Method;

public final class ContextUtil {

    /**
     * Context对象
     */
    private static Context CONTEXT_INSTANCE;

    /**
     * 取得Context对象
     * PS:必须在主线程调用
     * @return Context
     */
    public static Context getContext() {
        if (CONTEXT_INSTANCE == null) {
            synchronized (ContextUtil.class) {
                if (CONTEXT_INSTANCE == null) {
                    try {
                        Class<?> ActivityThread = Class.forName("android.app.ActivityThread");

                        Method method = ActivityThread.getMethod("currentActivityThread");
                        Object currentActivityThread = method.invoke(ActivityThread);//获取currentActivityThread 对象

                        Method method2 = currentActivityThread.getClass().getMethod("getApplication");
                        CONTEXT_INSTANCE =(Context)method2.invoke(currentActivityThread);//获取 Context对象

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return CONTEXT_INSTANCE;
    }
}
