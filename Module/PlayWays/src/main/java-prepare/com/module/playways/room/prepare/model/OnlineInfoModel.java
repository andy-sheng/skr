package com.module.playways.room.prepare.model;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.zq.live.proto.Room.OnlineInfo;

import java.io.Serializable;

public class OnlineInfoModel implements Serializable {
    /**
     * userID : 16
     * isOnline : true
     */

    private int userID;
    private boolean isOnline;
    private UserInfoModel userInfoModel;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public boolean isIsOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public UserInfoModel getUserInfoModel() {
        return userInfoModel;
    }

    public void setUserInfoModel(UserInfoModel userInfoModel) {
        this.userInfoModel = userInfoModel;
    }

    @Override
    public String toString() {
        return "OnlineInfoModel{" +
                "userID=" + userID +
                ", isOnline=" + isOnline +
                ", userInfoModel=" + userInfoModel +
                '}';
    }

    public void parse(OnlineInfo onlineInfo){
        if (onlineInfo == null){
            MyLog.e("JsonOnLineInfo OnlineInfo == null");
            return;
        }

        this.setUserID(onlineInfo.getUserID());
        this.setIsOnline(onlineInfo.getIsOnline());
        this.setUserInfoModel(UserInfoModel.parseFromPB(onlineInfo.getUserInfo()));
    }
}
