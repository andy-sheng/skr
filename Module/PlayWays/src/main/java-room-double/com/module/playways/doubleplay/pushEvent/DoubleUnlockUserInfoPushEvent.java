package com.module.playways.doubleplay.pushEvent;

import com.module.playways.doubleplay.pbLocalModel.LocalUserLockInfo;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.CombineRoom.UnlockUserInfoMsg;

import java.util.List;

public class DoubleUnlockUserInfoPushEvent {
    BasePushInfo basePushInfo;
    int roomID; //房间ID
    List<LocalUserLockInfo> userLockInfo;
    boolean enableNoLimitDuration; //开启没有限制的持续时间

    public BasePushInfo getBasePushInfo() {
        return basePushInfo;
    }

    public int getRoomID() {
        return roomID;
    }

    public List<LocalUserLockInfo> getUserLockInfo() {
        return userLockInfo;
    }

    public boolean isEnableNoLimitDuration() {
        return enableNoLimitDuration;
    }

    public DoubleUnlockUserInfoPushEvent(BasePushInfo basePushInfo, UnlockUserInfoMsg unlockUserInfoMsg) {
        this.basePushInfo = basePushInfo;
        roomID = unlockUserInfoMsg.getRoomID();
        userLockInfo = LocalUserLockInfo.toList(unlockUserInfoMsg.getUserLockInfoList());
        enableNoLimitDuration = unlockUserInfoMsg.getEnableNoLimitDuration();
    }
}
