package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.zq.live.proto.Room.QKickUserRequestMsg;

import java.util.List;

public class QKickUserReqEvent {

    public BasePushInfo info;
    public int sourceUserID;
    public int kickUserID;
    public List<Integer> otherOnlineUserIDs;

    public QKickUserReqEvent(BasePushInfo info, QKickUserRequestMsg qKickUserRequestMsg) {
        this.info = info;
        this.sourceUserID = qKickUserRequestMsg.getSourceUserID();
        this.kickUserID = qKickUserRequestMsg.getKickUserID();
        this.otherOnlineUserIDs = qKickUserRequestMsg.getOtherOnlineUserIDsList();
    }

    @Override
    public String toString() {
        return "QKickUserReqEvent{" +
                "info=" + info +
                ", sourceUserID=" + sourceUserID +
                ", kickUserID=" + kickUserID +
                ", otherOnlineUserIDs=" + otherOnlineUserIDs +
                '}';
    }
}
