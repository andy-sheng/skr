package com.module.playways.room.gift.event;

import com.zq.live.proto.Room.GPrensentGiftMsg;

/**
 * 一般为小礼物
 */
public class GiftBrushMsgEvent {
    GPrensentGiftMsg mGPrensentGiftMsg;

    public GiftBrushMsgEvent(GPrensentGiftMsg gPrensentGiftMsg) {
        this.mGPrensentGiftMsg = gPrensentGiftMsg;
    }

    public GPrensentGiftMsg getGPrensentGiftMsg() {
        return mGPrensentGiftMsg;
    }
}
