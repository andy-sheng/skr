package com.module.playways.rank.room.model;

import com.zq.live.proto.Room.MLightInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MLightInfoModel implements Serializable {
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
        return userID * 10 + seq;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MLightInfoModel that = (MLightInfoModel) o;
        return userID == that.userID &&
                seq == that.seq;
    }

    public static List<MLightInfoModel> parse(List<MLightInfo> mLightInfoList, int seq) {
        ArrayList<MLightInfoModel> mLightInfoModels = new ArrayList<>();
        if (mLightInfoList != null) {
            for (MLightInfo mlightInfo :
                    mLightInfoList) {
                mLightInfoModels.add(MLightInfoModel.parse(mlightInfo, seq));
            }
        }

        return mLightInfoModels;
    }

    public static MLightInfoModel parse(MLightInfo mlightInfo, int seq) {
        MLightInfoModel mLightInfoModel = new MLightInfoModel();
        mLightInfoModel.setScore(mlightInfo.getScore());
        mLightInfoModel.setUserID(mlightInfo.getUserID());
        mLightInfoModel.setTimeMs(mlightInfo.getTimeMs());
        mLightInfoModel.setSeq(seq);
        return mLightInfoModel;
    }
}
