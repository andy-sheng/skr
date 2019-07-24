package com.module.playways.doubleplay.model;

import com.module.playways.doubleplay.pbLocalModel.LocalChatSenceDataModel;
import com.module.playways.doubleplay.pbLocalModel.LocalGameSenceDataModel;
import com.module.playways.doubleplay.pbLocalModel.LocalSingSenceDataModel;
import com.module.playways.doubleplay.pbLocalModel.LocalUserLockInfo;
import com.component.live.proto.CombineRoom.CombineRoomSyncStatusV2Msg;

import java.io.Serializable;
import java.util.List;

public class DoubleSyncModel implements Serializable {
    long syncStatusTimeMs; //状态同步时的毫秒时间戳
    long passedTimeMs; //房间已经经历的毫秒数
    List<LocalUserLockInfo> userLockInfo;
    boolean enableNoLimitDuration; //开启没有限制的持续时间
    int combineStatus;
    int curScene;
    LocalSingSenceDataModel localSingSenceDataModel;
    LocalGameSenceDataModel localGameSenceDataModel;
    LocalChatSenceDataModel localChatSenceDataModel;

    public void setLocalSingSenceDataModel(LocalSingSenceDataModel localSingSenceDataModel) {
        this.localSingSenceDataModel = localSingSenceDataModel;
    }

    public void setLocalGameSenceDataModel(LocalGameSenceDataModel localGameSenceDataModel) {
        this.localGameSenceDataModel = localGameSenceDataModel;
    }

    public void setLocalChatSenceDataModel(LocalChatSenceDataModel localChatSenceDataModel) {
        this.localChatSenceDataModel = localChatSenceDataModel;
    }

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

    public int getCurScene() {
        return curScene;
    }

    public void setCurScene(int curScene) {
        this.curScene = curScene;
    }

    public List<LocalUserLockInfo> getUserLockInfo() {
        return userLockInfo;
    }

    public void setUserLockInfo(List<LocalUserLockInfo> userLockInfo) {
        this.userLockInfo = userLockInfo;
    }

    public int getCombineStatus() {
        return combineStatus;
    }

    public void setCombineStatus(int combineStatus) {
        this.combineStatus = combineStatus;
    }

    public LocalSingSenceDataModel getLocalSingSenceDataModel() {
        return localSingSenceDataModel;
    }

    public LocalGameSenceDataModel getLocalGameSenceDataModel() {
        return localGameSenceDataModel;
    }

    public LocalChatSenceDataModel getLocalChatSenceDataModel() {
        return localChatSenceDataModel;
    }

    @Override
    public String toString() {
        return "DoubleSyncModel{" +
                "syncStatusTimeMs=" + syncStatusTimeMs +
                ", passedTimeMs=" + passedTimeMs +
                ", userLockInfo=" + userLockInfo +
                ", enableNoLimitDuration=" + enableNoLimitDuration +
                ", combineStatus=" + combineStatus +
                ", curScene=" + curScene +
                ", localSingSenceDataModel=" + localSingSenceDataModel +
                ", localGameSenceDataModel=" + localGameSenceDataModel +
                ", localChatSenceDataModel=" + localChatSenceDataModel +
                '}';
    }

    public static DoubleSyncModel parse(CombineRoomSyncStatusV2Msg combineRoomSyncStatusMsg) {
        DoubleSyncModel doubleRoundInfoModel = new DoubleSyncModel();
//        doubleRoundInfoModel.basePushInfo = basePushInfo;
        doubleRoundInfoModel.syncStatusTimeMs = combineRoomSyncStatusMsg.getSyncStatusTimeMs();
        doubleRoundInfoModel.passedTimeMs = combineRoomSyncStatusMsg.getPassedTimeMs();
        doubleRoundInfoModel.userLockInfo = LocalUserLockInfo.toList(combineRoomSyncStatusMsg.getUserLockInfoList());
        doubleRoundInfoModel.enableNoLimitDuration = combineRoomSyncStatusMsg.getEnableNoLimitDuration();
        if (combineRoomSyncStatusMsg.hasCurScene()) {
            doubleRoundInfoModel.curScene = combineRoomSyncStatusMsg.getCurScene().getValue();
        }

        if (combineRoomSyncStatusMsg.hasSceneSingSyncStatusMsg()) {
            doubleRoundInfoModel.localSingSenceDataModel = new LocalSingSenceDataModel(combineRoomSyncStatusMsg.getSceneSingSyncStatusMsg());
        }

        if (combineRoomSyncStatusMsg.hasSceneGameSyncStatusMsg()) {
            doubleRoundInfoModel.localGameSenceDataModel = new LocalGameSenceDataModel(combineRoomSyncStatusMsg.getSceneGameSyncStatusMsg());
        }

        if (combineRoomSyncStatusMsg.hasSceneChatSyncStatusMsg()) {
            doubleRoundInfoModel.localChatSenceDataModel = new LocalChatSenceDataModel(combineRoomSyncStatusMsg.getSceneChatSyncStatusMsg());
        }

        doubleRoundInfoModel.combineStatus = combineRoomSyncStatusMsg.getStatus().getValue();
        return doubleRoundInfoModel;
    }
}
