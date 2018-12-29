package com.module.playways.rank.room.model;

import com.common.core.myinfo.MyUserInfoManager;

import java.util.List;

public class RecordData {
    public List<VoteInfoModel> mVoteInfoModels;
    public List<UserScoreModel> mUserScoreModels;

    public RecordData(List<VoteInfoModel> mVoteInfoModels, List<UserScoreModel> mUserScoreModels) {
        this.mVoteInfoModels = mVoteInfoModels;
        this.mUserScoreModels = mUserScoreModels;
    }

    public VoteInfoModel getSelfVoteInfoModel(){
        for (VoteInfoModel voteInfoModel :
                mVoteInfoModels) {
            if (voteInfoModel.getUserID() == MyUserInfoManager.getInstance().getUid()){
                return voteInfoModel;
            }
        }

        return null;
    }

}
