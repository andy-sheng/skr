package com.module.playways.room.room.model.score;

import com.common.log.MyLog;
import com.component.live.proto.Room.ScoreState;

import java.io.Serializable;

// 状态信息
public class ScoreStateModel implements Serializable {

    private int userID;             // 用户id
    private int seq;                // 分值状态的时间顺序, 数字越大越晚
    private int mainRanking;        // 主段位数值
    private int subRanking;         // 子段位数值
    private int currStar;           // 子段位当前星星数
    private int maxStar;            // 子段位星星数上限
    private int protectBattleIndex; // 掉段保护所需战力分值
    private int currBattleIndex;    // 当前战力分值
    private int maxBattleIndex;     // 战力分值上限
    private int totalScore;         // 用在段位排行榜中的总分值
    private int currExp;            // 子段位当前经验值
    private int maxExp;             // 子段位经验值上限
    private String rankingDesc;     // 描述段位

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public int getMainRanking() {
        return mainRanking;
    }

    public void setMainRanking(int mainRanking) {
        this.mainRanking = mainRanking;
    }

    public int getSubRanking() {
        return subRanking;
    }

    public void setSubRanking(int subRanking) {
        this.subRanking = subRanking;
    }

    public int getCurrStar() {
        return currStar;
    }

    public void setCurrStar(int currStar) {
        this.currStar = currStar;
    }

    public int getMaxStar() {
        return maxStar;
    }

    public void setMaxStar(int maxStar) {
        this.maxStar = maxStar;
    }

    public int getProtectBattleIndex() {
        return protectBattleIndex;
    }

    public void setProtectBattleIndex(int protectBattleIndex) {
        this.protectBattleIndex = protectBattleIndex;
    }

    public int getCurrBattleIndex() {
        return currBattleIndex;
    }

    public void setCurrBattleIndex(int currBattleIndex) {
        this.currBattleIndex = currBattleIndex;
    }

    public int getMaxBattleIndex() {
        return maxBattleIndex;
    }

    public void setMaxBattleIndex(int maxBattleIndex) {
        this.maxBattleIndex = maxBattleIndex;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getCurrExp() {
        return currExp;
    }

    public void setCurrExp(int currExp) {
        this.currExp = currExp;
    }

    public int getMaxExp() {
        return maxExp;
    }

    public void setMaxExp(int maxExp) {
        this.maxExp = maxExp;
    }

    public String getRankingDesc() {
        return rankingDesc;
    }

    public void setRankingDesc(String rankingDesc) {
        this.rankingDesc = rankingDesc;
    }

    public void parse(ScoreState scoreState) {
        if (scoreState == null) {
            MyLog.e("VoteInfoModel VoteInfo == null");
            return;
        }

        this.setUserID(scoreState.getUserID());
        this.setSeq(scoreState.getSeq());
        this.setMainRanking(scoreState.getMainRanking().intValue());
        this.setSubRanking(scoreState.getSubRanking().intValue());
        this.setCurrStar(scoreState.getCurrStar().intValue());
        this.setMaxStar(scoreState.getMaxStar().intValue());
        this.setProtectBattleIndex(scoreState.getProtectBattleIndex().intValue());
        this.setCurrBattleIndex(scoreState.getCurrBattleIndex().intValue());
        this.setMaxBattleIndex(scoreState.getMaxBattleIndex().intValue());
        this.setTotalScore(scoreState.getTotalScore().intValue());
        this.setCurrExp(scoreState.getCurrExp().intValue());
        this.setMaxExp(scoreState.getMaxExp().intValue());
        this.setRankingDesc(scoreState.getRankingDesc());
    }

    @Override
    public String toString() {
        return "ScoreStateModel{" +
                "userID=" + userID +
                ", seq=" + seq +
                ", mainRanking=" + mainRanking +
                ", subRanking=" + subRanking +
                ", currStar=" + currStar +
                ", maxStar=" + maxStar +
                ", protectBattleIndex=" + protectBattleIndex +
                ", currBattleIndex=" + currBattleIndex +
                ", maxBattleIndex=" + maxBattleIndex +
                ", totalScore=" + totalScore +
                ", currExp=" + currExp +
                ", maxExp=" + maxExp +
                ", rankingDesc='" + rankingDesc + '\'' +
                '}';
    }
}
