package com.zq.mediaengine.kit.log;

import com.common.log.MyLog;

public abstract class LogRunnable implements Runnable {
    String log;

    public LogRunnable(String log) {
        this.log = log;
    }

    @Override
    public final void run() {
        MyLog.w("ZqEngineKit", log + " begin");
        realRun();
        MyLog.w("ZqEngineKit", log + " end");
    }

    public abstract void realRun();
}
