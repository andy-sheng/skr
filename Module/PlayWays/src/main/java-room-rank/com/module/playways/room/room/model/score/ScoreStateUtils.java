package com.module.playways.room.room.model.score;

import com.common.log.MyLog;
import com.common.core.userinfo.model.ScoreStateModel;
import com.zq.live.proto.Room.ScoreState;

public class ScoreStateUtils {

    public static ScoreStateModel parse(ScoreState scoreState) {
        ScoreStateModel scoreStateModel = new  ScoreStateModel();
        if (scoreState == null) {
            MyLog.e("VoteInfoModel VoteInfo == null");
            return scoreStateModel;
        }

        scoreStateModel.setUserID(scoreState.getUserID());
        scoreStateModel.setSeq(scoreState.getSeq());
        scoreStateModel.setMainRanking(scoreState.getMainRanking().intValue());
        scoreStateModel.setSubRanking(scoreState.getSubRanking().intValue());
        scoreStateModel.setCurrStar(scoreState.getCurrStar().intValue());
        scoreStateModel.setMaxStar(scoreState.getMaxStar().intValue());
        scoreStateModel.setProtectBattleIndex(scoreState.getProtectBattleIndex().intValue());
        scoreStateModel.setCurrBattleIndex(scoreState.getCurrBattleIndex().intValue());
        scoreStateModel.setMaxBattleIndex(scoreState.getMaxBattleIndex().intValue());
        scoreStateModel.setTotalScore(scoreState.getTotalScore().intValue());
        scoreStateModel.setCurrExp(scoreState.getCurrExp().intValue());
        scoreStateModel.setMaxExp(scoreState.getMaxExp().intValue());
        scoreStateModel.setRankingDesc(scoreState.getRankingDesc());

        return scoreStateModel;
    }
}
