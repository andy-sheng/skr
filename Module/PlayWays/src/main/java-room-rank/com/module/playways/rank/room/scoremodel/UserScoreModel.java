package com.module.playways.rank.room.scoremodel;

import com.common.log.MyLog;
import com.zq.live.proto.Room.ScoreItem;
import com.zq.live.proto.Room.UserScoreRecord;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// 分值信息
public class UserScoreModel implements Serializable {
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
    private List<UserScoreItem> items;
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

    public List<UserScoreItem> getItems() {
        return items;
    }

    public void setItems(List<UserScoreItem> items) {
        this.items = items;
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

    @Override
    public String toString() {
        return "UserScoreModel{" +
                "userID=" + userID +
                ", scoreType=" + scoreType +
                ", scoreNow=" + scoreNow +
                ", scoreBefore=" + scoreBefore +
                ", items=" + items +
                ", scoreTypeDesc='" + scoreTypeDesc + '\'' +
                ", scoreNowDesc='" + scoreNowDesc + '\'' +
                ", scoreBeforeDesc='" + scoreBeforeDesc + '\'' +
                '}';
    }

    public void parse(UserScoreRecord userScoreRecord) {
        if (userScoreRecord == null) {
            MyLog.e("UserScoreRecord == null");
            return;
        }

        this.setUserID(userScoreRecord.getUserID());
        this.setScoreType(userScoreRecord.getScoreType().getValue());
        this.setScoreNow(userScoreRecord.getScoreNow());
        this.setScoreBefore(userScoreRecord.getScoreBefore());
        this.setScoreTypeDesc(userScoreRecord.getScoreTypeDesc());
        this.setScoreNowDesc(userScoreRecord.getScoreNowDesc());
        this.setScoreBeforeDesc(userScoreRecord.getScoreBeforeDesc());
        List<UserScoreItem> userScoreItemList = new ArrayList<>();
        for (ScoreItem scoreItem : userScoreRecord.getItemsList()) {
            UserScoreItem userScoreItem = new UserScoreItem();
            userScoreItem.parse(scoreItem);
            userScoreItemList.add(userScoreItem);
        }
        this.setItems(userScoreItemList);

    }
}
