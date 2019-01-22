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

    public WinResultModel getWinResult(int userID) {
        if (userID == 0) {
            return null;
        }
        if (mWinResultModels == null || mWinResultModels.size() <= 0) {
            return null;
        }
        for (WinResultModel winResultModel : mWinResultModels) {
            if (winResultModel.getUseID() == userID) {
                return winResultModel;
            }
        }
        return null;
    }
}
