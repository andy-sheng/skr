package com.base.mvp.specific;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.mvp.IRxView;

import rx.Observable;
import rx.functions.Func1;
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

    protected <V extends View> V $(@IdRes int resId) {
        return (V) findViewById(resId);
    }

    @CallSuper
    public void destroy() {
        mBehaviorSubject.onNext(PresenterEvent.DESTROY);
    }

    @Override
    public <T> Observable.Transformer<T, T> bindLifecycle() {
        return PresenterEvent.bindUntilEvent(mBehaviorSubject, PresenterEvent.DESTROY);
    }

    protected enum PresenterEvent {
        DESTROY;

        static <T, R> Observable.Transformer<T, T> bindUntilEvent(final BehaviorSubject<R> subject, final R event) {
            return new Observable.Transformer<T, T>() {
                @Override
                public Observable<T> call(Observable<T> source) {
                    return source.takeUntil(
                            subject.takeFirst(new Func1<R, Boolean>() {
                                @Override
                                public Boolean call(R lifecycleEvent) {
                                    return lifecycleEvent == event;
                                }
                            })
                    );
                }
            };
        }
    }
}
