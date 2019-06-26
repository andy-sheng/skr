package com.common.notification.event;

import com.common.core.userinfo.model.LocalCombineRoomConfig;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.StartCombineRoomByCreateMsg;

import java.util.List;

/**
 * 通过在唱聊房内邀请之后对方同意之后收到的push
 */
public class StartCombineRoomByCreateNotifyEvent {
    public BaseNotiInfo mBaseNotiInfo;
    int roomID; //房间ID
    long createdTimeMs; //房间创建的时间戳
    long passedTimeMs; //房间已经经历的毫秒数
    List<UserInfoModel> users; //玩家信息
    LocalCombineRoomConfig config; //配置信息
    boolean needMaskUserInfo = true; //是否需要隐藏用户信息

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

    public boolean isNeedMaskUserInfo() {
        return needMaskUserInfo;
    }

    public StartCombineRoomByCreateNotifyEvent(BaseNotiInfo baseNotiInfo, StartCombineRoomByCreateMsg startCombineRoomByCreateMsg) {
        mBaseNotiInfo = baseNotiInfo;
        roomID = startCombineRoomByCreateMsg.getRoomID();
        createdTimeMs = startCombineRoomByCreateMsg.getCreatedTimeMs();
        passedTimeMs = startCombineRoomByCreateMsg.getPassedTimeMs();
        users = UserInfoModel.parseFromPB(startCombineRoomByCreateMsg.getUsersList());
        config = LocalCombineRoomConfig.toLocalCombineRoomConfig(startCombineRoomByCreateMsg.getConfig());
        needMaskUserInfo = startCombineRoomByCreateMsg.getNeedMaskUserInfo();
    }
}
