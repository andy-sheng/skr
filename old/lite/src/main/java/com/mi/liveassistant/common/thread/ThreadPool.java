package com.mi.liveassistant.common.thread;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.mi.liveassistant.common.log.MyLog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    private static ExecutorService sPool = null;
    private static ExecutorService sIOPool = null;
    private static ExecutorService sEngineService = null;
    private static ExecutorService sUserInfoExecutor;
    private static ScheduledThreadPoolExecutor scheduledPool = new ScheduledThreadPoolExecutor(1);

    private static Handler sUiHandler = null;

    private static HandlerThread sHandlerThread = null;

    private static Handler sWorkerHandler = null;

    private static RejectedExecutionHandler mRejectedHandler = new RejectedExecutionHandler() {

        @Override
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
            // Some bad situation occurred: sPool is now full, so we need queue subsequent Runnable.
            // But 'put(r)' in executor.getQueue().put(r) might be locked and take too many delayTime, we need
            // post the 'queue' action into a single thread FIFO queue first, and then dispatched to
            // multiple thread pool

            // Debug.logI("ThreadPool is now full, need further dispatch ... " + r);

            ThreadPool.runOnWorker(new Runnable() {
                @Override
                public void run() {
                    try {
                        executor.getQueue().put(r);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    public static Future<?> runOnPool(Runnable r) {
        if (null != sPool) {
            return sPool.submit(r);
        }
        return null;
    }

    public static void runOnUi(Runnable r) {
        sUiHandler.post(r);
    }

    public static void runOnWorker(Runnable r) {
        sWorkerHandler.post(r);
    }

    public static Future<?> runOnEngine(final Runnable r, final String from) {
        if (sEngineService != null && !sEngineService.isShutdown()) {
            return sEngineService.submit(new Runnable() {
                @Override
                public void run() {
                    long begin = System.currentTimeMillis();
                    MyLog.d("ThreadForEngine", "exec " + from + " begin");
                    r.run();
                    long end = System.currentTimeMillis();
                    MyLog.d("ThreadForEngine", "exec " + from + ",time:" + (end - begin));
                }
            });
        }
        return null;
    }

    public static void postOnWorkerDelayed(Runnable r, int delay) {
        sWorkerHandler.postDelayed(r, delay);
    }

    public static Looper getWorkerLooper() {
        return sHandlerThread.getLooper();
    }

    public static Handler getWorkerHandler() {
        return sWorkerHandler;
    }

    /**
     * Schedule a runnable running at fixed rate
     */
    public static ScheduledFuture<?> schedule(Runnable r, long initialDelay, long period, TimeUnit unit) {
        return scheduledPool.scheduleAtFixedRate(r, initialDelay, period, TimeUnit.SECONDS);
    }

    public static ExecutorService getThreadPoolExecutor() {
        return sPool;
    }

    public static ExecutorService getThreadPoolIOExecutor() {
        return sIOPool;
    }

    public static ExecutorService getEngineExecutor() {
        return sEngineService;
    }

    public static ExecutorService getUserInfoExecutor() {
        return sUserInfoExecutor;
    }

    public static void startup() {
        ThreadUtils.ensureUiThread();

        final ThreadFactory factory = new ThreadFactory() {
            int count = 0;

            @Override
            public Thread newThread(Runnable r) {
                count++;
                Thread thr = new Thread(r, "generic-pool-" + count);
                thr.setDaemon(false);
                thr.setPriority((Thread.NORM_PRIORITY + Thread.MIN_PRIORITY) / 2);
                return thr;
            }
        };

        // standard pool runner
        int cpu_cores = Runtime.getRuntime().availableProcessors();
        final int maxThreads = cpu_cores * 32;
        sPool = new ThreadPoolExecutor(cpu_cores, maxThreads, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(
                maxThreads), factory, mRejectedHandler);
        sIOPool = new ThreadPoolExecutor(5, 10, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(
                maxThreads), factory, mRejectedHandler);
        // ui thread runner
        sUiHandler = new Handler();

        // handler based thread runner
        sHandlerThread = new HandlerThread("internal-work");
        sHandlerThread.setPriority(Thread.NORM_PRIORITY - 1);
        sHandlerThread.start();
        sWorkerHandler = new Handler(sHandlerThread.getLooper());

        sEngineService = Executors.newSingleThreadExecutor(new MaxPriorityThreadFactory());
        sUserInfoExecutor = Executors.newSingleThreadExecutor();
    }

    public static void shutdown() {
        sPool.shutdown();
        sEngineService.shutdown();
        sHandlerThread.quit();
        sUserInfoExecutor.shutdown();
    }

    public static Handler getUiHandler() {
        return sUiHandler;
    }


}

