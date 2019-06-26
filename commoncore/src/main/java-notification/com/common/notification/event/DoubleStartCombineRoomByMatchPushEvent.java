package com.common.notification.event;

import com.common.core.userinfo.model.LocalCombineRoomConfig;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Common.AgoraTokenInfo;
import com.zq.live.proto.Notification.CombineRoomEnterMsg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoubleStartCombineRoomByMatchPushEvent {
    BaseNotiInfo basePushInfo;
    int roomID; //房间ID
    long createdTimeMs; //房间创建的时间戳
    long passedTimeMs; //房间已经经历的毫秒数
    List<UserInfoModel> users; //玩家信息
    LocalCombineRoomConfig config; //配置信息
    Map<Integer, String> tokens; //声网token
    boolean needMaskUserInfo; //是否需要隐藏用户信息

    public BaseNotiInfo getBasePushInfo() {
        return basePushInfo;
    }

    public int getRoomID() {
        return roomID;
    }

    public long getCreatedTimeMs() {
        return createdTimeMs;
    }

    public long getPassedTimeMs() {
        return passedTimeMs;
    }

    public List<UserInfoModel> getUsers() {
        return users;
    }

    public LocalCombineRoomConfig getConfig() {
        return config;
    }

    public Map<Integer, String> getTokens() {
        return tokens;
    }

    public boolean isNeedMaskUserInfo() {
        return needMaskUserInfo;
    }

    public DoubleStartCombineRoomByMatchPushEvent(BaseNotiInfo basePushInfo, CombineRoomEnterMsg combineRoomEnterMsg) {
        this.basePushInfo = basePushInfo;
        this.roomID = combineRoomEnterMsg.getRoomID();
        this.createdTimeMs = combineRoomEnterMsg.getCreatedTimeMs();
        this.passedTimeMs = combineRoomEnterMsg.getPassedTimeMs();
        users = UserInfoModel.parseFromPB(combineRoomEnterMsg.getUsersList());
        config = LocalCombineRoomConfig.toLocalCombineRoomConfig(combineRoomEnterMsg.getConfig());
        tokens = new HashMap<>();
        for (AgoraTokenInfo agoraTokenInfo : combineRoomEnterMsg.getTokensList()) {
            tokens.put(agoraTokenInfo.getUserID(), agoraTokenInfo.getToken());
        }
        needMaskUserInfo = combineRoomEnterMsg.getNeedMaskUserInfo();
    }
}
