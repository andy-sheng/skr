package com.base.thread;

import android.support.annotation.NonNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yangli on 2017/11/29.
 */
public class NamedThreadFactory implements ThreadFactory {
    private final String name;
    private final int priority;

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public NamedThreadFactory(@NonNull String name) {
        this.name = name + "-";
        priority = Thread.NORM_PRIORITY;
    }

    public NamedThreadFactory(@NonNull String name, int priority) {
        this.name = name + "-";
        this.priority = priority;
    }

    @Override
    public Thread newThread(@NonNull Runnable r) {
        final Thread t = new Thread(r, name + threadNumber.getAndIncrement());
        if (priority != -1) {
            t.setPriority(priority);
        }
        return t;
    }
}
