package com.module.playways.rank.room.model;

import com.zq.live.proto.Room.MlightInfo;

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

    public static List<MLightInfoModel> parse(List<MlightInfo> mLightInfoList) {
        ArrayList<MLightInfoModel> mLightInfoModels = new ArrayList<>();
        if (mLightInfoList != null) {
            for (MlightInfo mlightInfo :
                    mLightInfoList) {
                mLightInfoModels.add(MLightInfoModel.parse(mlightInfo));
            }
        }

        return mLightInfoModels;
    }

    public static MLightInfoModel parse(MlightInfo mlightInfo) {
        MLightInfoModel mLightInfoModel = new MLightInfoModel();
        mLightInfoModel.setProcess(mlightInfo.getProcess());
        mLightInfoModel.setUserID(mlightInfo.getUserID());
        mLightInfoModel.setTimeMs(mlightInfo.getTimeMs());
        return mLightInfoModel;
    }

}
