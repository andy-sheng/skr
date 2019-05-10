package com.module.playways.room.gift.event;

import com.module.playways.room.gift.model.GPrensentGiftMsgModel;

/**
 * 一般为大礼物
 */
public class BigGiftMsgEvent {
    GPrensentGiftMsgModel mGPrensentGiftMsgModel;

    public BigGiftMsgEvent(GPrensentGiftMsgModel gPrensentGiftMsg) {
        this.mGPrensentGiftMsgModel = gPrensentGiftMsg;
    }

    public GPrensentGiftMsgModel getGPrensentGiftMsgModel() {
        return mGPrensentGiftMsgModel;
    }
}
