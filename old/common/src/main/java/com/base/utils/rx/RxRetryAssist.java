package com.base.utils.rx;

import android.text.TextUtils;

import com.base.common.R;
import com.base.global.GlobalData;
import com.base.log.MyLog;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * rxjava retryWhen 封装类，方便重新订阅数据流
 * Created by chengsimin on 16/2/27.
 */
public class RxRetryAssist implements Func1<Observable<? extends Throwable>, Observable<?>> {

    private int mRetryCount = 1; // 重试次数

    private int mRetryInterval = 5; // 重试间隔

    private boolean mAutoIncrement = true; // 是否允许重试间隔倍数增长

    private String mExceedMaxRetryTip = GlobalData.app().getResources().getString(R.string.exceed_max_retry_tip);

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
    public Observable<?> call(Observable<? extends Throwable> observable) {
        return observable
                .zipWith(Observable.range(0, mRetryCount + 1), new Func2<Throwable, Integer, Object>() {
                    @Override
                    public Object call(Throwable throwable, Integer integer) {
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
                .flatMap(new Func1<Object, Observable<?>>() {
                    @Override
                    public Observable<?> call(Object i) {
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
