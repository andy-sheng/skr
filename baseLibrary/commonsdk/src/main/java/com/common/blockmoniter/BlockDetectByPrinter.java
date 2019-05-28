package com.common.blockmoniter;

import android.os.Looper;
import android.util.Printer;

public class BlockDetectByPrinter {
    public static void start() {

        Looper.getMainLooper().setMessageLogging(new Printer() {
            //分发和处理消息开始前的log
            private static final String START = ">>>>> Dispatching";
            //分发和处理消息结束后的log
            private static final String END = "<<<<< Finished";

            @Override
            public void println(String x) {
                if (x.startsWith(START)) {
                    //开始计时
                    LogMonitor.getInstance().startMonitor();
                }
                if (x.startsWith(END)) {
                    //结束计时，并计算出方法执行时间
                    LogMonitor.getInstance().removeMonitor();
                }
            }
        });

    }
}
