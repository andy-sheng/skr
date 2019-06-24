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
    int status;
    boolean hasNextMusic;

    public long getSyncStatusTimeMs() {
        return syncStatusTimeMs;
    }

    public void setSyncStatusTimeMs(long syncStatusTimeMs) {
        this.syncStatusTimeMs = syncStatusTimeMs;
    }

    public long getPassedTimeMs() {
        return passedTimeMs;
    }

    public void setPassedTimeMs(long passedTimeMs) {
        this.passedTimeMs = passedTimeMs;
    }



    public boolean isEnableNoLimitDuration() {
        return enableNoLimitDuration;
    }

    public void setEnableNoLimitDuration(boolean enableNoLimitDuration) {
        this.enableNoLimitDuration = enableNoLimitDuration;
    }

    public List<LocalUserLockInfo> getUserLockInfo() {
        return userLockInfo;
    }

    public void setUserLockInfo(List<LocalUserLockInfo> userLockInfo) {
        this.userLockInfo = userLockInfo;
    }

    public LocalCombineRoomMusic getCurrentMusic() {
        return currentMusic;
    }

    public void setCurrentMusic(LocalCombineRoomMusic currentMusic) {
        this.currentMusic = currentMusic;
    }

    public String getNextMusicDesc() {
        return nextMusicDesc;
    }

    public void setNextMusicDesc(String nextMusicDesc) {
        this.nextMusicDesc = nextMusicDesc;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isHasNextMusic() {
        return hasNextMusic;
    }

    public void setHasNextMusic(boolean hasNextMusic) {
        this.hasNextMusic = hasNextMusic;
    }

    @Override
    public String toString() {
        return "DoubleSyncModel{" +
                "syncStatusTimeMs=" + syncStatusTimeMs +
                ", passedTimeMs=" + passedTimeMs +
                ", userLockInfo=" + userLockInfo +
                ", enableNoLimitDuration=" + enableNoLimitDuration +
                ", currentMusic=" + currentMusic +
                ", nextMusicDesc='" + nextMusicDesc + '\'' +
                ", status=" + status +
                ", hasNextMusic=" + hasNextMusic +
                '}';
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
        doubleRoundInfoModel.hasNextMusic = combineRoomSyncStatusMsg.getHasNextMusic();
        return doubleRoundInfoModel;
    }
}
