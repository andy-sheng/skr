package com.module.playways.rank.room.model;

import com.squareup.wire.WireField;
import com.zq.live.proto.Room.BLightInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BLightInfoModel implements Serializable {
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

    public static List<BLightInfoModel> parse(List<BLightInfo> bLightInfoList){
        ArrayList<BLightInfoModel> bLightInfoArrayList = new ArrayList<>();
        if(bLightInfoList != null){
            for (BLightInfo bLightInfo :
                    bLightInfoList) {
                bLightInfoArrayList.add(BLightInfoModel.parse(bLightInfo));
            }
        }

        return bLightInfoArrayList;
    }

    public static BLightInfoModel parse(BLightInfo bLightInfo){
        BLightInfoModel bLightInfoModel = new BLightInfoModel();
        bLightInfoModel.userID = bLightInfo.getUserID();
        bLightInfoModel.timeMs = bLightInfoModel.getTimeMs();

        return bLightInfoModel;
    }
}
