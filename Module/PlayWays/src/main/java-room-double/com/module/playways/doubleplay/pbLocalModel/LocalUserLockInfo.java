package com.module.playways.doubleplay.pbLocalModel;

import com.zq.live.proto.CombineRoom.UserLockInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LocalUserLockInfo implements Serializable {
    int userID; //解锁信息的用户ID
    boolean hasLock;

    public LocalUserLockInfo() {
    }

    public LocalUserLockInfo(int userID, boolean hasLock) {
        this.userID = userID;
        this.hasLock = hasLock;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public boolean isHasLock() {
        return hasLock;
    }

    public void setHasLock(boolean hasLock) {
        this.hasLock = hasLock;
    }

    public static List<LocalUserLockInfo> toList(List<UserLockInfo> userLockInfoList) {
        ArrayList<LocalUserLockInfo> localUserLockInfos = new ArrayList<>();
        if (userLockInfoList == null) {
            return localUserLockInfos;
        }

        for (UserLockInfo userLockInfo : userLockInfoList) {
            localUserLockInfos.add(new LocalUserLockInfo(userLockInfo.getUserID(), userLockInfo.getHasLock()));
        }

        return localUserLockInfos;
    }
}
