package com.module.playways.room.room.model;

import com.zq.live.proto.Room.AudienceScore;
import com.zq.live.proto.Room.UserGameResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserGameResultModel implements Serializable {

    public static final int Win = 1;
    public static final int Draw = 2;
    public static final int Lose = 3;

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

    private List<AudienceScoreModel> audienceScores;

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


    public List<AudienceScoreModel> getAudienceScores() {
        return audienceScores;
    }

    public void setAudienceScores(List<AudienceScoreModel> audienceScores) {
        this.audienceScores = audienceScores;
    }


    public static UserGameResultModel parse(UserGameResult userGameResult) {
        UserGameResultModel gameResultModel = new UserGameResultModel();
        gameResultModel.setIsEscape(userGameResult.getIsEscape());
        gameResultModel.setItemID(userGameResult.getItemID());
        gameResultModel.setRank(userGameResult.getRank());
        gameResultModel.setTotalScore(userGameResult.getTotalScore());
        gameResultModel.setUserID(userGameResult.getUserID());
        gameResultModel.setWinType(userGameResult.getWinType().getValue());
        List<AudienceScoreModel> listenProgressModels = new ArrayList<>();
        for (AudienceScore audienceScore : userGameResult.getAudienceScoresList()) {
            listenProgressModels.add(AudienceScoreModel.parse(audienceScore));
        }
        gameResultModel.setAudienceScores(listenProgressModels);
        return gameResultModel;
    }

    @Override
    public String toString() {
        return "UserGameResultModel{" +
                "isEscape=" + isEscape +
                ", itemID=" + itemID +
                ", rank=" + rank +
                ", totalScore=" + totalScore +
                ", userID=" + userID +
                ", winType=" + winType +
                ", audienceScores=" + audienceScores +
                '}';
    }
}
