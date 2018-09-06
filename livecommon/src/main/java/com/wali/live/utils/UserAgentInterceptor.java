package com.wali.live.utils;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by anping on 16-6-17.
 */
public class UserAgentInterceptor implements Interceptor {

    private final String userAgent;

    public UserAgentInterceptor(String userAgent) {
        userAgent = userAgent.replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "");//将不合法字符都替换掉
        this.userAgent = userAgent;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request requestWithUserAgent = null;
        try {
            requestWithUserAgent = originalRequest.newBuilder()
                    .header("User-Agent", userAgent)
                    .build();
        } catch (Exception e) {

        }
        return chain.proceed(requestWithUserAgent);
    }
}
