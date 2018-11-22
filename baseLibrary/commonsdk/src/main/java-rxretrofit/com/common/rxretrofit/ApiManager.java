package com.common.rxretrofit;

import com.common.rxretrofit.Api.BaseApi;
import com.common.rxretrofit.http.cookie.CookieInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {

    /**
     * 默认的Retrofit，大部分都由他发包
     */
    private static final String mBaseUrl = "https://suggest.taobao.com/";

    private Retrofit mDefalutRetrofit;

    private static class HttpManagerHolder {
        private static final ApiManager INSTANCE = new ApiManager();
    }

    private ApiManager() {
        //手动创建一个OkHttpClient并设置超时时间缓存等设置
        OkHttpClient defaultClient = new OkHttpClient.Builder()
                .connectTimeout(10 * 1000, TimeUnit.MILLISECONDS)
//                .addInterceptor(addInterceptor)
                .build();


        /*创建retrofit对象*/
        mDefalutRetrofit = new Retrofit.Builder()
                .client(defaultClient)
                .addConverterFactory(GsonConverterFactory.create())
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
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseParm.getBaseUrl())
                .build();

        return retrofit;
    }
}
