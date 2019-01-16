package com.common.rxretrofit.interceptor;

import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.utils.U;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CoreInfoInterceptor implements Interceptor {
    public final static String TAG = "CoreInfoInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        // todo 标识设备的唯一ID
        request = request.newBuilder()
                .addHeader("Inframe-Client-ID", U.getDeviceUtils().getDeviceID())
                .build();
        // 如果是测试环境的话
        HttpUrl httpUrl = request.url();
        String scheme = httpUrl.scheme();
        if (U.getChannelUtils().isStaging()) {
            if (scheme.equals("https")) {
                scheme = "http";
            }
        } else {
            if (scheme.equals("http")) {
                scheme = "https";
            }
        }
        String host = httpUrl.host();
        host = ApiManager.getInstance().findRealHostByChannel(host);
        // 替换host
        httpUrl = httpUrl.newBuilder().scheme(scheme).host(host).build();
        request = request.newBuilder()
                .url(httpUrl)
                .build();
        //before
        Response response = null;
        try {
            response = chain.proceed(request);
        } catch (SocketTimeoutException exception) {
            String log = new StringBuilder().append("请求超时").append(exception.getMessage())
                    .append("\n")
                    .append("url=").append(httpUrl.toString())
                    .toString();
            MyLog.w(TAG, log);
        }
        //after
        return response;
    }

}
