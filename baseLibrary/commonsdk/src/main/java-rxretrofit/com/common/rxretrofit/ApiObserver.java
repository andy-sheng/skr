package com.common.rxretrofit;

import android.util.Log;

import com.common.base.BuildConfig;
import com.common.utils.U;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 自定义的订阅者，加一层适配来处理各种常见问题
 * 比如 弹出错误提示
 *
 * @param <T>
 */
public abstract class ApiObserver<T> implements Observer<T> {
    @Override
    public void onSubscribe(Disposable d) {

    }

    public abstract void onNext(T obj);

    @Override
    public void onError(Throwable e) {
        if (BuildConfig.DEBUG || U.getChannelUtils().isTestChannel()) {
            String log = Log.getStackTraceString(e);
            U.getToastUtil().showShort(log);
        }
    }

    @Override
    public void onComplete() {

    }
}
