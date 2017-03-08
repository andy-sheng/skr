package com.base.thread;

import com.base.log.MyLog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by chenyong on 2017/3/7.
 */

public class ThreadPool {

    private static ExecutorService sEngineService = Executors.newSingleThreadExecutor();

    public static Future<?> runOnEngine(final Runnable r, final String from) {
        if (!sEngineService.isShutdown()) {
            return sEngineService.submit(new Runnable() {
                @Override
                public void run() {
                    long begin = System.currentTimeMillis();
                    MyLog.d("ThreadForEngine","exec "+from+" begin");
                    r.run();
                    long end = System.currentTimeMillis();
                    MyLog.d("ThreadForEngine","exec "+from+",time:"+(end-begin));
                }
            });
        }
        return null;
    }
}
