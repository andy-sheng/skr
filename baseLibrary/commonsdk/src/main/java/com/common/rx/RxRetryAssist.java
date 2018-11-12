package com.common.rx;

import android.text.TextUtils;

import com.common.log.MyLog;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

/**
 * rxjava retryWhen 封装类，方便重新订阅数据流
 * 会在onError时做出重试
 * Created by chengsimin on 16/2/27.
 */
public class RxRetryAssist implements Function<Observable<? extends Throwable>, Observable<?>> {

    private int mRetryCount = 1; // 重试次数

    private int mRetryInterval = 5; // 重试间隔 5秒后

    private boolean mAutoIncrement = true; // 是否允许重试间隔倍数增长

    private String mExceedMaxRetryTip = "超过最大重试";

    public RxRetryAssist() {

    }

    public RxRetryAssist(int mRetryCount, String mExceedMaxRetryTip) {
        this.mExceedMaxRetryTip = mExceedMaxRetryTip;
        this.mRetryCount = mRetryCount;
    }

    public RxRetryAssist(int mRetryCount, int mRetryInterval, boolean mAutoIncrement) {
        this.mRetryCount = mRetryCount;
        this.mRetryInterval = mRetryInterval;
        this.mAutoIncrement = mAutoIncrement;
    }


    @Override
    public Observable<?> apply(Observable<? extends Throwable> observable) throws Exception {
        return observable
                .zipWith(Observable.range(0, mRetryCount + 1), new BiFunction<Throwable, Integer, Object>() {
                    @Override
                    public Object apply(Throwable throwable, Integer integer) throws Exception {
                        if (throwable instanceof RefuseRetryExeption) {
                            return throwable;
                        }
                        if (integer == mRetryCount) {
                            if (TextUtils.isEmpty(mExceedMaxRetryTip)) {
                                return new MaxRetryException(throwable.getMessage(), throwable);
                            } else {
                                return new MaxRetryException(mExceedMaxRetryTip, throwable);
                            }
                        }
                        return integer;
                    }
                })
                .flatMap(new Function<Object, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Object i) throws Exception {
                        if (i instanceof RefuseRetryExeption) {
                            return Observable.error((RefuseRetryExeption) i);
                        }
                        if (i instanceof MaxRetryException) {
                            return Observable.error((MaxRetryException) i);
                        }
                        if (i instanceof Integer) {
                            int count = (Integer) i;
                            int delayTime = mRetryInterval;
                            if (mAutoIncrement) {
                                delayTime = mRetryInterval * (count + 1);
                            }
                            MyLog.d("RxRetryAssist", delayTime + "s后重试");
                            return Observable.timer(delayTime, TimeUnit.SECONDS);
                        }
                        if (i instanceof Throwable) {
                            return Observable.error((Throwable) i);
                        }
                        return Observable.error(new Exception("unknow exeption type!"));
                    }
                });
    }
}
