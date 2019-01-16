package com.common.blockmoniter;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.common.log.MyLog;

public class LogMonitor {
    private static final String TAG = "LogMonitor";
    private static LogMonitor sInstance = new LogMonitor();
    private Handler mIoHandler;
    //方法耗时的卡口,300毫秒
    private static final long TIME_BLOCK = 400L;

    private LogMonitor() {
        HandlerThread logThread = new HandlerThread("log");
        logThread.start();
        mIoHandler = new Handler(logThread.getLooper());
    }

    private static Runnable mLogRunnable = new Runnable() {
        @Override
        public void run() {
            //打印出执行的耗时方法的栈消息
            StringBuilder sb = new StringBuilder();
            StackTraceElement[] stackTrace = Looper.getMainLooper().getThread().getStackTrace();
            for (StackTraceElement s : stackTrace) {
                sb.append(s.toString());
                sb.append("\n");
            }
            MyLog.e(TAG, sb.toString());
        }
    };

    public static LogMonitor getInstance() {
        return sInstance;
    }


    /**
     * 开始计时
     */
    public void startMonitor() {
        mIoHandler.postDelayed(mLogRunnable, TIME_BLOCK);
    }

    /**
     * 停止计时
     */
    public void removeMonitor() {
        mIoHandler.removeCallbacks(mLogRunnable);
    }
}
