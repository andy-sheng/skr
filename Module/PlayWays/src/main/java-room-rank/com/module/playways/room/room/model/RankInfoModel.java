package com.module.playways.room.room.model;

import java.io.Serializable;

// 用来解析排位信息的类
public class RankInfoModel implements Serializable {

    /**
     * userID : 1924768
     * rankSeq : 2
     * avatar : http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/common/avatar_default_2.png
     * nickname : llLINE
     * starCnt : 4
     * levelDesc : 铂金唱将I
     * mainRanking : 4
     * subRanking : 1
     * maxStar : 5
     * sex : 1
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
    private int sex;

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

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }
}
