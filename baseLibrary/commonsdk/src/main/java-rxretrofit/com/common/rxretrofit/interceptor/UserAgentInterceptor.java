package com.common.rxretrofit.interceptor;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.webkit.WebSettings;

import com.common.log.MyLog;
import com.common.utils.U;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 因为okhttp并不是真正的原生的http请求，它在header中并没有真正的User-Agent，而是“okhttp/版本号”。
 * 这里去除原生的
 * 使用浏览器默认的 User-Agent
 */
public class UserAgentInterceptor implements Interceptor {

    public final String TAG = "UserAgentInterceptor";

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public Response intercept(Chain chain) throws IOException {
        String userAgent = null;
        try {
            userAgent = WebSettings.getDefaultUserAgent(U.app());
        } catch (Exception e) {
            MyLog.e(e);
        }
        if (!TextUtils.isEmpty(userAgent)) {
            Request request = chain.request()
                    .newBuilder()
                    .removeHeader("User-Agent")//移除旧的
                    /**
                     * 使用手机浏览器默认的 User-Agent
                     */
                    .addHeader("User-Agent", userAgent)//添加真正的头部
                    .build();
            return chain.proceed(request);
        } else {
            return chain.proceed(chain.request());
        }
    }
}
