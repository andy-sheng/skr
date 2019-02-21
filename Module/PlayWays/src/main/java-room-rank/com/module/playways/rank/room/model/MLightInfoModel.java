package com.module.playways.rank.room.model;

import com.zq.live.proto.Room.MlightInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MLightInfoModel implements Serializable {
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
        // TODO: 2019/2/21 等服务器上传新的PB
        return mLightInfoModel;
    }

    @Override
    public String toString() {
        return "MLightInfoModel{" +
                "per=" + per +
                ", timeMs=" + timeMs +
                ", userID=" + userID +
                '}';
    }
}
