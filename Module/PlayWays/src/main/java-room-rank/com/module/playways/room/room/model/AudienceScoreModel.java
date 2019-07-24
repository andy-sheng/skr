package com.module.playways.room.room.model;

import com.component.live.proto.Room.AudienceScore;

import java.io.Serializable;

public class AudienceScoreModel implements Serializable {
    /**
     * lightType : ELT_UNKNOWN
     * progress : 0
     * userID : 0
     */

    private int lightType;
    private float score;
    private int userID;

    public int getLightType() {
        return lightType;
    }

    public void setLightType(int lightType) {
        this.lightType = lightType;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public static AudienceScoreModel parse(AudienceScore audienceScore) {
        AudienceScoreModel model = new AudienceScoreModel();
        model.setLightType(audienceScore.getLightType().getValue());
        model.setScore(audienceScore.getScore());
        model.setUserID(audienceScore.getUserID());
        return model;
    }

    @Override
    public String toString() {
        return "AudienceScoreModel{" +
                "lightType=" + lightType +
                ", score=" + score +
                ", userID=" + userID +
                '}';
    }
}
