package com.module.playways.rank.room.model;

import com.zq.live.proto.Room.BLightInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BLightInfoModel implements Serializable {
    /**
     * per : 0
     * timeMs : 0
     * userID : 0
     */

    private int per;     //进度
    private int timeMs;  //时间戳
    private int userID;  //用户id

    public int getPer() {
        return per;
    }

    public void setPer(int per) {
        this.per = per;
    }

    public int getTimeMs() {
        return timeMs;
    }

    public void setTimeMs(int timeMs) {
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
        // TODO: 2019/2/21 等服务器上传新的PB

        return bLightInfoModel;
    }

    @Override
    public String toString() {
        return "BLightInfoModel{" +
                "per=" + per +
                ", timeMs=" + timeMs +
                ", userID=" + userID +
                '}';
    }
}
