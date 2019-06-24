package com.common.core.userinfo.model;

import com.zq.live.proto.Common.CombineRoomConfig;

import java.io.Serializable;

public class LocalCombineRoomConfig implements Serializable {
    public int durationTimeMs; //房间持续时间（4000ms）
    String maskMaleAvatar; //隐藏的男性用户头像
    String maskFemaleAvatar; //隐藏的女性用户头像
    String roomSignature; //房间描述签名

    public int getDurationTimeMs() {
        return durationTimeMs;
    }

    public String getMaskMaleAvatar() {
        return maskMaleAvatar;
    }

    public String getMaskFemaleAvatar() {
        return maskFemaleAvatar;
    }

    public String getRoomSignature() {
        return roomSignature;
    }

    private LocalCombineRoomConfig() {

    }

    public static LocalCombineRoomConfig toLocalCombineRoomConfig(CombineRoomConfig combineRoomConfig) {
        LocalCombineRoomConfig localCombineRoomConfig = new LocalCombineRoomConfig();
        localCombineRoomConfig.durationTimeMs = combineRoomConfig.getDurationTimeMs();
        localCombineRoomConfig.roomSignature = combineRoomConfig.getRoomSignature();
        localCombineRoomConfig.maskFemaleAvatar = combineRoomConfig.getMaskFemaleAvatar();
        localCombineRoomConfig.maskMaleAvatar = combineRoomConfig.getMaskMaleAvatar();
        return localCombineRoomConfig;
    }
}
