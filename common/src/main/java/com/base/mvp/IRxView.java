package com.base.mvp;

import rx.Observable;

/**
 * Created by lan on 17/4/5.
 */
public interface IRxView {
    <T> Observable.Transformer<T, T> bindLifecycle();
}
