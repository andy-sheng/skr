package com.module.playways.doubleplay.pushEvent;

import com.module.playways.doubleplay.pbLocalModel.LocalUserLockInfo;
import com.module.playways.room.msg.BasePushInfo;
import com.component.live.proto.CombineRoom.UnlockUserInfoMsg;

import java.util.List;

public class DoubleUnlockUserInfoPushEvent {
    BasePushInfo basePushInfo;
    List<LocalUserLockInfo> userLockInfo;
    boolean enableNoLimitDuration; //开启没有限制的持续时间

    public BasePushInfo getBasePushInfo() {
        return basePushInfo;
    }

    public List<LocalUserLockInfo> getUserLockInfo() {
        return userLockInfo;
    }

    public boolean isEnableNoLimitDuration() {
        return enableNoLimitDuration;
    }

    public DoubleUnlockUserInfoPushEvent(BasePushInfo basePushInfo, UnlockUserInfoMsg unlockUserInfoMsg) {
        this.basePushInfo = basePushInfo;
        userLockInfo = LocalUserLockInfo.toList(unlockUserInfoMsg.getUserLockInfoList());
        enableNoLimitDuration = unlockUserInfoMsg.getEnableNoLimitDuration();
    }
}
