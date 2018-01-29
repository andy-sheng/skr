package com.wali.live.watchsdk.income.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.base.preference.PreferenceUtils.PREF_KEY_ACCESS_TOKEN;
import static com.base.preference.PreferenceUtils.PREF_KEY_REFRESH_TOKEN;
import static com.base.preference.PreferenceUtils.SP_MI_ACCOUNT_CONFIG;


/**
 * 小米账号米币accessToken和refreshToken存储<br>
 * Created by rongzhisheng on 16-12-15.
 */
public class MiAccountToken {
    private static final String TAG = MiAccountToken.class.getSimpleName();

    private static final String DEFAULT_VALUE = "";
    private static String sAccessToken = DEFAULT_VALUE;
    private static String sRefreshToken = DEFAULT_VALUE;

    private static final Object sLock = new byte[0];

    @AnyThread
    public static void load() {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                SharedPreferences sharedPreferences = GlobalData.app().getSharedPreferences(SP_MI_ACCOUNT_CONFIG, Context.MODE_PRIVATE);
                String accessToken = PreferenceUtils.getSettingString(sharedPreferences, PREF_KEY_ACCESS_TOKEN, DEFAULT_VALUE);
                String refreshToken = PreferenceUtils.getSettingString(sharedPreferences, PREF_KEY_REFRESH_TOKEN, DEFAULT_VALUE);
                synchronized (sLock) {
                    sAccessToken = accessToken;
                    sRefreshToken = refreshToken;
                }
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(TAG, "load mi account token ok");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "load mi account token fail", e);
                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });
    }

    @AnyThread
    public static void clear() {
        synchronized (sLock) {
            sAccessToken = sRefreshToken = DEFAULT_VALUE;
        }
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                PreferenceUtils.clearPreference(GlobalData.app().getSharedPreferences(SP_MI_ACCOUNT_CONFIG, Context.MODE_PRIVATE));
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(TAG, "clear mi account token ok");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "clear mi account token fail", e);
                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });
    }

    /**
     * 默认值为空字符串
     *
     * @return
     */
    @AnyThread
    @NonNull
    public static Pair<String, String> getTokens() {
        synchronized (sLock) {
            return Pair.create(sAccessToken, sRefreshToken);
            // TODO: 16-12-16 just for test
            //String accessToken = "V2_kAEmjiyV31FY0JQ1xWprU1BRMLGakfo4qWr1RH9ryOdKCXm_0ZctTJzxINgApRHofq2bZt7tzXCJDoSpH5CIffoWjPNybWyJ9hfngJvd7PooufEezjaoCzgn6nsISzixv_Hnvn9JbOohpoFiEOHa1Q";
            //String refreshToken = "eJxjYGAQ6fly_fJyC_bje27xn9Vyt3q9j601iQEE4kFE5qzPz0B0RAjDKRDN7130WgPEYHkeBiSZjRXMgRRvUWpaUWpxRnxJfnZqHgCBCRh5";
            //return Pair.create(accessToken, refreshToken);
        }
    }

    @AnyThread
    public static void setTokens(@NonNull final String accessToken, @NonNull final String refreshToken) {
        synchronized (sLock) {
            sAccessToken = accessToken;
            sRefreshToken = refreshToken;
        }
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                SharedPreferences sharedPreferences = GlobalData.app().getSharedPreferences(SP_MI_ACCOUNT_CONFIG, Context.MODE_PRIVATE);
                PreferenceUtils.setSettingString(sharedPreferences, PREF_KEY_ACCESS_TOKEN, accessToken);
                PreferenceUtils.setSettingString(sharedPreferences, PREF_KEY_REFRESH_TOKEN, refreshToken);
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        MyLog.e(TAG, "set mi account token ok");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "set mi account token fail", e);
                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });
    }

}
