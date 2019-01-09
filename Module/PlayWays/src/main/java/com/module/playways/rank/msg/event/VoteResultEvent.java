package com.module.playways.rank.msg.event;

import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.room.scoremodel.UserScoreModel;
import com.module.playways.rank.room.model.VoteInfoModel;

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
