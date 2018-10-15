package com.wali.live.common.gift.view;

import com.mi.live.data.gift.model.GiftRecvModel;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by yangjiawei on 2017/8/7.
 */

public interface IGiftScheduler {

    GiftRecvModel tryGetNextModel(GiftRecvModel model, GiftContinuousView view);

    GiftRecvModel nextModel(GiftContinuousView view);

    void setGiftContinuousViews(List<GiftContinuousView> views);

    ExecutorService getSingleThreadForBuyGift();

    int getQueueSize();

    void onDestroy();

    void clearQueue();
}
