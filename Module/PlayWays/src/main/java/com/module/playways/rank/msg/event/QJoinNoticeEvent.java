package com.module.playways.rank.msg.event;

import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.rank.msg.BasePushInfo;
import com.zq.live.proto.Room.QJoinNoticeMsg;

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
