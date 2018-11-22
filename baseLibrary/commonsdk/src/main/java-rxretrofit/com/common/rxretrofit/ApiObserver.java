package com.common.rxretrofit;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * 自定义的订阅者，加一层适配来处理各种常见问题
 * 比如 弹出错误提示
 * @param <T>
 */
public abstract class ApiObserver<T> implements Observer<T> {
    @Override
    public void onSubscribe(Disposable d) {

    }

    public abstract void onNext(T obj);

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }
}
