package com.module.playways.rank.room.model;

import com.common.core.myinfo.MyUserInfoManager;
import com.module.playways.rank.room.model.score.ScoreResultModel;

import java.io.Serializable;
import java.util.List;

public class RecordData implements Serializable {

    public List<VoteInfoModel> mVoteInfoModels;
    public ScoreResultModel mScoreResultModel;
    public List<UserGameResultModel> mUserGameResultModels;

    public RecordData(List<VoteInfoModel> mVoteInfoModels, ScoreResultModel mScoreResultModel, List<UserGameResultModel> mUserGameResultModels) {
        this.mVoteInfoModels = mVoteInfoModels;
        this.mScoreResultModel = mScoreResultModel;
        this.mUserGameResultModels = mUserGameResultModels;
    }

    public VoteInfoModel getSelfVoteInfoModel() {
        for (VoteInfoModel voteInfoModel : mVoteInfoModels) {
            if (voteInfoModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                return voteInfoModel;
            }
        }

        return null;
    }

    // 是否有人逃跑
    public boolean hasEscape() {
        for (VoteInfoModel voteInfoModel : mVoteInfoModels) {
            if (voteInfoModel.isEscape()) {
                return true;
            }
        }
        return false;
    }

    public int getSelfWinType() {
        for (UserGameResultModel userGameResultModel : mUserGameResultModels) {
            if (userGameResultModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                return userGameResultModel.getWinType();
            }
        }

        return 0;
    }

    public VoteInfoModel getVoteInfoModel(int userID) {
        if (userID == 0) {
            return null;
        }
        if (mVoteInfoModels == null || mVoteInfoModels.size() <= 0) {
            return null;
        }
        for (VoteInfoModel voteInfoModel : mVoteInfoModels) {
            if (voteInfoModel.getUserID() == userID) {
                return voteInfoModel;
            }
        }
        return null;
    }

    public UserGameResultModel getUserGameResultModel(int userID) {
        if (userID == 0) {
            return null;
        }
        if (mUserGameResultModels == null || mUserGameResultModels.size() <= 0) {
            return null;
        }
        for (UserGameResultModel userGameResultModel : mUserGameResultModels) {
            if (userGameResultModel.getUserID() == userID) {
                return userGameResultModel;
            }
        }
        return null;
    }

    public int getUserIdByRank(int rank) {
        for (UserGameResultModel userGameResultModel : mUserGameResultModels) {
            if (userGameResultModel.getRank() == rank) {
                return userGameResultModel.getUserID();
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return "RecordData{" +
                "mVoteInfoModels=" + mVoteInfoModels +
                ", mScoreResultModel=" + mScoreResultModel +
                ", mUserGameResultModels=" + mUserGameResultModels +
                '}';
    }
}
