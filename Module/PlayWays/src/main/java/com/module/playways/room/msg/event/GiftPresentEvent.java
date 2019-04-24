package com.module.playways.room.msg.event;

import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.Room.GPrensentGiftMsg;
import com.zq.live.proto.Room.QChangeRoomName;

public class GiftPresentEvent {
    public BasePushInfo info;
    public GPrensentGiftMsg getNewRoomName;

    public GiftPresentEvent(BasePushInfo info, GPrensentGiftMsg msg) {
        this.info = info;
        this.getNewRoomName = msg;
    }
}
