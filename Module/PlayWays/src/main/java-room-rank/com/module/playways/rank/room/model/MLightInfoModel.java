package com.module.playways.rank.room.model;

import com.zq.live.proto.Room.MLightInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MLightInfoModel implements Serializable {
    /**
     * process : 0
     * timeMs : 0
     * userID : 0
     */

    private float process;
    private long timeMs;
    private int userID;
    private int seq;

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public float getProcess() {
        return process;
    }

    public void setProcess(float process) {
        this.process = process;
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
        MLightInfoModel mLightInfoModel = (MLightInfoModel) obj;
        return mLightInfoModel.getUserID() == getUserID() && mLightInfoModel.getSeq() == getSeq();
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
        mLightInfoModel.setProcess(mlightInfo.getProcess());
        mLightInfoModel.setUserID(mlightInfo.getUserID());
        mLightInfoModel.setTimeMs(mlightInfo.getTimeMs());
        mLightInfoModel.setSeq(seq);
        return mLightInfoModel;
    }
}
