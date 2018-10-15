package com.wali.live.common.gift.view;


import com.mi.live.data.gift.model.GiftRecvModel;

/**
 * Created by yangjiawei on 2017/8/7.
 */

//礼物连送View对应的接口
public interface IGiftContinueView {
    enum STATUS{
        IDLE,ENTERING,PLAYING,WAITING,DISMISSING
    }

    void setStatus(STATUS status);

    STATUS getStatus();

    void tryAwake();

    void setGiftScheduler(IGiftScheduler scheduler);

    boolean playingBatchGift();

    GiftRecvModel getModel();

    void switchAnchor();

    void onDestroy();

    boolean isPlayingModel(GiftRecvModel model);

}
