package com.module.playways.rank.room.model.score;

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
}
