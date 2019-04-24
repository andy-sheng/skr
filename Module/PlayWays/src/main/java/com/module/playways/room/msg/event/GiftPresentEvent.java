package com.module.playways.room.msg.event;

import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.Room.GPrensentGiftMsg;

public class GiftPresentEvent {
    public BasePushInfo info;
    public GPrensentGiftMsg mGPrensentGiftMsg;

    public GiftPresentEvent(BasePushInfo info, GPrensentGiftMsg msg) {
        this.info = info;
        this.mGPrensentGiftMsg = msg;
    }
}
