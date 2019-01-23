package com.common.core.userinfo.model;

import java.io.Serializable;

// 用来解析排位信息的类
public class RankInfoModel implements Serializable {

    /**
     * userID : 1002020
     * rankSeq : 1
     * avatar : http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/41a5c168a7a9e945.jpg
     * nickname : 我要灯不要脸
     * starCnt : 0
     * levelDesc : 荣耀歌王
     * mainRanking : 6
     * subRanking : 0
     * maxStar : 0
     */

    private int userID;
    private int rankSeq;
    private String avatar;
    private String nickname;
    private int starCnt;
    private String levelDesc;
    private int mainRanking;
    private int subRanking;
    private int maxStar;

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

    public int getMainRanking() {
        return mainRanking;
    }

    public void setMainRanking(int mainRanking) {
        this.mainRanking = mainRanking;
    }

    public int getSubRanking() {
        return subRanking;
    }

    public void setSubRanking(int subRanking) {
        this.subRanking = subRanking;
    }

    public int getMaxStar() {
        return maxStar;
    }

    public void setMaxStar(int maxStar) {
        this.maxStar = maxStar;
    }


    @Override
    public String toString() {
        return "RankInfoModel{" +
                "userID=" + userID +
                ", rankSeq=" + rankSeq +
                ", avatar='" + avatar + '\'' +
                ", nickname='" + nickname + '\'' +
                ", starCnt=" + starCnt +
                ", levelDesc='" + levelDesc + '\'' +
                ", mainRanking=" + mainRanking +
                ", subRanking=" + subRanking +
                ", maxStar=" + maxStar +
                '}';
    }
}
