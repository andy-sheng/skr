package com.module.playways.room.room.gift.model;

import android.os.Message;
import android.support.annotation.NonNull;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.CustomHandlerThread;
import com.module.playways.room.room.gift.GiftContinueViewGroup;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 循环播放动画时的控制模板类，封装点亮、背景、大礼物等播放的基本队列逻辑
 * Created by chengsimin on 16/6/17.
 */
public abstract class GiftPlayControlTemplate implements GiftContinueViewGroup.GiftProvider {
    public final static String TAG = "GiftPlayControlTemplate";

    public static final int MEDIUM_GIFT = 2;
    public static final int SMALL_GIFT = 3;
    public static final int FREE_GIFT = 4;

    public static final int SIZE = 100;//生产者池子里最多多少个

    private Object mQueueLock = new Object();

    /**
     * 播放动画队列
     */
    private LinkedHashMap<String, GiftPlayModel> mOwnerGiftMap = new LinkedHashMap<>();

    private LinkedHashMap<String, GiftPlayModel> mMediumGiftMap = new LinkedHashMap<>();

    private LinkedHashMap<String, GiftPlayModel> mSmallQueueMap = new LinkedHashMap<>();

    private LinkedHashMap<String, GiftPlayModel> mFreeQueueMap = new LinkedHashMap<>();

    //礼物可以切入，需要把已经赠送的数据保存起来
    private LinkedHashMap<String, Integer> mHasContinueCount = new LinkedHashMap<>();

    CustomHandlerThread mHandlerGiftPlayModelhread;// 保证++ --  都在后台线程操作

    public GiftPlayControlTemplate() {
        mHandlerGiftPlayModelhread = new CustomHandlerThread("my-queue-thread") {
            @Override
            protected void processMessage(Message var1) {

            }
        };
    }

