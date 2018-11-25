package io.rong.imkit;

import com.common.log.MyLog;
import com.common.rxretrofit.interceptor.UserAgentInterceptor;
import com.common.utils.U;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

public class RongIMAPIManager {

    public final static String TAG = "RongIMAPIManager";

    private Retrofit mDefalutRetrofit;

    private RongIMAPIManager() {

        OkHttpClient.Builder defaultClient = new OkHttpClient.Builder()
                .connectTimeout(10 * 1000, TimeUnit.MILLISECONDS);

        defaultClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                String nonce = String.valueOf((long) (Math.random() * 100000));
                String timaStamp = String.valueOf(Calendar.getInstance().getTimeInMillis());
                Request request = chain.request()
                        .newBuilder()
                        .addHeader("App-Key", getAppKey())
                        .addHeader("Nonce", nonce)
                        .addHeader("Timestamp", timaStamp)
                        .addHeader("Signature", getSign(nonce, timaStamp))
                        .build();
                return chain.proceed(request);
            }
        });

        defaultClient.addInterceptor(new UserAgentInterceptor());

        if (MyLog.isDebugLogOpen()) {
            // 确保这个拦截器最后添加,以便打印更多的日志
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    MyLog.d(TAG, message);
                }
            });
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            defaultClient.addInterceptor(httpLoggingInterceptor);
        }

        /*创建retrofit对象*/
        mDefalutRetrofit =new Retrofit.Builder()
                .client(defaultClient.build())
                .addConverterFactory(FastJsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("http://api.cn.ronghub.com/")
                .build();

    }

    private static class RongIMAPIManagerHolder {
        private static final RongIMAPIManager INSTANCE = new RongIMAPIManager();
    }

    public static final RongIMAPIManager getInstance() {
        return RongIMAPIManagerHolder.INSTANCE;
    }

    public <T> T createService(Class<T> cls) {
        return mDefalutRetrofit.create(cls);
    }

    /**
     * App-Key 或 RC-App-Key   开发者平台分配的 App Key。
     * 获取融云的appkey
     *
     * @return
     */
    public String getAppKey() {
        String appKey = U.getAppInfoUtils().getMetaInfo("RONG_CLOUD_APP_KEY");
        return appKey;
    }

    /**
     * 数据签名 Signature 或 RC-Signature
     *
     * @param nonce     Nonce 或 RC-Nonce	     随机数，无长度限制。
     * @param timeStamp Timestamp 或 RC-Timestamp	时间戳，19700101日0点0分0秒开始到现在的毫秒数。
     * @return
     */
    public String getSign(String nonce, String timeStamp) {
        StringBuilder sign = new StringBuilder();
        sign.append("EpUxnqD7saB").append(nonce).append(timeStamp);
        return U.getStringUtils().getSHA1Digest(sign.toString());
    }
}
