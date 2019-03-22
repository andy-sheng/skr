package com.module.playways.rank.room.gift.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.Pair;

import com.common.log.MyLog;
import com.common.utils.CustomHandlerThread;
import com.module.playways.rank.room.gift.GiftContinuousView;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 循环播放动画时的控制模板类，封装点亮、背景、大礼物等播放的基本队列逻辑
 * Created by chengsimin on 16/6/17.
 */
public abstract class GiftPlayControlTemplate {
    public static final String GiftPlayModelAG = "AnimationPlayControlGiftPlayModelemplate";

    static final int MSG_START_ON_UI = 80;

    static final int MSG_END_ON_UI = 81;

    public static final int SIZE = 100;//生产者池子里最多多少个

    /**
     * 播放动画队列
     */
    private LinkedHashMap<String, GiftPlayModel> mQueueMap = new LinkedHashMap<>();

    CustomHandlerThread mHandlerGiftPlayModelhread;// 保证++ --  都在后台线程操作

    Handler mUiHandler;


    public GiftPlayControlTemplate() {
        mHandlerGiftPlayModelhread = new CustomHandlerThread("my-queue-thread") {
            @Override
            protected void processMessage(Message var1) {

            }
        };
        mUiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_START_ON_UI:
                        Pair<GiftPlayModel, GiftContinuousView> pair = (Pair<GiftPlayModel, GiftContinuousView>) msg.obj;
                        onStart(pair.first, pair.second);
                        break;
                    case MSG_END_ON_UI:
                        onEnd((GiftPlayModel) msg.obj);
                        break;
                }
            }
        };
    }

    public void add(GiftPlayModel model, boolean must) {
        mHandlerGiftPlayModelhread.post(new Runnable() {
            @Override
            public void run() {
                // 加入队列
                String key = getKey(model);
                GiftPlayModel giftPlayModel = mQueueMap.get(key);
                if (giftPlayModel != null) {
                    if (model.getBeginCount() < giftPlayModel.getBeginCount()) {
                        giftPlayModel.setBeginCount(model.getBeginCount());
                    }
                    if (model.getEndCount() > giftPlayModel.getEndCount()) {
                        giftPlayModel.setEndCount(model.getEndCount());
                    }
                } else {
                    if (mQueueMap.size() < SIZE || must) {
                        mQueueMap.put(key, model);
                    }
                }
                play();
            }
        });
    }

    private GiftPlayModel peek() {
        Iterator iterator = mQueueMap.entrySet().iterator();
        GiftPlayModel model = null;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            model = (GiftPlayModel) entry.getValue();
            return model;
        }
        return null;
    }

    private String getKey(GiftPlayModel giftPlayModel) {
        return giftPlayModel.getContinueId() + "_" + giftPlayModel.getSender().getUserId();
    }

    private void play() {
        GiftPlayModel cur = peek();
        if (cur == null) {
            return;
        }
        GiftContinuousView consumer = accept(cur);
        if (consumer != null) {
            // 肯定有消费者，才会走到这
            mQueueMap.remove(getKey(cur));
            if (cur != null) {
                //取出来一个
                processInBackGround(cur);
                onStartInside(cur, consumer);
            }
        }
    }

    /**
     * 确保在主线程执行
     *
     * @param model
     */
    private void onStartInside(GiftPlayModel model, GiftContinuousView consumer) {
        MyLog.d(GiftPlayModelAG, "onStartInside model:" + model);
        Message msg = mUiHandler.obtainMessage(MSG_START_ON_UI);
        msg.obj = new Pair<>(model, consumer);
        mUiHandler.sendMessage(msg);
    }

    /**
     * 重要，每次消费完，请手动调用告知
     * 确保主线程执行
     *
     * @param model
     */
    public void endCurrent(GiftPlayModel model) {
        Message msg = mUiHandler.obtainMessage(MSG_END_ON_UI);
        msg.obj = model;
        mUiHandler.sendMessage(msg);

        mHandlerGiftPlayModelhread.post(new Runnable() {
            @Override
            public void run() {
                onEndInSide(model);
            }
        });
    }

    private void onEndInSide(GiftPlayModel model) {
        MyLog.d(GiftPlayModelAG, "onEndInSide model:" + model);
        play();
    }

    /**
     * 复位
     */
    public synchronized void reset() {
        mQueueMap.clear();
    }

    /**
     * 复位
     */
    public synchronized void destroy() {
        mQueueMap.clear();
        if (mUiHandler != null) {
            mUiHandler.removeCallbacksAndMessages(null);
        }
        mHandlerGiftPlayModelhread.destroy();
    }


    /**
     * 是否接受这个播放对象
     *
     * @param cur
     * @return
     */
    protected abstract GiftContinuousView accept(GiftPlayModel cur);

    /**
     * 某次动画开始时执行
     *
     * @param model
     */
    public abstract void onStart(GiftPlayModel model, GiftContinuousView consumer);

    /**
     * 某次动画结束了执行
     *
     * @param model
     */
    protected abstract void onEnd(GiftPlayModel model);

    protected void processInBackGround(GiftPlayModel model) {

    }

    /**
     * 队列这是否还有
     *
     * @return
     */
    public synchronized boolean hasMoreData() {
        return !mQueueMap.isEmpty();
    }

}
