package com.module.playways.doubleplay.model;

import com.module.playways.doubleplay.pbLocalModel.LocalCombineRoomMusic;
import com.module.playways.doubleplay.pbLocalModel.LocalUserLockInfo;
import com.zq.live.proto.CombineRoom.CombineRoomSyncStatusMsg;

import java.io.Serializable;
import java.util.List;

public class DoubleSyncModel implements Serializable {
    long syncStatusTimeMs; //状态同步时的毫秒时间戳
    long passedTimeMs; //房间已经经历的毫秒数
    List<LocalUserLockInfo> userLockInfo;
    boolean enableNoLimitDuration; //开启没有限制的持续时间
    LocalCombineRoomMusic currentMusic;
    String nextMusicDesc;

    public long getSyncStatusTimeMs() {
        return syncStatusTimeMs;
    }

    public long getPassedTimeMs() {
        return passedTimeMs;
    }

    public List<LocalUserLockInfo> getUserLockInfo() {
        return userLockInfo;
    }

    public boolean isEnableNoLimitDuration() {
        return enableNoLimitDuration;
    }

    public LocalCombineRoomMusic getCurrentMusic() {
        return currentMusic;
    }

    public String getNextMusicDesc() {
        return nextMusicDesc;
    }

    public static DoubleSyncModel parse(CombineRoomSyncStatusMsg combineRoomSyncStatusMsg) {
        DoubleSyncModel doubleRoundInfoModel = new DoubleSyncModel();
//        doubleRoundInfoModel.basePushInfo = basePushInfo;
        doubleRoundInfoModel.syncStatusTimeMs = combineRoomSyncStatusMsg.getSyncStatusTimeMs();
        doubleRoundInfoModel.passedTimeMs = combineRoomSyncStatusMsg.getPassedTimeMs();
        doubleRoundInfoModel.userLockInfo = LocalUserLockInfo.toList(combineRoomSyncStatusMsg.getUserLockInfoList());
        doubleRoundInfoModel.enableNoLimitDuration = combineRoomSyncStatusMsg.getEnableNoLimitDuration();
        doubleRoundInfoModel.currentMusic = new LocalCombineRoomMusic(combineRoomSyncStatusMsg.getCurrentMusic());
        doubleRoundInfoModel.nextMusicDesc = combineRoomSyncStatusMsg.getNextMusicDesc();
        return doubleRoundInfoModel;
    }
}
