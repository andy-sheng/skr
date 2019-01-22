package com.module.playways.rank.msg.event;


import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.room.model.VoteInfoModel;
import com.module.playways.rank.room.model.WinResultModel;
import com.module.playways.rank.room.model.score.ScoreResultModel;

import java.util.List;

public class RoundAndGameOverEvent {

    public long roundOverTimeMs;
    public BasePushInfo info;
    public List<VoteInfoModel> mVoteInfoModels;
    public ScoreResultModel mScoreResultModel;
    public List<WinResultModel> mWinResultModels;

    public RoundAndGameOverEvent(BasePushInfo info, long roundOverTimeMs, List<VoteInfoModel> mVoteInfoModels, ScoreResultModel scoreResultModel, List<WinResultModel> winResultModels) {
        this.info = info;
        this.roundOverTimeMs = roundOverTimeMs;
        this.mVoteInfoModels = mVoteInfoModels;
        this.mScoreResultModel = scoreResultModel;
        this.mWinResultModels = winResultModels;
    }
}
