package com.module.playways.room.msg.event;

import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.Room.OnlineInfo;
import com.zq.live.proto.Room.QJoinNoticeMsg;

import java.util.ArrayList;
import java.util.List;

public class QJoinNoticeEvent {
    public BasePushInfo info;
    public GrabPlayerInfoModel infoModel;
    public List<GrabPlayerInfoModel> waitUsers;
    public int roundSeq;

    public QJoinNoticeEvent(BasePushInfo info, QJoinNoticeMsg msg) {
        this.info = info;
        this.infoModel = GrabPlayerInfoModel.parse(msg);
        this.roundSeq = msg.getRoundSeq();
        this.waitUsers = new ArrayList<>();
        for(OnlineInfo onlineInfo : msg.getWaitUsersList()){
            this.waitUsers.add(GrabPlayerInfoModel.parse(onlineInfo));
        }
    }
}
