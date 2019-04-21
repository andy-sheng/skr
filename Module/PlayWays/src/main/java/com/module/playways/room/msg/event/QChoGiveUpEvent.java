package com.module.playways.room.msg.event;

import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.Room.QCHOGiveUpMsg;
import com.zq.live.proto.Room.QJoinNoticeMsg;

public class QChoGiveUpEvent {
    public BasePushInfo info;
    public int userID;
    public int roundSeq;

    public QChoGiveUpEvent(BasePushInfo info, QCHOGiveUpMsg msg) {
        this.info = info;
        this.userID = msg.getUserID();
        this.roundSeq = msg.getRoundSeq();
    }
}
