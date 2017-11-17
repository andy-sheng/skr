package com.base.mvp.specific;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.mvp.IRxView;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by lan on 17/4/13.
 */
public abstract class RxRelativeLayout extends RelativeLayout implements IRxView {
    protected final String TAG = getTAG();

    protected BehaviorSubject<PresenterEvent> mBehaviorSubject = BehaviorSubject.create();

    protected String getTAG() {
        return getClass().getSimpleName();
    }

    public RxRelativeLayout(Context context) {
        super(context);
    }

    public RxRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RxRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @CallSuper
    public void destroy() {
        mBehaviorSubject.onNext(PresenterEvent.DESTROY);
    }

    @Override
    public <T> Observable.Transformer<T, T> bindLifecycle() {
        return PresenterEvent.bindUntilEvent(mBehaviorSubject, PresenterEvent.DESTROY);
    }

    protected final <V extends View> V $(@IdRes int resId) {
        return (V) findViewById(resId);
    }

    protected final <V extends View> V $click(@IdRes int id, OnClickListener listener) {
        V v = $(id);
        if (v != null) {
            v.setOnClickListener(listener);
        }
        return v;
    }
}
