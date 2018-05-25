package com.wali.live.common.gift.view;


import com.mi.live.data.gift.model.GiftRecvModel;

/**
 * Created by yangjiawei on 2017/8/7.
 */

public interface IGiftModelQueue {

    GiftRecvModel tryNextModel(GiftRecvModel model);

    int getBatchSize();

    void offer(GiftRecvModel model);

    GiftRecvModel poll();

    GiftRecvModel top();

    void clear();

    int size();

    int batchGiftSize();

    GiftRecvModel nonThisModel(GiftRecvModel model);

}
