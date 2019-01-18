package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.zq.live.proto.Room.QExitGameMsg;

public class QExitGameMsgEvent {
    public BasePushInfo info;
    public Integer userID;

    public QExitGameMsgEvent(BasePushInfo info, QExitGameMsg qExitGameMsg) {
        this.info = info;
        this.userID = qExitGameMsg.getUserID();
    }

    public BasePushInfo getInfo() {
        return info;
    }

    public Integer getUserID() {
        return userID;
    }
}
