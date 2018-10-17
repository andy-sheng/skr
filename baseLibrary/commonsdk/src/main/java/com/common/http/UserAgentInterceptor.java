package com.common.http;

import com.common.log.MyLog;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by anping on 16-6-17.
 */
public class UserAgentInterceptor implements Interceptor {

    private final String userAgent;

    public UserAgentInterceptor(String userAgent) {
        userAgent = userAgent.replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "");//将不合法字符都替换掉
        this.userAgent = userAgent;
    }

    /**
     * 文件头信息
     * @param chain
     * @return
     * @throws IOException
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request requestWithUserAgent = null;
        try {
            requestWithUserAgent = originalRequest.newBuilder()
                    .header("User-Agent", userAgent)
                    .build();
        } catch (Exception e) {
            MyLog.e(e);
        }
        return chain.proceed(requestWithUserAgent);
    }
}
