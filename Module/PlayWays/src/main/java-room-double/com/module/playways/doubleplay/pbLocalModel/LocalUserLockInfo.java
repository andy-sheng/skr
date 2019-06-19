package com.module.playways.doubleplay.pbLocalModel;

import com.zq.live.proto.CombineRoom.UserLockInfo;

import java.util.ArrayList;
import java.util.List;

public class LocalUserLockInfo {
    int userID; //解锁信息的用户ID
    boolean hasLock;

    public LocalUserLockInfo(int userID, boolean hasLock) {
        this.userID = userID;
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
