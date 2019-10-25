package com.module.playways.room.msg.event;

import com.common.core.myinfo.MyUserInfoManager;
import com.module.playways.room.msg.BasePushInfo;
import com.module.playways.room.room.model.UserGameResultModel;
import com.module.playways.room.room.model.score.ScoreResultModel;
import com.module.playways.room.room.model.VoteInfoModel;
import com.zq.live.proto.GrabRoom.UserGameResult;
import com.zq.live.proto.GrabRoom.UserScoreResult;
import com.zq.live.proto.GrabRoom.VoteInfo;
import com.zq.live.proto.GrabRoom.VoteResultMsg;

import java.util.ArrayList;
import java.util.List;

public class VoteResultEvent {

    public BasePushInfo mBasePushInfo;
    public List<VoteInfoModel> mVoteInfoModels;
    public ScoreResultModel mScoreResultModel;
    public List<UserGameResultModel> mUserGameResultModels;

    public VoteResultEvent(BasePushInfo basePushInfo, VoteResultMsg voteResultMsg) {
        List<VoteInfoModel> voteInfoModels = new ArrayList<>();
        for (VoteInfo voteInfo : voteResultMsg.getVoteInfoList()) {
            VoteInfoModel voteInfoModel = new VoteInfoModel();
            voteInfoModel.parse(voteInfo);
            voteInfoModels.add(voteInfoModel);
        }

        ScoreResultModel scoreResultModel = new ScoreResultModel();
        for (UserScoreResult userScoreResult : voteResultMsg.getScoreResultsList()) {
            if (userScoreResult.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                scoreResultModel.parse(userScoreResult);
            }
        }

        List<UserGameResultModel> userGameResultModels = new ArrayList<>();
        for (UserGameResult userGameResult : voteResultMsg.getGameResultsList()) {
            userGameResultModels.add(UserGameResultModel.parse(userGameResult));
        }

        this.mBasePushInfo = basePushInfo;
        this.mVoteInfoModels = voteInfoModels;
        this.mScoreResultModel = scoreResultModel;
        this.mUserGameResultModels = userGameResultModels;

    }
}
