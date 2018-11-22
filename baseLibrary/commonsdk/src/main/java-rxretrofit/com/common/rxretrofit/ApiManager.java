package com.common.rxretrofit;

import com.common.base.BuildConfig;
import com.common.log.MyLog;
import com.common.rxretrofit.Api.BaseApi;
import com.common.rxretrofit.http.cookie.CookieInterceptor;
import com.common.rxretrofit.interceptor.UserAgentInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

public class ApiManager {

    public final static String TAG = "ApiManager";

    /**
     * 默认的Retrofit，大部分都由他发包
     */
    private static final String mBaseUrl = "http://dev.api.inframe.mobi/";

    private Retrofit mDefalutRetrofit;

    private static class HttpManagerHolder {
        private static final ApiManager INSTANCE = new ApiManager();
    }

    private ApiManager() {
        //手动创建一个OkHttpClient并设置超时时间缓存等设置
        OkHttpClient.Builder defaultClient = new OkHttpClient.Builder()
                .connectTimeout(10 * 1000, TimeUnit.MILLISECONDS);

        defaultClient.addInterceptor(new UserAgentInterceptor());

        if (BuildConfig.DEBUG) {
            // 确保这个拦截器最后添加,以便打印更多的日志
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    MyLog.d(TAG, message);
                }
            });
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            defaultClient.addInterceptor(httpLoggingInterceptor);
        } else {

        }

        /*创建retrofit对象*/
        mDefalutRetrofit = new Retrofit.Builder()
                .client(defaultClient.build())
                .addConverterFactory(FastJsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(mBaseUrl)
                .build();
    }

    public static final ApiManager getInstance() {
        return HttpManagerHolder.INSTANCE;
    }

    public <T> T createService(Class<T> cls) {
        return mDefalutRetrofit.create(cls);
    }


    /**
     * 新建一个 http 客户端
     * 自定义参数
     *
     * @param baseParm
     * @return
     */
    public Retrofit newClient(BaseApi baseParm) {
        //手动创建一个OkHttpClient并设置超时时间缓存等设置
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(baseParm.getConnectionTime(), TimeUnit.SECONDS);
        builder.addInterceptor(new CookieInterceptor(baseParm.isCache(), baseParm.getUrl()));

        /*创建retrofit对象*/
        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(FastJsonConverterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseParm.getBaseUrl())
                .build();

        return retrofit;
    }
}
