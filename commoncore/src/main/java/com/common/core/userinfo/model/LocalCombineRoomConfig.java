package com.common.core.userinfo.model;

import com.component.live.proto.Common.CombineRoomConfig;

import java.io.Serializable;

public class LocalCombineRoomConfig implements Serializable {
    public int durationTimeMs; //房间持续时间（4000ms）
    String maskMaleAvatar; //隐藏的男性用户头像
    String maskFemaleAvatar; //隐藏的女性用户头像
    String roomSignature; //房间描述签名

    public int getDurationTimeMs() {
        return durationTimeMs;
    }

    public void setDurationTimeMs(int durationTimeMs) {
        this.durationTimeMs = durationTimeMs;
    }

    public String getMaskMaleAvatar() {
        return maskMaleAvatar;
    }

    public void setMaskMaleAvatar(String maskMaleAvatar) {
        this.maskMaleAvatar = maskMaleAvatar;
    }

    public String getMaskFemaleAvatar() {
        return maskFemaleAvatar;
    }

    public void setMaskFemaleAvatar(String maskFemaleAvatar) {
        this.maskFemaleAvatar = maskFemaleAvatar;
    }

    public String getRoomSignature() {
        return roomSignature;
    }

    public void setRoomSignature(String roomSignature) {
        this.roomSignature = roomSignature;
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

    @Override
    public String toString() {
        return "LocalCombineRoomConfig{" +
                "durationTimeMs=" + durationTimeMs +
                ", maskMaleAvatar='" + maskMaleAvatar + '\'' +
                ", maskFemaleAvatar='" + maskFemaleAvatar + '\'' +
                ", roomSignature='" + roomSignature + '\'' +
                '}';
    }
}
