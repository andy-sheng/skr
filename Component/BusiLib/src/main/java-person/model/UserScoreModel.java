package model;

import com.common.log.MyLog;

// 分值信息
public class UserScoreModel {
    /**
     * userID : 123
     * scoreType : 1
     * scoreNow : 3
     * scoreBefore : 2
     * scoreTypeDesc : 等级
     * scoreNowDesc : 3级
     * scoreBeforeDesc : 2级
     */

    private int userID;
    private int scoreType;
    private int scoreNow;
    private int scoreBefore;
    private String scoreTypeDesc;
    private String scoreNowDesc;
    private String scoreBeforeDesc;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getScoreType() {
        return scoreType;
    }

    public void setScoreType(int scoreType) {
        this.scoreType = scoreType;
    }

    public int getScoreNow() {
        return scoreNow;
    }

    public void setScoreNow(int scoreNow) {
        this.scoreNow = scoreNow;
    }

    public int getScoreBefore() {
        return scoreBefore;
    }

    public void setScoreBefore(int scoreBefore) {
        this.scoreBefore = scoreBefore;
    }

    public String getScoreTypeDesc() {
        return scoreTypeDesc;
    }

    public void setScoreTypeDesc(String scoreTypeDesc) {
        this.scoreTypeDesc = scoreTypeDesc;
    }

    public String getScoreNowDesc() {
        return scoreNowDesc;
    }

    public void setScoreNowDesc(String scoreNowDesc) {
        this.scoreNowDesc = scoreNowDesc;
    }

    public String getScoreBeforeDesc() {
        return scoreBeforeDesc;
    }

    public void setScoreBeforeDesc(String scoreBeforeDesc) {
        this.scoreBeforeDesc = scoreBeforeDesc;
    }
}
