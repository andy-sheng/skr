package com.module.playways.room.gift.event;

import com.module.playways.room.room.gift.model.GiftPlayModel;

/**
 * 一般为中礼物
 */
public class OverlayGiftBrushMsgEvent {
    GiftPlayModel mGiftPlayModel;

    public OverlayGiftBrushMsgEvent(GiftPlayModel giftPlayModel) {
        mGiftPlayModel = giftPlayModel;
    }

    public GiftPlayModel getGiftPlayModel() {
        return mGiftPlayModel;
    }
}
