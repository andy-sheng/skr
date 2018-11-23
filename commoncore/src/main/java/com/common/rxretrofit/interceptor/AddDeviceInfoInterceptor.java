package com.common.rxretrofit.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AddDeviceInfoInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        request = request.newBuilder()
                .addHeader("自定义头部","testtest")
                .build();
        //before
        Response response = chain.proceed(request);
        //after
        return response;
    }

}
