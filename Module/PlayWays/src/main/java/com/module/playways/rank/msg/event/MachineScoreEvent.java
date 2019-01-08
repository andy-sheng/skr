package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.room.model.UserScoreModel;
import com.module.playways.rank.room.model.VoteInfoModel;

import java.util.List;

public class MachineScoreEvent {

    public BasePushInfo mBasePushInfo;
    public int userId;
    public int lineNo;
    public int score;

    public MachineScoreEvent(BasePushInfo mBasePushInfo, int userId, int lineNo, int score) {
        this.mBasePushInfo = mBasePushInfo;
        this.userId = userId;
        this.lineNo = lineNo;
        this.score = score;
    }
}
