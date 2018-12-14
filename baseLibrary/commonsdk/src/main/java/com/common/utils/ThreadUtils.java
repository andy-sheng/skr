package com.common.utils;

import android.os.Looper;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

/**
 * 通过U.getThreadUtils获得
 */
public class ThreadUtils {

    ThreadPoolExecutor mUrgentIOThreadPool;

    ThreadUtils() {
    }

    public void ensureUiThread() {
        if (!isUiThread()) {
            throw new IllegalStateException("ensureUiThread: thread check failed");
        }
    }

    public void ensureNotUiThread() {
        if (isUiThread()) {
            throw new IllegalStateException("ensureNonUiThread: thread check failed");
        }
    }

    public boolean isUiThread() {
        final Looper myLooper = Looper.myLooper();
        final Looper mainLooper = Looper.getMainLooper(); // never null

        return mainLooper.equals(myLooper);
    }

    /**
     * public ThreadPoolExecutor(int corePoolSize,
     * int maximumPoolSize,
     * long keepAliveTime,
     * TimeUnit unit,
     * BlockingQueue<Runnable> workQueue,
     * ThreadFactory threadFactory,
     * RejectedExecutionHandler handler) {
     * this.corePoolSize = corePoolSize;
     * this.maximumPoolSize = maximumPoolSize;
     * this.workQueue = workQueue;
     * this.keepAliveTime = unit.toNanos(keepAliveTime);
     * this.threadFactory = threadFactory;
     * this.handler = handler;
     * }
     * <p>
     * corePoolSize: 线程池的核心线程数，默认情况下， 核心线程会在线程池中一直存活， 即使处于闲置状态.
     * 但如果将allowCoreThreadTimeOut设置为true的话, 那么核心线程也会有超时机制， 在keepAliveTime设置的时间过后，
     * 核心线程也会被终止.
     * maximumPoolSize: 最大的线程数， 包括核心线程， 也包括非核心线程， 在线程数达到这个值后，新来的任务将会被阻塞.
     * keepAliveTime: 超时的时间， 闲置的非核心线程超过这个时长，讲会被销毁回收， 当allowCoreThreadTimeOut为true时，这个值也作用于核心线程.
     * unit：超时时间的时间单位.
     * workQueue：线程池的任务队列， 通过execute方法提交的runnable对象会存储在这个队列中.
     * threadFactory: 线程工厂, 为线程池提供创建新线程的功能.
     * handler: 任务无法执行时，回调handler的rejectedExecution方法来通知调用者.
     *
     * @return
     */
    public ThreadPoolExecutor getUrgentIoThreadPool() {
        if(mUrgentIOThreadPool==null){
            synchronized (this){
                if(mUrgentIOThreadPool==null){
                    mUrgentIOThreadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                            60L, TimeUnit.SECONDS,
                            new SynchronousQueue<Runnable>(), new CustomThreadFactory());
                }
            }
        }
        return mUrgentIOThreadPool;
    }

    /**
     * 比一般的IO线程优先级高点的紧急IO线程池子
     * @return
     */
    public Scheduler urgentIO() {
        return Schedulers.from(getUrgentIoThreadPool());
    }

    private static class CustomThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        CustomThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "-" +
                    poolNumber.getAndIncrement() +
                    "-zq_custom-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, new Runnable() {
                @Override
                public void run() {
                    // android侧设置进程优先级，值越小，优先级越高
                    // UI线程是-10  不超过ui线程，尽可能高就好了
                    android.os.Process.setThreadPriority(-9);
                    r.run();
                }
            },
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.MAX_PRIORITY) {
                // java层设置优先级，值越高优先级越高，UI线程是29无法超越
                t.setPriority(Thread.MAX_PRIORITY);
            }
            return t;
        }
    }

}

