package com.common.log;

import com.elvishew.xlog.LogItem;
import com.elvishew.xlog.interceptor.Interceptor;

/**
 * Created by linjinbin on 2018/3/20.
 */

public class MyInterceptor implements Interceptor {
    @Override
    public LogItem intercept(LogItem log) {
        return log;
    }
}
