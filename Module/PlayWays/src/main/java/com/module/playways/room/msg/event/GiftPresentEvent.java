package com.module.playways.room.msg.event;

import com.module.playways.room.gift.model.GPrensentGiftMsgModel;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.Room.GPrensentGiftMsg;

public class GiftPresentEvent {
    public BasePushInfo info;
    public GPrensentGiftMsgModel mGPrensentGiftMsgModel;

    public GiftPresentEvent(BasePushInfo info, GPrensentGiftMsg msg) {
        this.info = info;
        this.mGPrensentGiftMsgModel = GPrensentGiftMsgModel.parse(msg);
    }

    public GiftPresentEvent(BasePushInfo info, GPrensentGiftMsgModel msg) {
        this.info = info;
        this.mGPrensentGiftMsgModel = msg;
    }
}
