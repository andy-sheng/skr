package com.module.playways.rank.msg.event;


import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.room.scoremodel.ScoreDetailModel;
import com.module.playways.rank.room.scoremodel.UserScoreModel;
import com.module.playways.rank.room.model.VoteInfoModel;

import java.util.List;

public class RoundAndGameOverEvent {

    public long roundOverTimeMs;
    public BasePushInfo info;
    public List<VoteInfoModel> mVoteInfoModels;
    public ScoreDetailModel mScoreDetailModel;

    public RoundAndGameOverEvent(BasePushInfo info, long roundOverTimeMs, List<VoteInfoModel> mVoteInfoModels, ScoreDetailModel mScoreDetailModel) {
        this.info = info;
        this.roundOverTimeMs = roundOverTimeMs;
        this.mVoteInfoModels = mVoteInfoModels;
        this.mScoreDetailModel = mScoreDetailModel;
    }
}
