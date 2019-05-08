package com.module.playways.room.msg.event;

import com.zq.live.proto.Room.GPrensentGiftMsg;

public class BigGiftBrushMsgEvent {
    GPrensentGiftMsg mGPrensentGiftMsg;

    public BigGiftBrushMsgEvent(GPrensentGiftMsg gPrensentGiftMsg) {
        this.mGPrensentGiftMsg = gPrensentGiftMsg;
    }

    public GPrensentGiftMsg getGPrensentGiftMsg() {
        return mGPrensentGiftMsg;
    }
}
