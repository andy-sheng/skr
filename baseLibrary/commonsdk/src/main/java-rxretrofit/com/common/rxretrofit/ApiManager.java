package com.common.rxretrofit;

import android.net.Uri;

import com.common.log.MyLog;
import com.common.rxretrofit.cookie.ClearableCookieJar;
import com.common.rxretrofit.cookie.PersistentCookieJar;
import com.common.rxretrofit.cookie.cache.SetCookieCache;
import com.common.rxretrofit.cookie.persistence.SharedPrefsCookiePersistor;
import com.common.rxretrofit.interceptor.UserAgentInterceptor;
import com.common.utils.U;

import java.util.LinkedHashSet;
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

    public static final String APPLICATION_JSON = "application/json; charset=utf-8";

    /**
     * 会影响 {@link HttpLoggingInterceptor 中的日志打印}
     * 比如 心跳日志就不打印 太多了
     */
    public static final String NO_LOG_TAG = "NO-LOG: true";// 永远都没日志，不管什么版本
    public static final String ALWAYS_LOG_TAG = "ALWAYS_LOG: true"; // 永远都有日志，不管什么版本
    public static final String NO_NEED_LOGIN_TAG = "NO_NEED_LOGIN: yes"; // 这个请求不需要登录也能发

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

    private static class HttpManagerHolder {
        private static final ApiManager INSTANCE = new ApiManager();
    }

    private ApiManager() {
    }

    public static final ApiManager getInstance() {
        return HttpManagerHolder.INSTANCE;
    }

    public String findRealHostByChannel(String host) {
        if (host.endsWith("api.inframe.mobi")) {
            if (U.getChannelUtils().isDevChannel()) {
                return "dev.api.inframe.mobi";
            } else if (U.getChannelUtils().isTestChannel()) {
                return "test.api.inframe.mobi";
            } else if (U.getChannelUtils().isSandboxChannel()) {
                return "sandbox.api.inframe.mobi";
            } else {
                // 说明是线上环境，暂时没给域名
                return "api.inframe.mobi";
            }
        } else if (host.endsWith("game.inframe.mobi")) {
            if (U.getChannelUtils().isDevChannel()) {
                return "dev.game.inframe.mobi";
            } else if (U.getChannelUtils().isTestChannel()) {
                return "test.game.inframe.mobi";
            } else if (U.getChannelUtils().isSandboxChannel()) {
                return "sandbox.game.inframe.mobi";
            } else {
                // 说明是线上环境，暂时没给域名
                return "game.inframe.mobi";
            }
        } else if (host.endsWith("res.inframe.mobi")) {
            if (U.getChannelUtils().isDevChannel()) {
                return "dev.res.inframe.mobi";
            } else if (U.getChannelUtils().isTestChannel()) {
                return "test.res.inframe.mobi";
            } else if (U.getChannelUtils().isSandboxChannel()) {
                return "sandbox.res.inframe.mobi";
            } else {
                // 说明是线上环境，暂时没给域名
                return "res.inframe.mobi";
            }
        } else if (host.endsWith("kconf.inframe.mobi")) {
            if (U.getChannelUtils().isDevChannel()) {
                return "dev.kconf.inframe.mobi";
            } else if (U.getChannelUtils().isTestChannel()) {
                return "test.kconf.inframe.mobi";
            } else if (U.getChannelUtils().isSandboxChannel()) {
                return "sandbox.kconf.inframe.mobi";
            } else {
                // 说明是线上环境，暂时没给域名
                return "kconf.inframe.mobi";
            }
        } else if (host.endsWith("room.inframe.mobi")) {
            if (U.getChannelUtils().isDevChannel()) {
                return "dev.room.inframe.mobi";
            } else if (U.getChannelUtils().isTestChannel()) {
                return "test.room.inframe.mobi";
            } else if (U.getChannelUtils().isSandboxChannel()) {
                return "sandbox.room.inframe.mobi";
            } else {
                // 说明是线上环境，暂时没给域名
                return "room.inframe.mobi";
            }
        } else if (host.endsWith("stand.inframe.mobi")) {
            if (U.getChannelUtils().isDevChannel()) {
                return "dev.stand.inframe.mobi";
            } else if (U.getChannelUtils().isTestChannel()) {
                return "test.stand.inframe.mobi";
            } else if (U.getChannelUtils().isSandboxChannel()) {
                return "sandbox.stand.inframe.mobi";
            } else {
                // 说明是线上环境，暂时没给域名
                return "stand.inframe.mobi";
            }
        } else if (host.endsWith("app.inframe.mobi")) {
            if (U.getChannelUtils().isDevChannel()) {
                return "dev.app.inframe.mobi";
            } else if (U.getChannelUtils().isTestChannel()) {
                return "test.app.inframe.mobi";
            } else if (U.getChannelUtils().isSandboxChannel()) {
                return "sandbox.app.inframe.mobi";
            } else {
                // 说明是线上环境，暂时没给域名
                return "app.inframe.mobi";
            }
        }else if(host.endsWith("www.skrer.mobi")){
            if (U.getChannelUtils().isDevChannel()) {
                return "dev.app.inframe.mobi";
            } else if (U.getChannelUtils().isTestChannel()) {
                return "test.app.inframe.mobi";
            } else if (U.getChannelUtils().isSandboxChannel()) {
                return "sandbox.app.inframe.mobi";
            } else {
                // 说明是线上环境，暂时没给域名,线上环境使用skr的域名
                return "www.skrer.mobi";
            }
        }
        return host;
    }

    public String findRealUrlByChannel(String url) {
        Uri uri = Uri.parse(url);
        String host = findRealHostByChannel(uri.getHost());
        url = url.replace(uri.getHost(), host);
        return url;
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
                            .connectTimeout(5 * 1000, TimeUnit.MILLISECONDS);
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
                            MyLog.w(TAG, message);
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
        String sandbox;
        String dev;
        String test;
        String online;

        public HostPair() {
        }

        public String getSandbox() {
            return sandbox;
        }

        public void setSandbox(String sandbox) {
            this.sandbox = sandbox;
        }

        public String getDev() {
            return dev;
        }

        public void setDev(String dev) {
            this.dev = dev;
        }

        public String getTest() {
            return test;
        }

        public void setTest(String test) {
            this.test = test;
        }

        public String getOnline() {
            return online;
        }

        public void setOnline(String online) {
            this.online = online;
        }
    }
}
