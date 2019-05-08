package com.module.playways.room.gift.event;

import com.zq.live.proto.Room.GPrensentGiftMsg;

/**
 * 一般为大礼物
 */
public class BigGiftMsgEvent {
    GPrensentGiftMsg mGPrensentGiftMsg;

    public BigGiftMsgEvent(GPrensentGiftMsg gPrensentGiftMsg) {
        this.mGPrensentGiftMsg = gPrensentGiftMsg;
    }

    public GPrensentGiftMsg getGPrensentGiftMsg() {
        return mGPrensentGiftMsg;
    }
}
