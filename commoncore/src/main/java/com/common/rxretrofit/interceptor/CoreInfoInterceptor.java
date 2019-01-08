package com.common.rxretrofit.interceptor;

import com.common.rxretrofit.ApiManager;
import com.common.utils.U;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CoreInfoInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        // todo 标识设备的唯一ID
        request = request.newBuilder()
                .addHeader("Inframe-Client-ID", U.getDeviceUtils().getDeviceID())
                .build();
        // 如果是测试环境的话
        HttpUrl httpUrl = request.url();
        String host = httpUrl.host();
        host = ApiManager.getInstance().findRealHostByChannel(host);
        // 替换host
        httpUrl = httpUrl.newBuilder().host(host).build();
        request = request.newBuilder()
                .url(httpUrl)
                .build();
        //before
        Response response = chain.proceed(request);
        //after
        return response;
    }

}
