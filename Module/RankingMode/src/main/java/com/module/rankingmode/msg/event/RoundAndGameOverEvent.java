package com.module.rankingmode.msg.event;


import com.module.rankingmode.msg.BasePushInfo;
import com.module.rankingmode.room.model.UserScoreModel;
import com.module.rankingmode.room.model.VoteInfoModel;

import java.util.List;

public class RoundAndGameOverEvent {

    public long roundOverTimeMs;
    public BasePushInfo info;
    public List<VoteInfoModel> mVoteInfoModels;
    public List<UserScoreModel> mUserScoreModels;

    public RoundAndGameOverEvent(BasePushInfo info, long roundOverTimeMs, List<VoteInfoModel> mVoteInfoModels, List<UserScoreModel> mUserScoreModels) {
        this.info = info;
        this.roundOverTimeMs = roundOverTimeMs;
        this.mVoteInfoModels = mVoteInfoModels;
        this.mUserScoreModels = mUserScoreModels;
    }
}
