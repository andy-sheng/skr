package com.module.playways.room.gift.event;

import com.module.playways.room.gift.model.GPrensentGiftMsgModel;

/**
 * 一般为小礼物
 */
public class GiftBrushMsgEvent {
    GPrensentGiftMsgModel mGPrensentGiftMsg;

    public GiftBrushMsgEvent(GPrensentGiftMsgModel gPrensentGiftMsg) {
        this.mGPrensentGiftMsg = gPrensentGiftMsg;
    }

    public GPrensentGiftMsgModel getGPrensentGiftMsg() {
        return mGPrensentGiftMsg;
    }
}
