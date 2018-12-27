package com.module.rankingmode.msg.event;

import com.module.rankingmode.msg.BasePushInfo;
import com.module.rankingmode.room.model.UserScoreModel;
import com.module.rankingmode.room.model.VoteInfoModel;

import java.util.List;

public class VoteResultEvent {

    public BasePushInfo mBasePushInfo;
    public List<VoteInfoModel> mVoteInfoModels;
    public List<UserScoreModel> mUserScoreModels;

    public VoteResultEvent(BasePushInfo mBasePushInfo, List<VoteInfoModel> mVoteInfoModels, List<UserScoreModel> mUserScoreModels) {
        this.mBasePushInfo = mBasePushInfo;
        this.mVoteInfoModels = mVoteInfoModels;
        this.mUserScoreModels = mUserScoreModels;
    }
}
