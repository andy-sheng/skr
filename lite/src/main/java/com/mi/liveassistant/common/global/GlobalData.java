package com.mi.liveassistant.common.global;

import android.content.Context;
import android.util.DisplayMetrics;

import com.mi.liveassistant.common.log.MyLog;

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
    private static int REQUEST_CODE_FIRST = 100000;

    private static Object sRequestCodeLock = new Object();

    public static final int ASYNC_EXECUTOR_LEVEL_IMAGEWORKER = 0;

    public static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    private static ThreadPoolExecutor executors[] = new ThreadPoolExecutor[1];

    private static Context sApplication;

    public static DisplayMetrics displayMetrics;
    public static int screenWidth = 0;
    public static int screenHeight = 0;

    public static Context app() {
        return sApplication;
    }

    public static void setApplication(Context app) {
        sApplication = app;
        recordScreenParam(app);
        initialize();
    }

    public static int getRequestCode() {
        synchronized (sRequestCodeLock) {
            return REQUEST_CODE_FIRST++;
        }
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
}
