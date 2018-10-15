package com.common.mvp;

import android.support.annotation.CallSuper;

import com.common.integration.lifecycle.Lifecycleable;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.RxLifecycle;
import com.trello.rxlifecycle2.android.RxLifecycleAndroid;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by yangli on 16-8-5.
 */
public abstract class RxLifeCyclePresenter implements Presenter ,PresenterLifecycleable {
    protected final String TAG = getTAG();
    private BehaviorSubject<PresenterEvent> mBehaviorSubject = BehaviorSubject.create();
    boolean hasAddToLifeCycle = false;


    @CallSuper
    @Override
    public void addToLifeCycle() {
        hasAddToLifeCycle = true;
    }

    @CallSuper
    @Override
    public void start() {
        mBehaviorSubject.onNext(PresenterEvent.START);
    }

    @CallSuper
    @Override
    public void resume() {
        mBehaviorSubject.onNext(PresenterEvent.RESUME);
    }

    @CallSuper
    @Override
    public void pause() {
        mBehaviorSubject.onNext(PresenterEvent.PAUSE);
    }

    @CallSuper
    @Override
    public void stop() {
        mBehaviorSubject.onNext(PresenterEvent.STOP);
    }

    @CallSuper
    @Override
    public void destroy() {
        mBehaviorSubject.onNext(PresenterEvent.DESTROY);
    }

    public Subject<PresenterEvent> provideLifecycleSubject(){
        return mBehaviorSubject;
    }

    protected final <T> LifecycleTransformer<T> bindUntilEvent(PresenterEvent event) {
        if (!hasAddToLifeCycle) {
            throw new IllegalStateException("please add present to lifeCycle before call bindUntilEvent");
        }
        return RxLifecycle.bindUntilEvent(mBehaviorSubject, event);
    }

    protected String getTAG() {
        return getClass().getSimpleName();
    }


}
