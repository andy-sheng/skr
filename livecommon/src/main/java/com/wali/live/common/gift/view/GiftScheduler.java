package com.wali.live.common.gift.view;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.gift.model.GiftRecvModel;
import com.wali.live.event.EventClass;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by yangjiawei on 2017/8/7.
 */

public class GiftScheduler implements IGiftScheduler {
    private static final String TAG = "GiftScheduler";

    private IGiftModelQueue mQueue = new GiftModelQueue();
    private List<GiftContinuousView> views;
    private ExecutorService singleThreadForBuyGift;

    public GiftScheduler() {
        EventBus.getDefault().register(this);
    }

    private boolean isPlayingModel(GiftContinuousView excludeView, GiftRecvModel model) {
        if (views == null) {
            return false;
        }
        for (GiftContinuousView v : views) {
            if (v != excludeView && v.isPlayingModel(model)) {
                return true;
            }
        }
        return false;
    }

    //试图获取当前continueId的下一个model
    @Override
    public synchronized GiftRecvModel tryGetNextModel(GiftRecvModel model, GiftContinuousView view) {
        //横竖屏切换时会去除上面的view，防止遮挡运营位
        if (!views.contains(view)) {
            return null;
        }

        //队列里面有组合礼物，则停止播放普通礼物
        if (!model.isBatchGift() && mQueue.batchGiftSize() > 0) {
            return null;
        }

        //队列里有其他组合礼物，则正在播放的组合礼物只能占据其主id轨道
        if (mQueue.batchGiftSize() > 1 && model.isBatchGift() && model.getMainOrbitId() != view.getMyId()) {
            return null;
        }

        //提高本地礼物的优先级,本地礼物插队逻辑
        GiftRecvModel top = mQueue.top();
        if (!model.isFromSelf() && top != null && top.isFromSelf() && !isPlayingModel(view, top)) {
            //队列里有未播放的本地组合礼物，则停掉正在播放的非本地礼物
            if (top.isBatchGift()) {
                return null;
            }

            //队列里有未播放的本地普通礼物，则停掉正在播放的非本地普通礼物
            if (!model.isBatchGift() && !top.isBatchGift()) {
                return null;
            }
        }

        return mQueue.tryNextModel(model);
    }


    @Override
    public synchronized GiftRecvModel nextModel(GiftContinuousView view) {
        //横竖屏切换时会去除上面的view，防止遮挡运营位
        if (!views.contains(view)) {
            return null;
        }

        GiftRecvModel data = mQueue.poll();

        //避免普通礼物占据多个轨道
        if (data != null && !data.isBatchGift() && isPlayingModel(view, data)) {
            mQueue.offer(data);
            data = mQueue.nonThisModel(data);
        }

        //队列中组合礼物数目大于1，则一个组合礼物只能占据一个轨道
        if (data != null && data.isBatchGift() && mQueue.batchGiftSize() > 1 && isPlayingModel(view, data)) {
            mQueue.offer(data);
            data = mQueue.nonThisModel(data);
        }

        //将开始播放一个组合礼物的轨道ID设置为该model的主轨道id
        if (data != null && data.isBatchGift()) {
            boolean isPlaying = false;
            for (GiftContinuousView v : views) {
                if (v != view && v.isPlayingModel(data)) {
                    isPlaying = true;
                    data.setMainOrbitId(v.getModel().getMainOrbitId());
                    break;
                }
            }
            if (!isPlaying) {
                data.setMainOrbitId(view.getMyId());
            }
        }

        return data;
    }

    @Override
    public void setGiftContinuousViews(List<GiftContinuousView> views) {
        this.views = views;
        for (GiftContinuousView view : views) {
            view.setGiftScheduler(this);
        }
    }

    @Override
    public ExecutorService getSingleThreadForBuyGift() {
        if (singleThreadForBuyGift == null) {
            singleThreadForBuyGift = Executors.newFixedThreadPool(1);
        }
        return singleThreadForBuyGift;
    }

    @Override
    public int getQueueSize() {
        return mQueue.size();
    }

    @Override
    public void onDestroy() {
        if (singleThreadForBuyGift != null) {
            singleThreadForBuyGift.shutdown();
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void clearQueue() {
        mQueue.clear();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(GiftEventClass.GiftAttrMessage.Normal event) {
        Observable.just((GiftRecvModel) event.obj1)
                .throttleFirst(50, TimeUnit.MILLISECONDS)
                .filter(new Func1<GiftRecvModel, Boolean>() {
                    @Override
                    public Boolean call(GiftRecvModel giftRecvModel) {
                        if (giftRecvModel != null && !TextUtils.isEmpty(giftRecvModel.getPicPath())) {
                            return true;
                        }
                        return false;
                    }
                })
                .subscribeOn(Schedulers.from(getSingleThreadForBuyGift()))
                .subscribe(new Observer<GiftRecvModel>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.w(TAG, e.getMessage());
                    }

                    @Override
                    public void onNext(GiftRecvModel giftRecvModel) {
                        mQueue.offer(giftRecvModel);
                        for (GiftContinuousView view : views) {
                            view.tryAwake();
                        }
                    }
                });
    }


}
