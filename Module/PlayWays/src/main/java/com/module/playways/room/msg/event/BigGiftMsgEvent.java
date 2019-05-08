package com.module.playways.room.msg.event;

import com.zq.live.proto.Room.GPrensentGiftMsg;

public class BigGiftMsgEvent {
    GPrensentGiftMsg mGPrensentGiftMsg;

    public BigGiftMsgEvent(GPrensentGiftMsg gPrensentGiftMsg) {
        this.mGPrensentGiftMsg = gPrensentGiftMsg;
    }

    public GPrensentGiftMsg getGPrensentGiftMsg() {
        return mGPrensentGiftMsg;
    }
}
