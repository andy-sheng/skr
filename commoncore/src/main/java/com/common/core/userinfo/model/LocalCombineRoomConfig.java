package com.common.core.userinfo.model;

import com.zq.live.proto.Common.CombineRoomConfig;

import java.io.Serializable;

public class LocalCombineRoomConfig implements Serializable {
    int durationTimeMs; //房间持续时间（4000ms）
    String roomSignature; //房间描述签名
    String maskUserNickname; //隐藏的用户昵称
    String maskUserAvatar; //隐藏的用户头像

    private LocalCombineRoomConfig() {

    }

    public static LocalCombineRoomConfig toLocalCombineRoomConfig(CombineRoomConfig combineRoomConfig) {
        LocalCombineRoomConfig localCombineRoomConfig = new LocalCombineRoomConfig();
        localCombineRoomConfig.durationTimeMs = combineRoomConfig.getDurationTimeMs();
        localCombineRoomConfig.roomSignature = combineRoomConfig.getRoomSignature();
        localCombineRoomConfig.maskUserNickname = combineRoomConfig.getMaskUserNickname();
        localCombineRoomConfig.maskUserAvatar = combineRoomConfig.getMaskUserAvatar();
        return localCombineRoomConfig;
    }
}
