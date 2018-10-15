package com.wali.live.watchsdk.vip.presenter;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.vip.contact.NobleUserEnterBigAnimContact;
import com.wali.live.watchsdk.vip.manager.OperationAnimManager;
import com.wali.live.watchsdk.vip.model.OperationAnimation;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zhujianning on 18-6-30.
 */

public class NobleUserEnterBigAnimPresenter extends RxLifeCyclePresenter implements NobleUserEnterBigAnimContact.IPresenter {
    private static final String TAG = "NobleUserEnterBigAnimPresenter";

    private NobleUserEnterBigAnimContact.IView mIview;
    private Subscription mGetExistedAnimResSubscription;
    private Subscription mTryLoadAnimResSubscription;

    public NobleUserEnterBigAnimPresenter(NobleUserEnterBigAnimContact.IView iView) {
        this.mIview = iView;
        EventBus.getDefault().register(this);
    }

    @Override
    public void destroy() {
        super.destroy();
        EventBus.getDefault().unregister(this);

        if(mGetExistedAnimResSubscription != null && !mGetExistedAnimResSubscription.isUnsubscribed()) {
            mGetExistedAnimResSubscription.unsubscribe();
        }

        if(mTryLoadAnimResSubscription != null && !mTryLoadAnimResSubscription.isUnsubscribed()) {
            mTryLoadAnimResSubscription.unsubscribe();
        }
    }

    @Override
    public void getExistedAnimRes(final int animId) {
        mGetExistedAnimResSubscription = Observable.create(new Observable.OnSubscribe<OperationAnimation>() {
            @Override
            public void call(Subscriber<? super OperationAnimation> subscriber) {
                OperationAnimation animation = OperationAnimManager.getExistedAnimRes(animId);
                if (animation != null) {
                    subscriber.onNext(animation);
                } else {
                    subscriber.onError(new Throwable("animation == null"));
                }
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<OperationAnimation>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Observer<OperationAnimation>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                        mIview.getExistedAnimResFail();
                    }

                    @Override
                    public void onNext(OperationAnimation operationAnimation) {
                        mIview.getExistedAnimResSuccess(operationAnimation);
                    }
                });
    }

    @Override
    public void loadAnimRes() {
        mTryLoadAnimResSubscription = Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                OperationAnimManager.loadRes();//去下载动效的webp资源，下载完了也不播
                subscriber.onCompleted();
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .compose(this.<Object>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(e);
                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(EventClass.UpdateVipEnterRoomEffectSwitchEvent event) {
        MyLog.d(TAG, "UpdateVipEnterRoomEffectSwitchEvent");

        if(event == null) {
            return;
        }

        mIview.updateVipEnterRoomEffectSwitchEvent(event.anchorId, event.enableEffect);
    }
}
