package com.common.notification.event;

import com.common.core.userinfo.model.LocalCombineRoomConfig;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Common.AgoraTokenInfo;
import com.zq.live.proto.Notification.CombineRoomEnterMsg;

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
    boolean needMaskUserInfo; //是否需要隐藏用户信息

    public CombineRoomSyncInviteUserNotifyEvent(BaseNotiInfo baseNotiInfo, CombineRoomEnterMsg combineRoomEnterMsg) {
        mBaseNotiInfo = baseNotiInfo;
        roomID = combineRoomEnterMsg.getRoomID();
        createdTimeMs = combineRoomEnterMsg.getCreatedTimeMs();
        passedTimeMs = combineRoomEnterMsg.getPassedTimeMs();
        users = UserInfoModel.parseFromPB(combineRoomEnterMsg.getUsersList());
        config = LocalCombineRoomConfig.toLocalCombineRoomConfig(combineRoomEnterMsg.getConfig());
        tokens = new HashMap<>();
        for (AgoraTokenInfo agoraTokenInfo : combineRoomEnterMsg.getTokensList()) {
            tokens.put(agoraTokenInfo.getUserID(), agoraTokenInfo.getToken());
        }
        needMaskUserInfo = combineRoomEnterMsg.getNeedMaskUserInfo();
    }

    public Map<Integer, String> getTokens() {
        return tokens;
    }

    public boolean isNeedMaskUserInfo() {
        return needMaskUserInfo;
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
