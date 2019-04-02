package com.common.rxretrofit;

import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.log.MyLog;
import com.common.mvp.PresenterEvent;
import com.common.mvp.RxLifeCyclePresenter;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.FragmentEvent;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ApiMethods {

    static HashMap<String, Disposable> sReqMap = new HashMap<>();

//    /**
//     * 订阅数据
//     *
//     * @param observable
//     * @param apiObserver
//     * @param <T>
//     */
//    public static <T> void subscribe(Observable<T> observable, ApiObserver<T> apiObserver) {
//        innerSubscribe(observable, apiObserver, null);
//    }
//
//    /**
//     * 订阅数据，并绑定BaseFragment的生命周期
//     *
//     * @param observable
//     * @param apiObserver
//     * @param baseFragment
//     * @param <T>
//     */
//    public static <T> void subscribe(Observable<T> observable, ApiObserver<T> apiObserver, BaseFragment baseFragment) {
//        innerSubscribe(observable, apiObserver, baseFragment.bindUntilEvent(FragmentEvent.DESTROY));
//    }
//
//    /**
//     * 订阅数据，并绑定BaseFragment的生命周期
//     *
//     * @param observable
//     * @param apiObserver
//     * @param baseActivity
//     * @param <T>
//     */
//    public static <T> void subscribe(Observable<T> observable, ApiObserver<T> apiObserver, BaseActivity baseActivity) {
//        innerSubscribe(observable, apiObserver, baseActivity.bindUntilEvent(ActivityEvent.DESTROY));
//    }
//
//    /**
//     * 订阅数据，并绑定RxLifeCyclePresenter的声明周期
//     *
//     * @param observable
//     * @param apiObserver
//     * @param presenter
//     * @param <T>
//     */
//    public static <T> void subscribe(Observable<T> observable, ApiObserver<T> apiObserver, RxLifeCyclePresenter presenter) {
//        innerSubscribe(observable, apiObserver, presenter.bindUntilEvent(PresenterEvent.DESTROY));
//    }
//
//
//    private static <T> void innerSubscribe(Observable<T> observable, ApiObserver<T> apiObserver, ObservableTransformer transformer) {
//        Observable<T> ob1 = observable.subscribeOn(Schedulers.io())
//                .unsubscribeOn(Schedulers.io());
//        if (transformer != null) {
//            ob1 = ob1.compose(transformer);
//        }
//        ob1 = ob1.observeOn(AndroidSchedulers.mainThread());
//        if (apiObserver == null) {
//            // 给个默认的实现，防止崩溃
//            ob1.subscribe(new ApiObserver<T>() {
//                @Override
//                public void process(T obj) {
//
//                }
//            });
//        } else {
//            ob1.subscribe(apiObserver);
//        }
//    }

    /**
     * 订阅数据，自己处理生命周期
     *
     * @param observable
     * @param apiObserver
     * @param <T>
     * @return
     */
    public static <T> Disposable subscribe(Observable<T> observable, ApiObserver<T> apiObserver) {
        return subscribe(observable, apiObserver, null,null);
    }

    /**
     * 订阅数据，并绑定BaseFragment的生命周期
     *
     * @param observable
     * @param apiObserver
     * @param baseFragment
     * @param <T>
     */
    public static <T> Disposable subscribe(Observable<T> observable, ApiObserver<T> apiObserver, BaseFragment baseFragment) {
        return subscribe(observable, apiObserver, baseFragment.bindUntilEvent(FragmentEvent.DESTROY),null);
    }

    /**
     * 订阅数据，并绑定BaseFragment的生命周期
     *
     * @param observable
     * @param apiObserver
     * @param baseActivity
     * @param <T>
     */
    public static <T> Disposable subscribe(Observable<T> observable, ApiObserver<T> apiObserver, BaseActivity baseActivity) {
        return subscribe(observable, apiObserver, baseActivity.bindUntilEvent(ActivityEvent.DESTROY),null);
    }

    /**
     * 订阅数据，并绑定RxLifeCyclePresenter的声明周期
     *
     * @param observable
     * @param apiObserver
     * @param presenter
     * @param <T>
     */
    public static <T> Disposable subscribe(Observable<T> observable, ApiObserver<T> apiObserver, RxLifeCyclePresenter presenter) {
        return subscribe(observable, apiObserver, presenter.bindUntilEvent(PresenterEvent.DESTROY),null);
    }

    public static <T> Disposable subscribe(final Observable<T> observable, final ApiObserver<T> apiObserver, ObservableTransformer transformer, final RequestControl requestControl) {

        if (requestControl != null) {
            if (requestControl.mControlType == ControlType.CancelLast) {
                // 取消上一次,继续
                Disposable lastDisposeable = sReqMap.get(requestControl.mKey);
                if (lastDisposeable != null && !lastDisposeable.isDisposed()) {
                    lastDisposeable.dispose();
                }
            } else if (requestControl.mControlType == ControlType.CancelThis) {
                // 取消上一次,继续
                Disposable lastDisposeable = sReqMap.get(requestControl.mKey);
                if (lastDisposeable != null && !lastDisposeable.isDisposed()) {
                    MyLog.e("ApiMethod", "请求key=" + requestControl.mKey + "还在执行，取消这一次");
                    return null;
                }
            }
        }
        Observable<T> ob1 = observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io());
        if (transformer != null) {
            ob1 = ob1.compose(transformer);
        }

        ob1 = ob1.observeOn(AndroidSchedulers.mainThread());
        if (apiObserver == null) {
            // 给个默认的实现，防止崩溃
            final ApiObserver observer = new ApiObserver() {
                @Override
                public void process(Object obj) {

                }
            };

            Disposable disposable = ob1.subscribe(new Consumer<T>() {
                @Override
                public void accept(T t) throws Exception {
                    observer.onNext(t);
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    observer.onError(throwable);
                    if (requestControl != null) {
                        sReqMap.remove(requestControl.mKey);
                    }
                }
            }, new Action() {
                @Override
                public void run() throws Exception {
                    observer.onComplete();
                    if (requestControl != null) {
                        sReqMap.remove(requestControl.mKey);
                    }
                }
            }, new Consumer<Disposable>() {
                @Override
                public void accept(Disposable disposable) throws Exception {
                    observer.onSubscribe(disposable);
                }
            });
            if (requestControl != null ) {
                sReqMap.put(requestControl.mKey, disposable);
            }
            return disposable;

        } else {
            Disposable disposable = ob1.subscribe(new Consumer<T>() {
                @Override
                public void accept(T t) throws Exception {
                    apiObserver.onNext(t);
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    apiObserver.onError(throwable);
                    if (requestControl != null) {
                        sReqMap.remove(requestControl.mKey);
                    }
                }
            }, new Action() {
                @Override
                public void run() throws Exception {
                    apiObserver.onComplete();
                    if (requestControl != null) {
                        sReqMap.remove(requestControl.mKey);
                    }
                }
            }, new Consumer<Disposable>() {
                @Override
                public void accept(Disposable disposable) throws Exception {
                    apiObserver.onSubscribe(disposable);
                }
            });
            if (requestControl != null ) {
                sReqMap.put(requestControl.mKey, disposable);
            }
            return disposable;
        }
    }

    private class RequestControl {
        String mKey; // key 用于标识 唯一的请求行为
        ControlType mControlType; // 控制类型

        public RequestControl(String key, ControlType controlType) {
            mKey = key;
            mControlType = controlType;
        }
    }

    private enum ControlType {
        CancelLast,// 同一个api请求不可以重复，有重复会取消上一次
        CancelThis// 同一个api请求不可以重复，有重复会取消这一次
        // 如果不想有控制 RequestControl 传入 null
    }
}
