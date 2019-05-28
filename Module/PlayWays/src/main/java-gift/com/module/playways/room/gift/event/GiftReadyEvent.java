package com.module.playways.room.gift.event;

public class GiftReadyEvent {
    //加载礼物成功与否
    boolean mIsGiftLoadSuccess = false;

    public GiftReadyEvent(boolean aBoolean) {
        mIsGiftLoadSuccess = aBoolean;
    }

    public boolean isGiftLoadSuccess() {
        return mIsGiftLoadSuccess;
    }
}
