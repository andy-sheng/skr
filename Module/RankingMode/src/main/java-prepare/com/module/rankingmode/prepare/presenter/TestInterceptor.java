package com.module.rankingmode.prepare.presenter;

import com.common.core.account.UserAccountManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TestInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        // todo 标识设备的唯一ID
        request = request.newBuilder()
                .addHeader("Inframe-User-ID", UserAccountManager.getInstance().getUuid())
                .build();
        //before
        Response response = chain.proceed(request);
        //after
        return response;
    }
}
