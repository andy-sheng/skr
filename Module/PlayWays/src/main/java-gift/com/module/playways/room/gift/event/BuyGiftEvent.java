package com.module.playways.room.gift.event;

import com.module.playways.room.gift.model.BaseGift;

public class BuyGiftEvent {
    BaseGift mBaseGift;

    long mReceiverId;

    public BuyGiftEvent(BaseGift baseGift, long receiverId) {
        mBaseGift = baseGift;
        mReceiverId = receiverId;
    }

    public long getReceiverId() {
        return mReceiverId;
    }

    public BaseGift getBaseGift() {
        return mBaseGift;
    }
}
