package com.module.playways.room.gift.event;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.room.gift.model.BaseGift;

public class BuyGiftEvent {
    BaseGift mBaseGift;

    UserInfoModel mReceiver;

    public BuyGiftEvent(BaseGift baseGift, UserInfoModel receiver) {
        mBaseGift = baseGift;
        mReceiver = receiver;
    }

    public UserInfoModel getReceiver() {
        return mReceiver;
    }

    public BaseGift getBaseGift() {
        return mBaseGift;
    }
}
