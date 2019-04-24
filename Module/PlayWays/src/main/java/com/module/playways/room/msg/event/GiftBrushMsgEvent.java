package com.module.playways.room.msg.event;

import com.zq.live.proto.Room.GPrensentGiftMsg;

public class GiftBrushMsgEvent {
    GPrensentGiftMsg mGPrensentGiftMsg;

    public GiftBrushMsgEvent(GPrensentGiftMsg gPrensentGiftMsg) {
        this.mGPrensentGiftMsg = gPrensentGiftMsg;
    }

    public GPrensentGiftMsg getGPrensentGiftMsg() {
        return mGPrensentGiftMsg;
    }
}
