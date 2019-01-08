package com.common.rxretrofit;

import com.common.log.MyLog;
import com.common.rxretrofit.cookie.ClearableCookieJar;
import com.common.rxretrofit.cookie.PersistentCookieJar;
import com.common.rxretrofit.cookie.cache.SetCookieCache;
import com.common.rxretrofit.cookie.persistence.SharedPrefsCookiePersistor;
import com.common.rxretrofit.interceptor.UserAgentInterceptor;
import com.common.utils.U;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
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

    public static final String APPLICATION_JSOIN = "application/json; charset=utf-8";

    /**
     * 会影响 {@link HttpLoggingInterceptor 中的日志打印}
     * 比如 心跳日志就不打印 太多了
     */
    public static final String NO_LOG_TAG = "NO-LOG: true";


    private Retrofit mDefalutRetrofit;

    /**
     * cookie持久化，cookie相关应该在http层面做，不属于业务
     * http 的response 让 set-cookie 就 set-cookie
     */
    private ClearableCookieJar mCookieJar;

    /**
     * 外部拦截器list
     */
    private LinkedHashSet<Interceptor> mOutInterceptors = new LinkedHashSet<>();

    private HashMap<String, String> mStaging2Online = new HashMap<>();

    private HashMap<String, String> mOnline2Staging = new HashMap<>();

    private static class HttpManagerHolder {
        private static final ApiManager INSTANCE = new ApiManager();
    }

    private ApiManager() {
        List<HostPair> l = new ArrayList<>();
        /**
         * 测试环境与线上环境相关的ip添加到这
         */
        {
            HostPair hostPair = new HostPair("test.api.inframe.mobi", "dev.api.inframe.mobi");
            l.add(hostPair);
        }
        {
            HostPair hostPair = new HostPair("test.game.inframe.mobi", "test.game.inframe.mobi");
            l.add(hostPair);
        }
        for (HostPair hp : l) {
            mStaging2Online.put(hp.staging, hp.online);
            mOnline2Staging.put(hp.online, hp.staging);
        }
    }

    public static final ApiManager getInstance() {
        return HttpManagerHolder.INSTANCE;
    }

    public String findOnLineHostByStagingHost(String host) {
        if (mStaging2Online.containsKey(host)) {
            return mStaging2Online.get(host);
        }
        return host;
    }

    /**
     * 其他module 在 application onCreate 时可以调用这个加入 Interceptor
     *
     * @param interceptor
     */
    public void addInterceptor(Interceptor interceptor) {
        if (mDefalutRetrofit != null) {
            throw new IllegalStateException("can't add interceptor when mDefalutRetrofit already init");
        } else {
            mOutInterceptors.add(interceptor);
        }
    }

    private void tryInit() {
        if (mDefalutRetrofit == null) {
            synchronized (this) {
                if (mDefalutRetrofit == null) {

                    //手动创建一个OkHttpClient并设置超时时间缓存等设置
                    OkHttpClient.Builder defaultClient = new OkHttpClient.Builder()
                            .connectTimeout(3 * 1000, TimeUnit.MILLISECONDS);
                    mCookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(U.app()));
                    defaultClient.cookieJar(mCookieJar);
                    defaultClient.addInterceptor(new UserAgentInterceptor());

                    /**
                     * 确保第三方的interceptor在靠后位置，以便可以覆盖默认的
                     */
                    MyLog.d(TAG, "tryInit mOutInterceptors.size:" + mOutInterceptors.size());

                    for (Interceptor interceptor : mOutInterceptors) {
                        defaultClient.addInterceptor(interceptor);
                    }
                    mOutInterceptors.clear();


                    // 确保这个拦截器最后添加,以便打印更多的日志
                    HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                        @Override
                        public void log(String message) {
                            if (MyLog.isDebugLogOpen()) {
                                MyLog.d(TAG, message);
                            }
                        }
                    });
                    httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                    defaultClient.addInterceptor(httpLoggingInterceptor);

                    /*创建retrofit对象*/
                    mDefalutRetrofit = new Retrofit.Builder()
                            .client(defaultClient.build())
                            .addConverterFactory(FastJsonConverterFactory.create())
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .baseUrl(mBaseUrl)
                            .build();
                }
            }
        }
    }

    public <T> T createService(Class<T> cls) {
        tryInit();
        return mDefalutRetrofit.create(cls);
    }

    public void clearCookies() {
        mCookieJar.clear();
    }

    static class HostPair {
        String staging;
        String online;

        public HostPair(String staging, String online) {
            this.staging = staging;
            this.online = online;
        }
    }
}
