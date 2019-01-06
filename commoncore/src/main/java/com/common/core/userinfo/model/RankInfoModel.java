package com.common.core.userinfo.model;

// 用来解析排位信息的类
public class RankInfoModel {
    /**
     * userID : 1000166
     * rankSeq : 1
     * avatar : http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/common/avatar_default_1.png
     * nickname : 又是这样
     * starCnt : 1000
     * levelDesc : 王者
     */

    private int userID;
    private int rankSeq;
    private String avatar;
    private String nickname;
    private int starCnt;
    private String levelDesc;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getRankSeq() {
        return rankSeq;
    }

    public void setRankSeq(int rankSeq) {
        this.rankSeq = rankSeq;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getStarCnt() {
        return starCnt;
    }

    public void setStarCnt(int starCnt) {
        this.starCnt = starCnt;
    }

    public String getLevelDesc() {
        return levelDesc;
    }

    public void setLevelDesc(String levelDesc) {
        this.levelDesc = levelDesc;
    }
}
