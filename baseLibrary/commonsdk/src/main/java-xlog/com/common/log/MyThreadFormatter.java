package com.common.log;

import com.elvishew.xlog.formatter.thread.ThreadFormatter;

/**
 * Created by linjinbin on 2018/3/20.
 */

public class MyThreadFormatter implements ThreadFormatter {

    @Override
    public String format(Thread data) {
        return "Thread: " + data.getName() + "|" + data.getId();
    }
}

