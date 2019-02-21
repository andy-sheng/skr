package com.module.playways.rank.room.model;

import com.zq.live.proto.Room.ListenProgress;

import java.io.Serializable;

public class ListenProgressModel implements Serializable {
    /**
     * lightType : ELT_UNKNOWN
     * progress : 0
     * userID : 0
     */

    private String lightType;
    private int progress;
    private int userID;

    public String getLightType() {
        return lightType;
    }

    public void setLightType(String lightType) {
        this.lightType = lightType;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public static ListenProgressModel parse(ListenProgress listenProgress) {
        ListenProgressModel model = new ListenProgressModel();
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
