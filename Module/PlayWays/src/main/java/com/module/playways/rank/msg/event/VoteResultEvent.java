package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.room.model.WinResultModel;
import com.module.playways.rank.room.model.score.ScoreResultModel;
import com.module.playways.rank.room.model.VoteInfoModel;

import java.util.List;

public class VoteResultEvent {

    public BasePushInfo mBasePushInfo;
    public List<VoteInfoModel> mVoteInfoModels;
    public ScoreResultModel mScoreResultModel;
    public List<WinResultModel> mWinResultModels;

    public VoteResultEvent(BasePushInfo mBasePushInfo, List<VoteInfoModel> mVoteInfoModels, ScoreResultModel scoreResultModel,
                           List<WinResultModel> winResultModels) {
        this.mBasePushInfo = mBasePushInfo;
        this.mVoteInfoModels = mVoteInfoModels;
        this.mScoreResultModel = scoreResultModel;
        this.mWinResultModels = winResultModels;
    }
}
