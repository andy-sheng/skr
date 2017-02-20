package com.base.presenter;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

/**
 * Created by yangli on 16-8-5.
 */
public abstract class RxLifeCyclePresenter implements Presenter {

    private BehaviorSubject<PresenterEvent> mBehaviorSubject = BehaviorSubject.create();

    @Override
    public void start() {
        mBehaviorSubject.onNext(PresenterEvent.START);
    }

    @Override
    public void resume() {
        mBehaviorSubject.onNext(PresenterEvent.RESUME);
    }

    @Override
    public void pause() {
        mBehaviorSubject.onNext(PresenterEvent.PAUSE);
    }

    @Override
    public void stop() {
        mBehaviorSubject.onNext(PresenterEvent.STOP);
    }

    @Override
    public void destroy() {
        mBehaviorSubject.onNext(PresenterEvent.DESTROY);
    }

    protected final <T> Observable.Transformer<T, T> bindUntilEvent(PresenterEvent event) {
        return PresenterEvent.bindUntilEvent(mBehaviorSubject, event);
    }

    protected enum PresenterEvent {
        START, RESUME, PAUSE, STOP, DESTROY;

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
