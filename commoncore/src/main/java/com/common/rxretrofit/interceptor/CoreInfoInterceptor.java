package com.common.rxretrofit.interceptor;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.common.core.account.UserAccountManager;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.utils.U;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

public class CoreInfoInterceptor implements Interceptor {
    public final static String TAG = "CoreInfoInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        String noNeedLogin = request.header("NO_NEED_LOGIN");
        if (!TextUtils.isEmpty(noNeedLogin)) {
            request = request.newBuilder().removeHeader("NO_NEED_LOGIN").build();
        } else {
            if (!UserAccountManager.getInstance().hasAccount()) {
                MyLog.e(TAG,"未登录前不能发送该请求-->"+request.url());
                HashMap hashMap = new HashMap<>();
                hashMap.put("errno",102);
                hashMap.put("errmsg","未登录不能发送该请求-->"+request.url());
                ResponseBody responseBody = ResponseBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(hashMap));
                Response response = new Response.Builder()
                        .request(request)
                        .code(200)
                        .message("未登录不能发送该请求")
                        .protocol(Protocol.HTTP_1_0)
                        .body(responseBody)
                        .build();
                return response;
            }
        }
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
