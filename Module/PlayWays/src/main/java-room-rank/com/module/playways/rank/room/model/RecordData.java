package com.module.playways.rank.room.model;

import com.common.core.myinfo.MyUserInfoManager;
import com.module.playways.rank.room.model.score.ScoreResultModel;

import java.util.List;

public class RecordData {

    public List<VoteInfoModel> mVoteInfoModels;
    public ScoreResultModel mScoreResultModel;
    public List<WinResultModel> mWinResultModels;

    public RecordData(List<VoteInfoModel> mVoteInfoModels, ScoreResultModel mScoreResultModel, List<WinResultModel> mWinResultModels) {
        this.mVoteInfoModels = mVoteInfoModels;
        this.mScoreResultModel = mScoreResultModel;
        this.mWinResultModels = mWinResultModels;
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
