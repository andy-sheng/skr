package com.common.rxretrofit.interceptor;

import com.common.core.account.UserAccountManager;
import com.common.utils.U;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CoreInfoInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        // todo 标识设备的唯一ID
        request = request.newBuilder()
                .addHeader("Inframe-Client-ID",U.getDeviceUtils().getDeviceID())
                .build();
        //before
        Response response = chain.proceed(request);
        //after
        return response;
    }

}
