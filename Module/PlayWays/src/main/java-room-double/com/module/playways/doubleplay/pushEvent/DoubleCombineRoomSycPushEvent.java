package com.module.playways.doubleplay.pushEvent;

import com.module.playways.doubleplay.pbLocalModel.LocalCombineRoomMusic;
import com.module.playways.doubleplay.pbLocalModel.LocalUserLockInfo;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.CombineRoom.CombineRoomSyncStatusMsg;

import java.util.List;

public class DoubleCombineRoomSycPushEvent {
    BasePushInfo basePushInfo;
    long syncStatusTimeMs; //状态同步时的毫秒时间戳
    long passedTimeMs; //房间已经经历的毫秒数
    List<LocalUserLockInfo> userLockInfo;
    boolean enableNoLimitDuration; //开启没有限制的持续时间
    LocalCombineRoomMusic currentMusic;
    String nextMusicDesc;

    public DoubleCombineRoomSycPushEvent(BasePushInfo basePushInfo, CombineRoomSyncStatusMsg combineRoomSyncStatusMsg) {
        this.basePushInfo = basePushInfo;
        this.syncStatusTimeMs = combineRoomSyncStatusMsg.getSyncStatusTimeMs();
        this.passedTimeMs = combineRoomSyncStatusMsg.getPassedTimeMs();
        this.userLockInfo = LocalUserLockInfo.toList(combineRoomSyncStatusMsg.getUserLockInfoList());
        this.enableNoLimitDuration = combineRoomSyncStatusMsg.getEnableNoLimitDuration();
        this.currentMusic = new LocalCombineRoomMusic(combineRoomSyncStatusMsg.getCurrentMusic());
        this.nextMusicDesc = combineRoomSyncStatusMsg.getNextMusicDesc();
    }
}
