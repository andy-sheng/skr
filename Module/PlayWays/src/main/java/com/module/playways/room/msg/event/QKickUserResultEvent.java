package com.module.playways.room.msg.event;

import com.module.playways.room.msg.BasePushInfo;
import com.component.live.proto.Room.QKickUserResultMsg;

import java.util.List;

public class QKickUserResultEvent {
    public BasePushInfo mBasePushInfo;

    public int sourceUserID;
    public int kickUserID;
    public List<Integer> giveYesVoteUserIDs;
    public List<Integer> giveNoVoteUserIDs;
    public List<Integer> giveUnknownVoteUserIDs;
    public String kickResultContent;
    public boolean isKickSuccess;
    public int kickFailedReason;

    public QKickUserResultEvent(BasePushInfo info, QKickUserResultMsg qKickUserResultMsg) {
        this.mBasePushInfo = info;
        this.sourceUserID = qKickUserResultMsg.getSourceUserID();
        this.kickUserID = qKickUserResultMsg.getKickUserID();
        this.isKickSuccess = qKickUserResultMsg.getIsKickSuccess();
        this.kickFailedReason = qKickUserResultMsg.getKickFailedReason().getValue();
        this.giveYesVoteUserIDs = qKickUserResultMsg.getGiveYesVoteUserIDsList();
        this.giveNoVoteUserIDs = qKickUserResultMsg.getGiveNoVoteUserIDsList();
        this.giveUnknownVoteUserIDs = qKickUserResultMsg.getGiveUnknownVoteUserIDsList();
        this.kickResultContent = qKickUserResultMsg.getKickResultContent();
    }

    @Override
    public String toString() {
        return "QKickUserResultEvent{" +
                "mBasePushInfo=" + mBasePushInfo +
                ", sourceUserID=" + sourceUserID +
                ", kickUserID=" + kickUserID +
                ", giveYesVoteUserIDs=" + giveYesVoteUserIDs +
                ", giveNoVoteUserIDs=" + giveNoVoteUserIDs +
                ", giveUnknownVoteUserIDs=" + giveUnknownVoteUserIDs +
                ", kickResultContent='" + kickResultContent + '\'' +
                ", isKickSuccess=" + isKickSuccess +
                ", kickFailedReason=" + kickFailedReason +
                '}';
    }
}
