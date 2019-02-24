package com.module.playways.rank.room.model;

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
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        BLightInfoModel bLightInfoModel = (BLightInfoModel) obj;
        return bLightInfoModel.getUserID() == getUserID() && bLightInfoModel.getSeq() == getSeq();
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


}
