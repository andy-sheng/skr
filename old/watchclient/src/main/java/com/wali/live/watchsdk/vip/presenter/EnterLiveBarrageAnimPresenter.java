package com.wali.live.watchsdk.vip.presenter;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.vip.contact.EnterLiveBarrageAnimContact;
import com.wali.live.watchsdk.vip.manager.OperationAnimManager;
import com.wali.live.watchsdk.vip.model.OperationAnimation;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

public class EnterLiveBarrageAnimPresenter extends RxLifeCyclePresenter implements EnterLiveBarrageAnimContact.Ipresenter {
    private static final String TAG = "EnterLiveBarrageAnimPresenter";

    private EnterLiveBarrageAnimContact.Iview mIview;
    private Subscription mGetExistedAnimResSubscription;
    private Subscription mTryLoadAnimResSubscription;
    private Subscription mTransformFileToDrawableSubscribe;

    public EnterLiveBarrageAnimPresenter(EnterLiveBarrageAnimContact.Iview iview) {
        EventBus.getDefault().register(this);
        this.mIview = iview;
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

        if(mTransformFileToDrawableSubscribe != null && !mTransformFileToDrawableSubscribe.isUnsubscribed()) {
            mTransformFileToDrawableSubscribe.unsubscribe();
        }
    }

    @Override
    public void getExistedAnimRes(final int animId) {
        mGetExistedAnimResSubscription = Observable.create(new Observable.OnSubscribe<OperationAnimation>() {
            @Override
            public void call(Subscriber<? super OperationAnimation> subscriber) {
                OperationAnimation animation = OperationAnimManager.getExistedAnimRes(animId);
                subscriber.onNext(animation);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<OperationAnimation>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Action1<OperationAnimation>() {
                    @Override
                    public void call(OperationAnimation animation) {

                        if (animation == null) {
                            mIview.onNoRes();
                        } else {
                            mIview.getExistedAnimResSuccess(animation);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, throwable.getMessage());
                    }
                });
    }

    @Override
    public void tryLoadAnimRes() {
        //去下载动效的webp资源，下载完了也不播
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

    @Override
    public void transformFileToDrawable(final List<String> paths) {
        mTransformFileToDrawableSubscribe = Observable.create(new Observable.OnSubscribe<List<Drawable>>() {
            @Override
            public void call(Subscriber<? super List<Drawable>> subscriber) {

                List<Drawable> results = new ArrayList<>();
                if(paths != null && !paths.isEmpty()) {
                    for(int i = 0; i < paths.size(); i++) {
                        results.add(tryTransformFileToDrawable(paths.get(i)));
                    }
                }
                if (!results.isEmpty()) {
                    subscriber.onNext(results);
                } else {
                    subscriber.onError(new Exception("drawable == null"));
                }

                subscriber.onCompleted();
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<List<Drawable>>bindUntilEvent(PresenterEvent.DESTROY))
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<Drawable>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(List<Drawable> datas) {
                        mIview.transformFileToDrawableSuccess(datas);
                    }
                });
    }

    private Drawable tryTransformFileToDrawable(String path) {
        Drawable drawable = null;
        File tempFile = new File(path);
        if (tempFile.exists()) {
            try {
                drawable = BitmapDrawable.createFromPath(tempFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (drawable == null) {
            MyLog.d("BitmapToDrawable", "Fail to transform drawable");
        }
        return drawable;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(EventClass.UpdateVipEnterRoomEffectSwitchEvent event) {
        MyLog.d(TAG, "UpdateVipEnterRoomEffectSwitchEvent");

        if(event == null) {
            return;
        }

        if(mIview != null) {
            mIview.updateVipEnterRoomEffectSwitchEvent(event.anchorId, event.enableEffect);
        }
    }
}
