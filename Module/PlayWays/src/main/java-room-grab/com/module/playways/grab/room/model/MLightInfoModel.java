package com.module.playways.grab.room.model;

import com.zq.live.proto.Room.NoPassSingInfo;
import com.zq.live.proto.Room.QMLightMsg;

import java.io.Serializable;

/**
 * 灭灯信息
 */
public class MLightInfoModel implements Serializable {
    int userID;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    @Override
    public String toString() {
        return "MLightInfo{" +
                "userID=" + userID +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        MLightInfoModel that = (MLightInfoModel) object;
        return userID == that.userID;
    }

    @Override
    public int hashCode() {
        return userID;
    }

    public static MLightInfoModel parse(QMLightMsg pb) {
        MLightInfoModel noPassingInfo = new MLightInfoModel();
        noPassingInfo.setUserID(pb.getUserID());
        return noPassingInfo;
    }
}