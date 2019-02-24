package com.module.playways.rank.room.model;

import com.zq.live.proto.Room.AudienceScore;
import com.zq.live.proto.Room.UserGameResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserGameResultModel implements Serializable {
    /**
     * isEscape : true
     * itemID : 0
     * listenProgress : [{"lightType":"ELT_UNKNOWN","progress":0,"userID":0}]
     * rank : 0
     * totalScore : 0.0
     * userID : 0
     * winType : InvalidEWinType
     */

    private boolean isEscape;
    private int itemID;
    private int rank;
    private float totalScore;
    private int userID;
    private int winType;
    private List<ListenProgressModel> listenProgress;

    public boolean isIsEscape() {
        return isEscape;
    }

    public void setIsEscape(boolean isEscape) {
        this.isEscape = isEscape;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public float getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(float totalScore) {
        this.totalScore = totalScore;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getWinType() {
        return winType;
    }

    public void setWinType(int winType) {
        this.winType = winType;
    }

    public List<ListenProgressModel> getListenProgress() {
        return listenProgress;
    }

    public void setListenProgress(List<ListenProgressModel> listenProgress) {
        this.listenProgress = listenProgress;
    }

    public static UserGameResultModel parse(UserGameResult userGameResult) {
        UserGameResultModel gameResultModel = new UserGameResultModel();
        gameResultModel.setIsEscape(userGameResult.getIsEscape());
        gameResultModel.setItemID(userGameResult.getItemID());
        gameResultModel.setRank(userGameResult.getRank());
        gameResultModel.setTotalScore(userGameResult.getTotalScore());
        gameResultModel.setUserID(userGameResult.getUserID());
        gameResultModel.setWinType(userGameResult.getWinType().getValue());
        List<ListenProgressModel> listenProgressModels = new ArrayList<>();
        for (AudienceScore audienceScore : userGameResult.getAudienceScoresList()) {
            listenProgressModels.add(ListenProgressModel.parse(audienceScore));
        }
        gameResultModel.setListenProgress(listenProgressModels);
        return gameResultModel;
    }

    @Override
    public String toString() {
        return "UserGameResult{" +
                "isEscape=" + isEscape +
                ", itemID=" + itemID +
                ", rank=" + rank +
                ", totalScore=" + totalScore +
                ", userID=" + userID +
                ", winType='" + winType + '\'' +
                ", listenProgress=" + listenProgress +
                '}';
    }
}
