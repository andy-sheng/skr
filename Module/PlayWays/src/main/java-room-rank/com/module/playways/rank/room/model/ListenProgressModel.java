package com.module.playways.rank.room.model;

import com.zq.live.proto.Room.AudienceScore;

import java.io.Serializable;

public class ListenProgressModel implements Serializable {
    /**
     * lightType : ELT_UNKNOWN
     * progress : 0
     * userID : 0
     */

    private int lightType;
    private float progress;
    private int userID;

    public int getLightType() {
        return lightType;
    }

    public void setLightType(int lightType) {
        this.lightType = lightType;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public static ListenProgressModel parse(AudienceScore audienceScore) {
        ListenProgressModel model = new ListenProgressModel();
        model.setLightType(audienceScore.getLightType().getValue());
        model.setProgress(audienceScore.getScore());
        model.setUserID(audienceScore.getUserID());
        return model;
    }

    @Override
    public String toString() {
        return "ListenProgress{" +
                "lightType='" + lightType + '\'' +
                ", progress=" + progress +
                ", userID=" + userID +
                '}';
    }
}
