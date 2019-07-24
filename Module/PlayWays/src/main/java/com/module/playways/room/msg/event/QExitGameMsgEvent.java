package com.module.playways.room.msg.event;

import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.Room.QExitGameMsg;

public class QExitGameMsgEvent {
    public BasePushInfo info;
    public Integer userID;
    public Integer roundSeq;
    public QExitGameMsgEvent(BasePushInfo info, QExitGameMsg qExitGameMsg) {
        this.info = info;
        this.userID = qExitGameMsg.getUserID();
        this.roundSeq = qExitGameMsg.getRoundSeq();
    }
}
