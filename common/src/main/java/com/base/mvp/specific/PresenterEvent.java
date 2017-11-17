package com.base.mvp.specific;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

public enum PresenterEvent {
    DESTROY;

    static final <T, R> Observable.Transformer<T, T> bindUntilEvent(final BehaviorSubject<R> subject, final R event) {
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