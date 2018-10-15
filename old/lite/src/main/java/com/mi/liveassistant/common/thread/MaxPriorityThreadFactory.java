package com.mi.liveassistant.common.thread;

import java.util.concurrent.ThreadFactory;

/**
 * Created by chenyong on 16/7/1.
 */
public class MaxPriorityThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName("ThreadForEngine:");
        t.setPriority(Thread.MAX_PRIORITY);
        return t;
    }
}
