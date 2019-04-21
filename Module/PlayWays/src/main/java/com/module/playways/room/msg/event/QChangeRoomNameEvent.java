package com.module.playways.room.msg.event;

import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.Room.QChangeRoomName;

public class QChangeRoomNameEvent {
    public BasePushInfo info;
    public String newName;

    public QChangeRoomNameEvent(BasePushInfo info, QChangeRoomName msg) {
        this.info = info;
        this.newName = msg.getNewRoomName();
    }
}
