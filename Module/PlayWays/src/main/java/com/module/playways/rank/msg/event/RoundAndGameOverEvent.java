package com.module.playways.rank.msg.event;


import com.common.core.myinfo.MyUserInfoManager;
import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.room.model.UserGameResultModel;
import com.module.playways.rank.room.model.VoteInfoModel;
import com.module.playways.rank.room.model.WinResultModel;
import com.module.playways.rank.room.model.score.ScoreResultModel;
import com.zq.live.proto.Room.RoundAndGameOverMsg;
import com.zq.live.proto.Room.UserGameResult;
import com.zq.live.proto.Room.UserScoreResult;
import com.zq.live.proto.Room.VoteInfo;

import java.util.ArrayList;
import java.util.List;

public class RoundAndGameOverEvent {

    public long roundOverTimeMs;
    public BasePushInfo info;
    public List<VoteInfoModel> mVoteInfoModels;
    public ScoreResultModel mScoreResultModel;
    public List<UserGameResultModel> mUserGameResultModels;
    public List<WinResultModel> mWinResultModels;

    public RoundAndGameOverEvent(BasePushInfo info, RoundAndGameOverMsg roundAndGameOverMsg) {
        List<VoteInfoModel> voteInfoModels = new ArrayList<>();
        for (VoteInfo voteInfo : roundAndGameOverMsg.getVoteInfoList()) {
            VoteInfoModel voteInfoModel = new VoteInfoModel();
            voteInfoModel.parse(voteInfo);
            voteInfoModels.add(voteInfoModel);
        }

        List<WinResultModel> winResultModels = new ArrayList<>();     // 保存3个人胜负平和投票、逃跑结果
        ScoreResultModel scoreResultModel = new ScoreResultModel();
        for (UserScoreResult userScoreResult : roundAndGameOverMsg.getScoreResultsList()) {
            WinResultModel model = new WinResultModel();
            model.setUseID(userScoreResult.getUserID());
            model.setType(userScoreResult.getWinType().getValue());
            winResultModels.add(model);

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
        this.mWinResultModels = winResultModels;
    }
}
