package com.module.playways.rank.room.model;

import com.zq.live.proto.Room.BLightInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BLightInfoModel implements Serializable {
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

    public static List<BLightInfoModel> parse(List<BLightInfo> bLightInfoList) {
        ArrayList<BLightInfoModel> bLightInfoArrayList = new ArrayList<>();
        if (bLightInfoList != null) {
            for (BLightInfo bLightInfo : bLightInfoList) {
                bLightInfoArrayList.add(BLightInfoModel.parse(bLightInfo));
            }
        }

        return bLightInfoArrayList;
    }

    public static BLightInfoModel parse(BLightInfo bLightInfo) {
        BLightInfoModel bLightInfoModel = new BLightInfoModel();
        bLightInfoModel.setUserID(bLightInfo.getUserID());
        bLightInfoModel.setProcess(bLightInfo.getProcess());
        bLightInfoModel.setTimeMs(bLightInfo.getTimeMs());
        return bLightInfoModel;
    }


}
