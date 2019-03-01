package com.module.playways.rank.msg.event;


import com.common.core.myinfo.MyUserInfoManager;
import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.room.model.RankRoundInfoModel;
import com.module.playways.rank.room.model.UserGameResultModel;
import com.module.playways.rank.room.model.VoteInfoModel;
import com.module.playways.rank.room.model.score.ScoreResultModel;
import com.zq.live.proto.Room.RoundAndGameOverMsg;
import com.zq.live.proto.Room.UserGameResult;
import com.zq.live.proto.Room.UserScoreResult;
import com.zq.live.proto.Room.VoteInfo;

import java.util.ArrayList;
import java.util.List;

public class RoundAndGameOverEvent {

    public BasePushInfo info;
    public long roundOverTimeMs;
    public List<VoteInfoModel> mVoteInfoModels;
    public ScoreResultModel mScoreResultModel;
    public List<UserGameResultModel> mUserGameResultModels;
    public int mExitUserID;
    public RankRoundInfoModel mRankRoundInfoModel;

    public RoundAndGameOverEvent(BasePushInfo info, RoundAndGameOverMsg roundAndGameOverMsg) {
        List<VoteInfoModel> voteInfoModels = new ArrayList<>();
        for (VoteInfo voteInfo : roundAndGameOverMsg.getVoteInfoList()) {
            VoteInfoModel voteInfoModel = new VoteInfoModel();
            voteInfoModel.parse(voteInfo);
            voteInfoModels.add(voteInfoModel);
        }

        ScoreResultModel scoreResultModel = new ScoreResultModel();
        for (UserScoreResult userScoreResult : roundAndGameOverMsg.getScoreResultsList()) {
            if (userScoreResult.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                scoreResultModel.parse(userScoreResult);
            }
        }

        List<UserGameResultModel> userGameResultModels = new ArrayList<>();
        for (UserGameResult userGameResult : roundAndGameOverMsg.getGameResultsList()) {
            userGameResultModels.add(UserGameResultModel.parse(userGameResult));
        }

        this.info = info;
        this.roundOverTimeMs = roundAndGameOverMsg.getRoundOverTimeMs();
        this.mVoteInfoModels = voteInfoModels;
        this.mScoreResultModel = scoreResultModel;
        this.mUserGameResultModels = userGameResultModels;
        this.mExitUserID = roundAndGameOverMsg.getExitUserID();
        this.mRankRoundInfoModel = RankRoundInfoModel.parseFromRoundInfo(roundAndGameOverMsg.getCurrentRound());
    }
}
