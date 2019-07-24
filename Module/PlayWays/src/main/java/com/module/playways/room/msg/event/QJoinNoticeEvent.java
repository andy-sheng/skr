package com.module.playways.room.msg.event;

import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.room.msg.BasePushInfo;
import com.component.live.proto.Room.QJoinNoticeMsg;

public class QJoinNoticeEvent {
    public BasePushInfo info;
    public GrabPlayerInfoModel infoModel;
    public int roundSeq;

    public QJoinNoticeEvent(BasePushInfo info, QJoinNoticeMsg msg) {
        this.info = info;
        this.infoModel = GrabPlayerInfoModel.parse(msg);
        this.roundSeq = msg.getRoundSeq();
    }
}
