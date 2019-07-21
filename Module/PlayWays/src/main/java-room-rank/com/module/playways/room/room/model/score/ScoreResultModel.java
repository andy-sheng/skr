package com.module.playways.room.room.model.score;

import com.common.log.MyLog;
import com.zq.live.proto.Room.EFightForceWhy;
import com.zq.live.proto.Room.ScoreItem;
import com.zq.live.proto.Room.ScoreState;
import com.zq.live.proto.Room.UserScoreResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScoreResultModel implements Serializable {

    public final String TAG = "ScoreResultModel";

    private int userID;
    private List<ScoreStateModel> states;             //分值状态：初始、中间、最终状态，第0个为占位用
    private List<ScoreItemModel> starChange;          //星星变动详情，通过累加可以判断是加星还是减星
    private List<ScoreItemModel> battleIndexChange;   //战力值变动详情，累加可以判断是增加还是减少
    private int winType;                              //结果
    private int sss;  //战斗评价, sss or ss or s or a...
    private List<ScoreItemModel> expChange;           //经验值变动详情

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public List<ScoreStateModel> getStates() {
        return states;
    }

    public void setStates(List<ScoreStateModel> states) {
        this.states = states;
    }

    public List<ScoreItemModel> getStarChange() {
        return starChange;
    }

    public void setStarChange(List<ScoreItemModel> starChange) {
        this.starChange = starChange;
    }

    public List<ScoreItemModel> getBattleIndexChange() {
        return battleIndexChange;
    }

    public void setBattleIndexChange(List<ScoreItemModel> battleIndexChange) {
        this.battleIndexChange = battleIndexChange;
    }

    public int getWinType() {
        return winType;
    }

    public void setWinType(int winType) {
        this.winType = winType;
    }

    public int getSss() {
        return sss;
    }

    public void setSss(int sss) {
        this.sss = sss;
    }

    public List<ScoreItemModel> getExpChange() {
        return expChange;
    }

    public void setExpChange(List<ScoreItemModel> expChange) {
        this.expChange = expChange;
    }

    public void parse(UserScoreResult userScoreResult) {
        if (userScoreResult == null) {
            MyLog.d(TAG, "parse" + " userScoreResult=null ");
            return;
        }

        this.setUserID(userScoreResult.getUserID());

        List<ScoreStateModel> scoreStateModels = new ArrayList<>();
        for (ScoreState scoreState : userScoreResult.getStatesList()) {
            ScoreStateModel scoreStateModel = new ScoreStateModel();
            scoreStateModel.parse(scoreState);
            scoreStateModels.add(scoreStateModel);
        }
        this.setStates(scoreStateModels);

        List<ScoreItemModel> starChangeModels = new ArrayList<>();
        for (ScoreItem scoreItem : userScoreResult.getStarChangeList()) {
            ScoreItemModel scoreItemModel = new ScoreItemModel();
            scoreItemModel.parse(scoreItem);
            starChangeModels.add(scoreItemModel);
        }
        this.setStarChange(starChangeModels);

        List<ScoreItemModel> battleChangeModels = new ArrayList<>();
        for (ScoreItem scoreItem : userScoreResult.getBattleIndexChangeList()) {
            ScoreItemModel scoreItemModel = new ScoreItemModel();
            scoreItemModel.parse(scoreItem);
            battleChangeModels.add(scoreItemModel);
        }
        this.setBattleIndexChange(battleChangeModels);

        this.setWinType(userScoreResult.getWinType().getValue());
        this.setSss(userScoreResult.getSss());

        List<ScoreItemModel> expChangeModels = new ArrayList<>();
        for (ScoreItem scoreItem : userScoreResult.getExpChangeList()) {
            ScoreItemModel scoreItemModel = new ScoreItemModel();
            scoreItemModel.parse(scoreItem);
            expChangeModels.add(scoreItemModel);
        }
        this.setExpChange(expChangeModels);
    }

    // 是否战力值满，兑换星星（表示战力变化才有）
    public boolean isExchangeStar() {
        if (battleIndexChange != null && battleIndexChange.size() > 0) {
            for (ScoreItemModel scoreItemModel : battleIndexChange) {
                if (scoreItemModel.getIndex() == EFightForceWhy.ExchangeStarDecr.getValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    // 是否掉段保护，兑换星星（表示战力变化才有）
    public boolean isProtectRank() {
        if (battleIndexChange != null && battleIndexChange.size() > 0) {
            for (ScoreItemModel scoreItemModel : battleIndexChange) {
                if (scoreItemModel.getIndex() == EFightForceWhy.ProtectRankingDecr.getValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    // 真实战力变化
    public int getBattleChange() {
        int total = 0;
        if (battleIndexChange != null && battleIndexChange.size() > 0) {
            for (ScoreItemModel scoreItemModel : battleIndexChange) {
                if (scoreItemModel.getIndex() != EFightForceWhy.ProtectRankingDecr.getValue()
                        && scoreItemModel.getIndex() != EFightForceWhy.ExchangeStarDecr.getValue()) {
                    total = total + scoreItemModel.getScore();
                }
            }
        }
        return total;
    }

    public ScoreStateModel getSeq(int index) {
        if (states != null && states.size() > 0) {
            for (ScoreStateModel scoreStateModel : states) {
                if (scoreStateModel.getSeq() == index) {
                    return scoreStateModel;
                }
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "ScoreResultModel{" +
                "userID=" + userID +
                ", states=" + states +
                ", starChange=" + starChange +
                ", battleIndexChange=" + battleIndexChange +
                ", winType=" + winType +
                ", sss=" + sss +
                ", expChange=" + expChange +
                '}';
    }
}
