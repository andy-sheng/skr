package com.module.playways.room.msg.event;

import com.module.playways.room.room.gift.model.GiftPlayModel;

public class BigGiftBrushMsgEvent {
    GiftPlayModel mGiftPlayModel;

    public BigGiftBrushMsgEvent(GiftPlayModel giftPlayModel) {
        mGiftPlayModel = giftPlayModel;
    }

    public GiftPlayModel getGiftPlayModel() {
        return mGiftPlayModel;
    }
}
