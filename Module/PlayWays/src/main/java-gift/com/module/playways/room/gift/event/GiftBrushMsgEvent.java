package com.module.playways.room.gift.event;

import com.module.playways.room.gift.model.GPrensentGiftMsgModel;

public class GiftBrushMsgEvent {
    GPrensentGiftMsgModel mGPrensentGiftMsg;

    public GiftBrushMsgEvent(GPrensentGiftMsgModel gPrensentGiftMsg) {
        this.mGPrensentGiftMsg = gPrensentGiftMsg;
    }

    public GPrensentGiftMsgModel getGPrensentGiftMsg() {
        return mGPrensentGiftMsg;
    }
}
