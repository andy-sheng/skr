package com.module.playways.rank.room.model;

import com.zq.live.proto.Room.BLightInfo;
import com.zq.live.proto.Room.MlightInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MLightInfoModel implements Serializable {
    /**
     * 玩家id
     */
    public int userID;

    /**
     * 爆灯时间戳
     */
    public long timeMs;

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public Long getTimeMs() {
        return timeMs;
    }

    public void setTimeMs(Long timeMs) {
        this.timeMs = timeMs;
    }

    public static List<MLightInfoModel> parse(List<MlightInfo> mLightInfoList){
        ArrayList<MLightInfoModel> mLightInfoModels = new ArrayList<>();
        if(mLightInfoList != null){
            for (MlightInfo mlightInfo :
                    mLightInfoList) {
                mLightInfoModels.add(MLightInfoModel.parse(mlightInfo));
            }
        }

        return mLightInfoModels;
    }

    public static MLightInfoModel parse(MlightInfo mlightInfo){
        MLightInfoModel mLightInfoModel = new MLightInfoModel();
        mLightInfoModel.userID = mlightInfo.getUserID();
        mLightInfoModel.timeMs = mlightInfo.getTimeMs();

        return mLightInfoModel;
    }
}
