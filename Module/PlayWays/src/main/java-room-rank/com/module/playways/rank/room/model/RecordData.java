package com.module.playways.rank.room.model;

import java.util.List;

public class RecordData {
    public List<VoteInfoModel> mVoteInfoModels;
    public List<UserScoreModel> mUserScoreModels;

    public RecordData(List<VoteInfoModel> mVoteInfoModels, List<UserScoreModel> mUserScoreModels) {
        this.mVoteInfoModels = mVoteInfoModels;
        this.mUserScoreModels = mUserScoreModels;
    }
}
