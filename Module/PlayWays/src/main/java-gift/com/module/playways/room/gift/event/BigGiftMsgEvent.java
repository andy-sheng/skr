package com.module.playways.room.gift.event;

import com.module.playways.room.room.gift.model.GiftPlayModel;

/**
 * 一般为大礼物
 */
public class BigGiftMsgEvent {

    GiftPlayModel mGiftPlayModel;

    public BigGiftMsgEvent(GiftPlayModel giftPlayModel) {
        this.mGiftPlayModel = giftPlayModel;
    }

    public GiftPlayModel getGiftPlayModel() {
        return mGiftPlayModel;
    }
}
