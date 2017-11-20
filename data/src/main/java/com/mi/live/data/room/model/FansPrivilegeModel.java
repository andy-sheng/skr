package com.mi.live.data.room.model;

import com.wali.live.proto.VFansCommonProto;

/**
 * Created by anping on 17/6/23.
 */

public class FansPrivilegeModel {
    public static final int UPGREAD_ACCELERATE_LEVEL = 1;
    public static final int SEND_COLOR_BARRAGE_VIP_LEVEL = 3;
    public static final int SEND_FLY_BARRAGE_LEVEL = 5;
    public static final int GAG_LEVEL = 8;

    private int memType;
    private int vipLevel;
    private int petLevel;
    private long expireTime;
    private String medal;

    private int hasSendFlyBarrageTimes;
    private int maxCanSendFlyBarrageTimes;

    public int getMemType() {
        return memType;
    }

    public void setMemType(int memType) {
        this.memType = memType;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public int getHasSendFlyBarrageTimes() {
        return hasSendFlyBarrageTimes;
    }

    public void setHasSendFlyBarrageTimes(int hasSendFlyBarrageTimes) {
        this.hasSendFlyBarrageTimes = hasSendFlyBarrageTimes;
    }

    public int getMaxCanSendFlyBarrageTimes() {
        return maxCanSendFlyBarrageTimes;
    }

    public void setMaxCanSendFlyBarrageTimes(int maxCanSendFlyBarrageTimes) {
        this.maxCanSendFlyBarrageTimes = maxCanSendFlyBarrageTimes;
    }

    public int getPetLevel() {
        return petLevel;
    }

    public void setPetLevel(int petLevel) {
        this.petLevel = petLevel;
    }

    public String getMedal() {
        return medal;
    }

    public void setMedal(String medal) {
        this.medal = medal;
    }

    public boolean canSendFlyBarrage() {
        return memType != VFansCommonProto.GroupMemType.NONE_VALUE && vipLevel > 0
                && expireTime > System.currentTimeMillis() / 1000
                && petLevel >= SEND_FLY_BARRAGE_LEVEL;
    }

    public boolean canSendColorBarrage() {
        return memType != VFansCommonProto.GroupMemType.NONE_VALUE && petLevel >= SEND_COLOR_BARRAGE_VIP_LEVEL
                && expireTime > System.currentTimeMillis() / 1000 && vipLevel > 0;
    }

    public boolean canGagSomeone() {
        return memType <= VFansCommonProto.GroupMemType.NONE_VALUE || (
                memType != VFansCommonProto.GroupMemType.NONE.getNumber() && petLevel >= GAG_LEVEL
                        && expireTime > System.currentTimeMillis() / 1000 && vipLevel > 0);
    }
}
