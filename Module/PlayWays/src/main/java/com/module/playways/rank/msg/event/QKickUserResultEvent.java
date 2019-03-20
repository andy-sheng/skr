package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.zq.live.proto.Room.QKickUserResultMsg;

import java.util.List;

public class QKickUserResultEvent {
    public BasePushInfo mBasePushInfo;

    public int sourceUserID;
    public int kickUserID;
    public List<Integer> giveYesVoteUserIDs;
    public List<Integer> giveNoVoteUserIDs;
    public List<Integer> giveUnknownVoteUserIDs;
    public String kickResultContent;

    public QKickUserResultEvent(BasePushInfo info, QKickUserResultMsg qKickUserResultMsg) {
        this.mBasePushInfo = info;
        this.sourceUserID = qKickUserResultMsg.getSourceUserID();
        this.kickUserID = qKickUserResultMsg.getKickUserID();
        this.giveYesVoteUserIDs = qKickUserResultMsg.getGiveYesVoteUserIDsList();
        this.giveNoVoteUserIDs = qKickUserResultMsg.getGiveNoVoteUserIDsList();
        this.giveUnknownVoteUserIDs = qKickUserResultMsg.getGiveUnknownVoteUserIDsList();
        this.kickResultContent = qKickUserResultMsg.getKickResultContent();
    }
}
