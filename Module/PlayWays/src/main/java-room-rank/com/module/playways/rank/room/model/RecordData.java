package com.module.playways.rank.room.model;

import com.common.core.myinfo.MyUserInfoManager;
import com.module.playways.rank.room.scoremodel.ScoreDetailModel;
import com.module.playways.rank.room.scoremodel.UserScoreModel;
import com.zq.live.proto.Room.VoteInfo;

import java.util.List;

public class RecordData {

    public List<VoteInfoModel> mVoteInfoModels;
    public ScoreDetailModel mScoreDetailModel;

    public RecordData(List<VoteInfoModel> mVoteInfoModels, ScoreDetailModel mScoreDetailModel) {
        this.mVoteInfoModels = mVoteInfoModels;
        this.mScoreDetailModel = mScoreDetailModel;
    }

    public VoteInfoModel getSelfVoteInfoModel() {
        for (VoteInfoModel voteInfoModel :
                mVoteInfoModels) {
            if (voteInfoModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                return voteInfoModel;
            }
        }

        return null;
    }

    // 是否有人逃跑
    public boolean hasEscape() {
        for (VoteInfoModel voteInfoModel : mVoteInfoModels) {
            if (voteInfoModel.isIsEscape()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasVote(int useId) {
        for (VoteInfoModel voteInfoModel : mVoteInfoModels) {
            for (Integer voterId : voteInfoModel.getVoter()) {
                if (useId == voterId) {
                    return true;
                }
            }
        }
        return false;
    }
}
