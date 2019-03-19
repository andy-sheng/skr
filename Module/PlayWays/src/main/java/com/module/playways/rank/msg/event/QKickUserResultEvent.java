package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.zq.live.proto.Room.QKickUserSuccessMsg;

import java.util.List;

public class QKickUserResultEvent {
    public BasePushInfo mBasePushInfo;
    public int sourceUserID;
    public int kickUserID;
    public List<Integer> voteUserIDs;
    public String kickSuccessMsg;

    public QKickUserResultEvent(BasePushInfo info, QKickUserSuccessMsg qKickUserSuccessMsg) {
        this.mBasePushInfo = info;
        this.sourceUserID = qKickUserSuccessMsg.getSourceUserID();
        this.kickUserID = qKickUserSuccessMsg.getKickUserID();
        this.voteUserIDs = qKickUserSuccessMsg.getVoteUserIDsList();
        this.kickSuccessMsg = qKickUserSuccessMsg.getKickSuccessMsg();
    }
}
