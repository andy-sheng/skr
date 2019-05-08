package com.module.playways.room.msg.event;

import com.module.playways.room.room.gift.model.GiftPlayModel;

public class OverlayGiftBrushMsgEvent {
    GiftPlayModel mGiftPlayModel;

    public OverlayGiftBrushMsgEvent(GiftPlayModel giftPlayModel) {
        mGiftPlayModel = giftPlayModel;
    }

    public GiftPlayModel getGiftPlayModel() {
        return mGiftPlayModel;
    }
}
