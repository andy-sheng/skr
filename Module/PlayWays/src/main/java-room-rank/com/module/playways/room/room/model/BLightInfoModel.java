package com.module.playways.room.room.model;

import com.zq.live.proto.Room.BLightInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BLightInfoModel implements Serializable {
    /**
     * score : 0
     * timeMs : 0
     * userID : 0
     */

    private float score;
    private long timeMs;
    private int userID;
    private int seq;

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public void setTimeMs(long timeMs) {
        this.timeMs = timeMs;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BLightInfoModel that = (BLightInfoModel) o;
        return userID == that.userID &&
                seq == that.seq;
    }

    @Override
    public int hashCode() {
        return userID * 10 + seq;
    }

    public static List<BLightInfoModel> parse(List<BLightInfo> bLightInfoList, int seq) {
        ArrayList<BLightInfoModel> bLightInfoArrayList = new ArrayList<>();
        if (bLightInfoList != null) {
            for (BLightInfo bLightInfo : bLightInfoList) {
                bLightInfoArrayList.add(BLightInfoModel.parse(bLightInfo, seq));
            }
        }

        return bLightInfoArrayList;
    }

    public static BLightInfoModel parse(BLightInfo bLightInfo, int seq) {
        BLightInfoModel bLightInfoModel = new BLightInfoModel();
        bLightInfoModel.setUserID(bLightInfo.getUserID());
        bLightInfoModel.setScore(bLightInfo.getScore());
        bLightInfoModel.setTimeMs(bLightInfo.getTimeMs());
        bLightInfoModel.setSeq(seq);
        return bLightInfoModel;
    }

    @Override
    public String toString() {
        return "BLightInfoModel{" +
                "score=" + score +
                ", timeMs=" + timeMs +
                ", userID=" + userID +
                ", seq=" + seq +
                '}';
    }
}
