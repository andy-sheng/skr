package com.zq.mediaengine.kit.log;

import com.common.log.MyLog;

public abstract class LogRunnable implements Runnable {
    String log;

    public LogRunnable(String log) {
        this.log = log;
    }

    @Override
    public final void run() {
        MyLog.d("ZqEngineKit", log + " begin");
        realRun();
        MyLog.d("ZqEngineKit", log + " end");
    }

    public abstract void realRun();
}
