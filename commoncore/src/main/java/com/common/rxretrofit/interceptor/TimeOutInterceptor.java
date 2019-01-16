package com.common.rxretrofit.interceptor;

import com.common.log.MyLog;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class TimeOutInterceptor implements Interceptor {
    public final static String TAG = "TimeOutInterceptor";
    @Override
    public Response intercept(Chain chain) throws IOException {
        try {
            return chain.proceed(chain.request());
        } catch (SocketTimeoutException exception) {
            MyLog.w(TAG, "请求超时" + exception.getMessage());
        }

        return chain.proceed(chain.request());
    }
}
