
package com.wali.live.utils;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.thread.ThreadPool;

import java.util.concurrent.RejectedExecutionException;

@SuppressLint("NewApi")
public abstract class AsyncTaskUtils {
    public static <Params, Progress, Result> void exe(AsyncTask<Params, Progress, Result> asyncTask,
                                                      Params... params) {
        try {
            if (Build.VERSION.SDK_INT >= 11) {
                asyncTask.executeOnExecutor(ThreadPool.getThreadPoolExecutor(), params);
            } else {
                asyncTask.execute(params);
            }
        } catch (RejectedExecutionException e) {
            MyLog.v("async task pool full");
        }
    }

    public static <Params, Progress, Result> void exe(int level, AsyncTask<Params, Progress, Result> asyncTask,


                                                      Params... params) {
        try {
            if (Build.VERSION.SDK_INT >= 11) {

                if (null == GlobalData.getExecutorByLevel(level)) {

                    return;

                }

                asyncTask.executeOnExecutor(GlobalData.getExecutorByLevel(level), params);

            } else {
                asyncTask.execute(params);
            }
        } catch (RejectedExecutionException e) {
            MyLog.v("async task pool full");

        }
    }

    public static <Params, Progress, Result> void exeNetWorkTask(AsyncTask<Params, Progress, Result> asyncTask,
                                                                 Params... params) {
        try {
            if (Build.VERSION.SDK_INT >= 11) {

                asyncTask.executeOnExecutor(
                        GlobalData.getExecutorByLevel(GlobalData.ASYNC_EXECUTOR_LEVEL_IMAGEWORKER), params);
            } else {
                asyncTask.execute(params);
            }
        } catch (RejectedExecutionException e) {
            MyLog.v("async task pool full");
        }
    }

    public static <Params, Progress, Result> void exeIOTask(AsyncTask<Params, Progress, Result> asyncTask,
                                                            Params... params) {
        try {
            if (Build.VERSION.SDK_INT >= 11) {
                asyncTask.executeOnExecutor(
                        ThreadPool.getThreadPoolIOExecutor(), params);
            } else {
                asyncTask.execute(params);
            }
        } catch (RejectedExecutionException e) {
            MyLog.v("async task pool full");
        }
    }
}
