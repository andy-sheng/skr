package com.wali.live.componentwrapper.presenter;

import android.support.annotation.CallSuper;

import com.base.presenter.Presenter;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.presenter.ComponentPresenter;

import rx.Observable;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;

/**
 * Created by yangli on 2017/8/2.
 *
 * @author YangLi
 * @mail yanglijd@gmail.com
 */
public abstract class BaseSdkRxPresenter<VIEW> extends ComponentPresenter<VIEW>
        implements Presenter {

    private BehaviorSubject<PresenterEvent> mBehaviorSubject = BehaviorSubject.create();

    public BaseSdkRxPresenter(IEventController controller) {
        super(controller);
    }

    @Deprecated
    @Override
    public void start() {
    }

    @Deprecated
    @Override
    public void resume() {
    }

    @Deprecated
    @Override
    public void pause() {
    }

    @Deprecated
    @Override
    public void stop() {
    }

    @Override
    @CallSuper
    public void startPresenter() {
        mBehaviorSubject.onNext(PresenterEvent.START);
    }

    @Override
    @CallSuper
    public void stopPresenter() {
        mBehaviorSubject.onNext(PresenterEvent.STOP);
    }

    @Override
    @CallSuper
    public void destroy() {
        mBehaviorSubject.onNext(PresenterEvent.DESTROY);
    }

    protected final <T> Observable.Transformer<T, T> bindUntilEvent(PresenterEvent event) {
        return PresenterEvent.bindUntilEvent(mBehaviorSubject, event);
    }

    protected enum PresenterEvent {
        START, STOP, DESTROY;

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
