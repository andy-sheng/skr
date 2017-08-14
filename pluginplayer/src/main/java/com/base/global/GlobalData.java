package com.base.global;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;

import com.base.log.MyLog;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by chengsimin on 16/7/1.
 *
 * @notice 以后都用这个globaldata
 */
public class GlobalData {
    public static final int ASYNC_EXECUTOR_LEVEL_IMAGEWORKER = 0;

    public static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    private static ThreadPoolExecutor executors[] = new ThreadPoolExecutor[1];

    private static Application sApplication;

    public static DisplayMetrics displayMetrics;
    public static int screenWidth = 0;
    public static int screenHeight = 0;

    private static boolean sIsLoaded;

    public static Application app() {
        return sApplication;
    }

    public static void setApplication(Application app) {
        if (sApplication == null) {
            sApplication = app;
            recordScreenParam(app);
            initialize();
        }
    }

    public static boolean isLoaded() {
        return sIsLoaded;
    }

    private static void recordScreenParam(Context context) {
        displayMetrics = context.getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
    }

    private static void initialize() {
        RejectedExecutionHandler rehHandler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                MyLog.w("GlobalData ASYNC_EXECUTOR_LEVEL_IMAGEWORKER rehHandler");
            }
        };
        executors[ASYNC_EXECUTOR_LEVEL_IMAGEWORKER] = new ThreadPoolExecutor(0, CPU_COUNT * 20, 30, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), rehHandler);
        executors[ASYNC_EXECUTOR_LEVEL_IMAGEWORKER].allowCoreThreadTimeOut(true);
    }

    public static Executor getExecutorByLevel(int level) {
        if (level > ASYNC_EXECUTOR_LEVEL_IMAGEWORKER) {
            throw new IllegalArgumentException("wrong level");
        }
        return executors[level];
    }

    static {
        try {
            System.loadLibrary("gnustl_shared");
            System.loadLibrary("broadcast");
            System.loadLibrary("milive_transport");
            sIsLoaded = true;
        } catch (Throwable e) {
            MyLog.e(e);
            sIsLoaded = false;
        }
    }
}
