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

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Environment;

import com.common.base.BuildConfig;
import com.common.log.MyLog;


import java.io.File;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * @author Andy Zhang(zhangyong232@gmail.com)
 */
public class MyCrashHandler implements UncaughtExceptionHandler {

    private static final String TAG = "MyCrashHandler";

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT1 = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final MyCrashHandler sMyCrashHandler = new MyCrashHandler();


    private UncaughtExceptionHandler mOldHandler;

    private Context mContext;


    public static MyCrashHandler getInstance() {
        return sMyCrashHandler;
    }

    public void register(Context context) {
        if (context != null) {
            mOldHandler = Thread.getDefaultUncaughtExceptionHandler();
            if (mOldHandler != this) {
                Thread.setDefaultUncaughtExceptionHandler(this);
            }
            mContext = context;

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
    }

    private String getIMEI(Context mContext) {
        return "test";
    }

    public String getProcessName() {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();
        for (RunningAppProcessInfo info : infos) {
            if (info.pid == android.os.Process.myPid()) {
                return info.processName;
            }
        }
        return null;
    }
}
