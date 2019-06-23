package com.common.notification.event;

import com.common.core.userinfo.model.LocalCombineRoomConfig;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Common.AgoraTokenInfo;
import com.zq.live.proto.Notification.CombineRoomSyncInviteUserMsg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CombineRoomSyncInviteUserNotifyEvent {
    public BaseNotiInfo mBaseNotiInfo;
    private Integer roomID;
    private Long createdTimeMs;
    private Long passedTimeMs;
    private List<UserInfoModel> users;
    Map<Integer, String> tokens; //声网token
    private LocalCombineRoomConfig config;

    public CombineRoomSyncInviteUserNotifyEvent(BaseNotiInfo baseNotiInfo, CombineRoomSyncInviteUserMsg combineRoomSyncInviteUserMsg) {
        mBaseNotiInfo = baseNotiInfo;
        roomID = combineRoomSyncInviteUserMsg.getRoomID();
        createdTimeMs = combineRoomSyncInviteUserMsg.getCreatedTimeMs();
        passedTimeMs = combineRoomSyncInviteUserMsg.getPassedTimeMs();
        users = UserInfoModel.parseFromPB(combineRoomSyncInviteUserMsg.getUsersList());
        config = LocalCombineRoomConfig.toLocalCombineRoomConfig(combineRoomSyncInviteUserMsg.getConfig());
        tokens = new HashMap<>();
        for (AgoraTokenInfo agoraTokenInfo : combineRoomSyncInviteUserMsg.getTokensList()) {
            tokens.put(agoraTokenInfo.getUserID(), agoraTokenInfo.getToken());
        }
    }

    public Map<Integer, String> getTokens() {
        return tokens;
    }

    public BaseNotiInfo getBaseNotiInfo() {
        return mBaseNotiInfo;
    }

    public Integer getRoomID() {
        return roomID;
    }

    public Long getCreatedTimeMs() {
        return createdTimeMs;
    }

    public Long getPassedTimeMs() {
        return passedTimeMs;
    }

    public List<UserInfoModel> getUsers() {
        return users;
    }

    public LocalCombineRoomConfig getConfig() {
        return config;
    }
}