    public void add(GiftPlayModel model, boolean must) {
        mHandlerGiftPlayModelhread.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mQueueLock) {
                    MyLog.d(TAG, "add " + model);
                    if (model.getSender().getUserId() == MyUserInfoManager.getInstance().getUid()) {
                        MyLog.d(TAG, "add owner");
                        updateOrPushGiftModel(mOwnerGiftMap, model, true);
                    } else {
                        switch (model.getGift().getDisplayType()) {
                            case MEDIUM_GIFT:
                                MyLog.d(TAG, "add mMediumGiftMap");
                                updateOrPushGiftModel(mMediumGiftMap, model, true);
                                break;
                            case SMALL_GIFT:
                                MyLog.d(TAG, "add mSmallQueueMap");
                                updateOrPushGiftModel(mSmallQueueMap, model, false);
                                break;
                            case FREE_GIFT:
                                MyLog.d(TAG, "add mFreeQueueMap");
                                updateOrPushGiftModel(mFreeQueueMap, model, false);
                                break;
                            default:
                                MyLog.e(TAG, "未知类型的礼物");
                                updateOrPushGiftModel(mFreeQueueMap, model, false);
                                break;
                        }
                    }
                }
            }
        });
    }

    private void updateOrPushGiftModel(LinkedHashMap<String, GiftPlayModel> linkedHashMap, GiftPlayModel model, boolean must) {
        String key = getKey(model);
        GiftPlayModel giftPlayModel = linkedHashMap.get(key);
        MyLog.d(TAG, "updateOrPushGiftModel" + " giftPlayModel=" + giftPlayModel);
        if (giftPlayModel != null) {
            MyLog.d(TAG, "updateOrPushGiftModel 1");
            if (model.getBeginCount() < giftPlayModel.getBeginCount()) {
                MyLog.d(TAG, "updateOrPushGiftModel 2");
                giftPlayModel.setBeginCount(model.getBeginCount());
            }
            if (model.getEndCount() > giftPlayModel.getEndCount()) {
                MyLog.d(TAG, "updateOrPushGiftModel 3");
                giftPlayModel.setEndCount(model.getEndCount());
            }
        } else {
            MyLog.d(TAG, "updateOrPushGiftModel 5");
            if (linkedHashMap.size() < SIZE || must) {
                MyLog.d(TAG, "updateOrPushGiftModel 6");
                linkedHashMap.put(key, model);
            }
        }

        needNotify();
    }

    private GiftPlayModel peek(LinkedHashMap<String, GiftPlayModel> linkedHashMap, int id) {
        Iterator iterator = linkedHashMap.entrySet().iterator();
        GiftPlayModel model = null;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            model = (GiftPlayModel) entry.getValue();
            if (!isGiftModelIsPlayingExpectOwer(model, id)) {
                return model;
            }
        }
        return null;
    }

    private String getKey(GiftPlayModel giftPlayModel) {
        if (giftPlayModel == null) {
            return "";
        }

        return giftPlayModel.getContinueId() + "_" + giftPlayModel.getSender().getUserId();
    }

    @Override
    public GiftPlayModel tryGetGiftModel(GiftPlayModel giftPlayModel, int curNum, int id) {
        synchronized (mQueueLock) {
            GiftPlayModel model = getNextPlayModel(giftPlayModel, curNum, id);
            if (model != null) {
                Integer curContinue = mHasContinueCount.get(getKey(model));
                if (curContinue != null) {
                    model.setBeginCount(curContinue.intValue() + 1);
                } else {
                    model.setBeginCount(1);
                }
            }
            return model;
        }

    }

    private void printQueueState(String name, LinkedHashMap<String, GiftPlayModel> linkedHashMap) {
        Iterator iterator = linkedHashMap.entrySet().iterator();
        GiftPlayModel model = null;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            model = (GiftPlayModel) entry.getValue();
            MyLog.d(TAG, name + " has model " + model);
        }
    }

    //获取最新的礼物数据
    private GiftPlayModel getNextPlayModel(GiftPlayModel giftPlayModel, int curNum, int id) {

        if (giftPlayModel == null) {
            /**
             * giftPlayModel == null说明是IDLE的状态，从最先优先级取一个数据
             */
            if (!mOwnerGiftMap.isEmpty()) {
                return peek(mOwnerGiftMap, id);
            } else if (!mMediumGiftMap.isEmpty()) {
                return peek(mMediumGiftMap, id);
            } else if (!mSmallQueueMap.isEmpty()) {
                return peek(mSmallQueueMap, id);
            } else if (!mFreeQueueMap.isEmpty()) {
                return peek(mFreeQueueMap, id);
            }
        } else {
            /**
             * 这是播放的状态中取数据的，从最高优先级开始往下找
             */
            if (mHasContinueCount.size() > 30) {
                Iterator iterator = mHasContinueCount.entrySet().iterator();
                while (iterator.hasNext()) {
                    iterator.remove();
                }
            }

            mHasContinueCount.put(getKey(giftPlayModel), curNum);

            GiftPlayModel model = tryGetContinueGiftModel(mOwnerGiftMap, giftPlayModel, curNum, id);
            if (model != null) {
                return model;
            }

            model = tryGetContinueGiftModel(mMediumGiftMap, giftPlayModel, curNum, id);
            if (model != null) {
                return model;
            }

            model = tryGetContinueGiftModel(mSmallQueueMap, giftPlayModel, curNum, id);
            if (model != null) {
                return model;
            }

            model = tryGetContinueGiftModel(mFreeQueueMap, giftPlayModel, curNum, id);
            if (model != null) {
                return model;
            }
        }

        return null;
    }

    private GiftPlayModel tryGetContinueGiftModel(LinkedHashMap<String, GiftPlayModel> linkedHashMap, GiftPlayModel giftPlayModel, int curNum, int id) {
        GiftPlayModel model = linkedHashMap.get(getKey(giftPlayModel));

        if (model != null) {
            if (model.getEndCount() > curNum) {
                return linkedHashMap.get(getKey(giftPlayModel));
            } else {
                linkedHashMap.remove(getKey(giftPlayModel));
                return peek(linkedHashMap, id);
            }
        } else {
            return peek(linkedHashMap, id);
        }
    }

    /**
     * 复位
     */
    public synchronized void reset() {
        mOwnerGiftMap.clear();
        mMediumGiftMap.clear();
        mSmallQueueMap.clear();
        mFreeQueueMap.clear();
        mHasContinueCount.clear();
    }

    /**
     * 复位
     */
    public synchronized void destroy() {
        mOwnerGiftMap.clear();
        mMediumGiftMap.clear();
        mSmallQueueMap.clear();
        mFreeQueueMap.clear();
        mHasContinueCount.clear();
        mHandlerGiftPlayModelhread.destroy();
    }

    protected abstract void needNotify();

    protected abstract boolean isGiftModelIsPlayingExpectOwer(@NonNull GiftPlayModel giftPlayModel, int id);

    protected void processInBackGround(GiftPlayModel model) {

    }

    /**
     * 队列这是否还有
     *
     * @return
     */
    public synchronized boolean hasMoreData() {
        return !mOwnerGiftMap.isEmpty()
                || !mSmallQueueMap.isEmpty()
                || !mMediumGiftMap.isEmpty()
                || !mFreeQueueMap.isEmpty();
    }

}
