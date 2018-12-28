package com.module.playways.rank.prepare.model;

import com.common.log.MyLog;
import com.zq.live.proto.Room.OnlineInfo;

public class OnlineInfoModel {
    /**
     * userID : 16
     * isOnline : true
     */

    private int userID;
    private boolean isOnline;

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

    public void parse(OnlineInfo onlineInfo){
        if (onlineInfo == null){
            MyLog.e("JsonOnLineInfo OnlineInfo == null");
            return;
        }

        this.setUserID(onlineInfo.getUserID());
        this.setIsOnline(onlineInfo.getIsOnline());
    }
}
