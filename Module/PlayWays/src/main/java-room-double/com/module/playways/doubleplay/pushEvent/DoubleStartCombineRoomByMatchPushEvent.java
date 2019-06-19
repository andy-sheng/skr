package com.module.playways.doubleplay.pushEvent;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.doubleplay.pbLocalModel.LocalAgoraTokenInfo;
import com.module.playways.doubleplay.pbLocalModel.LocalCombineRoomConfig;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.CombineRoom.StartCombineRoomByMatchMsg;

import java.util.List;

public class DoubleStartCombineRoomByMatchPushEvent {
    BasePushInfo basePushInfo;
    int roomID; //房间ID
    long createdTimeMs; //房间创建的时间戳
    long passedTimeMs; //房间已经经历的毫秒数
    List<UserInfoModel> users; //玩家信息
    LocalCombineRoomConfig config; //配置信息
    List<LocalAgoraTokenInfo> tokens; //声网token
    boolean needMaskUserInfo; //是否需要隐藏用户信息

    public DoubleStartCombineRoomByMatchPushEvent(BasePushInfo basePushInfo, StartCombineRoomByMatchMsg startCombineRoomByMatchMsg) {
        this.basePushInfo = basePushInfo;
        this.roomID = startCombineRoomByMatchMsg.getRoomID();
        this.createdTimeMs = startCombineRoomByMatchMsg.getCreatedTimeMs();
        this.passedTimeMs = startCombineRoomByMatchMsg.getPassedTimeMs();
        users = UserInfoModel.parseFromPB(startCombineRoomByMatchMsg.getUsersList());
        config = LocalCombineRoomConfig.toLocalCombineRoomConfig(startCombineRoomByMatchMsg.getConfig());
        tokens = LocalAgoraTokenInfo.toLocalAgoraTokenInfo(startCombineRoomByMatchMsg.getTokensList());
        needMaskUserInfo = startCombineRoomByMatchMsg.getNeedMaskUserInfo();
    }
}
