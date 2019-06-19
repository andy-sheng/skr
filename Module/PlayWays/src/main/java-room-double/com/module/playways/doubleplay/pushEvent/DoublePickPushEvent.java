package com.module.playways.doubleplay.pushEvent;

import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.CombineRoom.PickMsg;

public class DoublePickPushEvent {
    BasePushInfo basePushInfo;
    int fromPickUserID;
    int toPickUserID;

    public DoublePickPushEvent(BasePushInfo basePushInfo, PickMsg pickMsg) {
        this.basePushInfo = basePushInfo;
        this.fromPickUserID = pickMsg.getFromPickUserID();
        this.toPickUserID = pickMsg.getToPickUserID();
    }
}
