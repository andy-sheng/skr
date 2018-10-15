package com.wali.live.watchsdk.fans.rank.data;

import com.mi.live.data.data.UserListData;
import com.wali.live.proto.VFansProto;

/**
 * Created by zhaomin on 17-6-13.
 */
public class RankFansData extends UserListData {
    private int exp;  // 经验值或者魅力值
    private int level;  // 魅力值或者宠爱 等级
    private int memType; // 成员类型
    private String medalName; // 等级对应的勋章名字
    private int rankNum; // 排名
    private int catchUpExp; // 和上一名的差距
    private String groupName; // 宠爱团名称
    private boolean isGroupRank;// 是否是团排名

    public RankFansData() {
    }

    public RankFansData(VFansProto.MemberInfo info) {
        userId = info.getUuid();
        userNickname = info.getNickname();
        avatar = info.getAvatar();
        exp = info.getPetExp();
        level = info.getPetLevel();
        memType = info.getMemType().getNumber();
        medalName = info.getMedalValue();
        isFollowing = info.getIsFollowing();
        isBothway = info.getIsBothfollowing();
        isGroupRank = false;
    }

    public RankFansData(VFansProto.GroupRankInfo info) {
        userId = info.getZuid();
        userNickname = info.getGroupName();
        avatar = info.getAvatar();
        exp = info.getCharmExp();
        medalName = info.getCharmTitle();
        isFollowing = info.getIsFollowing();
        isBothway = info.getIsBothfollowing();
        level = info.getCharmLevel();
        isGroupRank = true;
    }

    public RankFansData(int petExp, int petLevel, String medalName, int rankNum, int catchUpExp, String groupName) {
        this.exp = petExp;
        this.level = petLevel;
        this.medalName = medalName;
        this.rankNum = rankNum;
        this.catchUpExp = catchUpExp;
        this.groupName = groupName;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMemType() {
        return memType;
    }

    public void setMemType(int memType) {
        this.memType = memType;
    }

    public String getMedalName() {
        return medalName;
    }

    public void setMedalName(String medalName) {
        this.medalName = medalName;
    }

    public int getRankNum() {
        return rankNum;
    }

    public void setRankNum(int rankNum) {
        this.rankNum = rankNum;
    }

    public int getCatchUpExp() {
        return catchUpExp;
    }

    public void setCatchUpExp(int catchUpExp) {
        this.catchUpExp = catchUpExp;
    }

    public boolean isGroupRank() {
        return isGroupRank;
    }

    public void setGroupRank(boolean groupRank) {
        isGroupRank = groupRank;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
