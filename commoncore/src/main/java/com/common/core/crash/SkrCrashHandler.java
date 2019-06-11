/*
 **        DroidPlugin Project
 **
 ** Copyright(c) 2015 Andy Zhang <zhangyong232@gmail.com>
 **
 ** This file is part of DroidPlugin.
 **
 ** DroidPlugin is free software: you can redistribute it and/or
 ** modify it under the terms of the GNU Lesser General Public
 ** License as published by the Free Software Foundation, either
 ** version 3 of the License, or (at your option) any later version.
 **
 ** DroidPlugin is distributed in the hope that it will be useful,
 ** but WITHOUT ANY WARRANTY; without even the implied warranty of
 ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 ** Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public
 ** License along with DroidPlugin.  If not, see <http://www.gnu.org/licenses/lgpl.txt>
 **
 **/

package com.common.core.crash;

import com.common.base.BuildConfig;
import com.common.log.MyLog;
import com.common.utils.U;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeoutException;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

public class SkrCrashHandler implements UncaughtExceptionHandler {

    private static final String TAG = "SkrCrashHandler";

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT1 = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final SkrCrashHandler sMyCrashHandler = new SkrCrashHandler();


    private UncaughtExceptionHandler mOldHandler;


    public static SkrCrashHandler getInstance() {
        return sMyCrashHandler;
    }

    /**
     * 保证这个是最后执行的 ，发生异常时第一个处理
     */
    public void register() {
        MyLog.d(TAG, "register");
        mOldHandler = Thread.getDefaultUncaughtExceptionHandler();
        /**
         * 其实没什么用，handler 已经被别的sdk注册了
         */
        Thread.setDefaultUncaughtExceptionHandler(this);

        //TODO 看的实现 RxJavaPlugins.onError
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                MyLog.d(TAG, throwable);
                if (BuildConfig.DEBUG) {
                    if (throwable instanceof IgnoreException || throwable.getCause() instanceof IgnoreException) {

                    } else {
                        uncaught(new Throwable("来自的rx的异常,不会导致崩溃，但要分析下原因是否合理", throwable));
                    }
                }
            }
        });

        try {
            if (U.getDeviceUtils().isOppo() || U.getDeviceUtils().isVivo()) {
                Class clazz = Class.forName("java.lang.Daemons$FinalizerWatchdogDaemon");
                Method method = clazz.getSuperclass().getDeclaredMethod("stop");
                method.setAccessible(true);
                Field field = clazz.getDeclaredField("INSTANCE");
                field.setAccessible(true);
                method.invoke(field.get(null));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static void uncaught(@NonNull Throwable error) {
        Thread currentThread = Thread.currentThread();
        UncaughtExceptionHandler handler = currentThread.getUncaughtExceptionHandler();
        handler.uncaughtException(currentThread, error);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        MyLog.e(TAG, "uncaughtException", ex);
        if (thread.getName().equals("FinalizerWatchdogDaemon") && ex instanceof TimeoutException) {
            MyLog.e(TAG, "忽略这个异常 FinalizerWatchdogDaemon TimeoutException");
        } else {
            MyLog.closeLog();
            if (mOldHandler != null) {
                mOldHandler.uncaughtException(thread, ex);
            }
        }

    }

}
