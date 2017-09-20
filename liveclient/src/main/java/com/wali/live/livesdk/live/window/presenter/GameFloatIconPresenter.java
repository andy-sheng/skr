package com.wali.live.livesdk.live.window.presenter;

import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.wali.live.dao.Gift;
import com.wali.live.livesdk.live.window.event.FloatGiftEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by wangmengjie on 17-9-11.
 */

public class GameFloatIconPresenter extends BaseRxPresenter<IGameFloatIcon> {

    private IGameFloatIcon mView;
    private Subscription giftSubscription;
    private LinkedList<Gift> mGiftQueue = new LinkedList<>();
    private boolean mReceiveGift = true;

    @Override
    protected String getTAG() {
        return "GameFloatIconPresenter";
    }

    public GameFloatIconPresenter(IGameFloatIcon view) {
        mView = view;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void unregister() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(FloatGiftEvent event) {
        if (!mReceiveGift) {
            mGiftQueue.clear();
            return;
        }
        boolean mIsAdd = false;
        if (event != null && event.barrageMsg != null) {
            Gift gift = event.barrageMsg;
            MyLog.d(TAG, "gift = " + gift.getPicture());
            mIsAdd = mGiftQueue.add(gift);
        }
        if (mIsAdd) {
            processGift();
        }
    }

    private void processGift() {
        Gift gift = mGiftQueue.poll();
        if (gift != null) {
            if (giftSubscription != null && !giftSubscription.isUnsubscribed()) {
                giftSubscription.unsubscribe();
            }
            BaseImage image = ImageFactory.newHttpImage(gift.getPicture()).build();
            mView.startGiftAnimator(false, image);
        }
    }

    public void giftShowTimer(long delayTime) {
        if (giftSubscription != null && !giftSubscription.isUnsubscribed()) {
            giftSubscription.unsubscribe();
        }
        giftSubscription = Observable.timer(delayTime, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if (!mReceiveGift)
                            return;
                        mView.startGiftAnimator(mGiftQueue.isEmpty(), null);
                        if (!mGiftQueue.isEmpty()) {
                            processGift();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "giftSubscription throwable=" + throwable);
                    }
                });
    }


    public void setReceiveGift(boolean receiveGift) {
        mReceiveGift = receiveGift;
    }
}
