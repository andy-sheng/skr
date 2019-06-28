package com.common.view.ex;

import com.trello.rxlifecycle2.LifecycleTransformer;

/**
 * 可以订阅生命周期的view 一般为
 * {@link ExConstraintLayout}
 * {@link ExRelativeLayout}
 */
public interface RxLifecycleView {
    public <T> LifecycleTransformer<T> bindDetachEvent();
}
