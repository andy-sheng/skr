package com.module.playways.doubleplay.pushEvent;

import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.CombineRoom.EndCombineRoomMsg;

public class DoubleEndCombineRoomPushEvent {
    BasePushInfo basePushInfo;

    int roomID; //房间ID
    int reason; //关闭原因
    int exitUserID;
    int noResponseUserID;

    public BasePushInfo getBasePushInfo() {
        return basePushInfo;
    }

    public int getRoomID() {
        return roomID;
    }

    public int getReason() {
        return reason;
    }

    public int getExitUserID() {
        return exitUserID;
    }

    public int getNoResponseUserID() {
        return noResponseUserID;
    }

    public DoubleEndCombineRoomPushEvent(BasePushInfo basePushInfo, EndCombineRoomMsg endCombineRoomMsg) {
        this.basePushInfo = basePushInfo;
        this.roomID = endCombineRoomMsg.getRoomID();
        this.reason = endCombineRoomMsg.getReason().getValue();
        this.exitUserID = endCombineRoomMsg.getExitUserID();
        this.noResponseUserID = endCombineRoomMsg.getNoResponseUserID();
    }
}
