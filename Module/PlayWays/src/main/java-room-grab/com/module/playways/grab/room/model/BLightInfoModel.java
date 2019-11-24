package com.module.playways.grab.room.model;

import com.component.busilib.model.BLightEffectModel;
import com.zq.live.proto.GrabRoom.QBLightMsg;

import java.io.Serializable;

/**
 * 爆灯信息
 */
public class BLightInfoModel implements Serializable {
    int userID;
    BLightEffectModel bLightEffectModel;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public BLightEffectModel getbLightEffectModel() {
        return bLightEffectModel;
    }

    public void setbLightEffectModel(BLightEffectModel bLightEffectModel) {
        this.bLightEffectModel = bLightEffectModel;
    }

    @Override
    public String toString() {
        return "BLightInfo{" +
                "userID=" + userID +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        BLightInfoModel that = (BLightInfoModel) object;
        return userID == that.userID;
    }

    @Override
    public int hashCode() {
        return userID;
    }

    public static BLightInfoModel parse(QBLightMsg pb) {
        BLightInfoModel noPassingInfo = new BLightInfoModel();
        noPassingInfo.setUserID(pb.getUserID());
        noPassingInfo.setbLightEffectModel(BLightEffectModel.Companion.parseBLightEffectModelFromPb(pb.getShowInfo()));
        return noPassingInfo;
    }
}