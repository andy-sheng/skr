package com.module.msg.follow;

import java.io.Serializable;

public class LastFollowModel implements Serializable {
    public static final int RELATION_ME_FOLLOW = 1;         //我关注的relation
    public static final int RELATION_FOLLOW_ME = 2;       //关注的relation

    /**
     * userID : 1134740
     * avatar : http://res-static.inframe.mobi/common/avatar_default_2.png
     * nickname : 又又又又子
     * timeMs : 0
     * isFriend : true
     * isFollow : true
     * sex : 2
     * statusDesc : 2019-03-26 我关注了ta
     * relation : 1
     */

    private int userID;
    private String avatar;
    private String nickname;
    private long timeMs;
    private boolean isFriend;
    private boolean isFollow;
    private int sex;
    private String statusDesc;
    private int relation;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
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

    public long getTimeMs() {
        return timeMs;
    }

    public void setTimeMs(long timeMs) {
        this.timeMs = timeMs;
    }

    public boolean isIsFriend() {
        return isFriend;
    }

    public void setIsFriend(boolean isFriend) {
        this.isFriend = isFriend;
    }

    public boolean isIsFollow() {
        return isFollow;
    }

    public void setIsFollow(boolean isFollow) {
        this.isFollow = isFollow;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public int getRelation() {
        return relation;
    }

    public void setRelation(int relation) {
        this.relation = relation;
    }

    @Override
    public String toString() {
        return "LastFollowModel{" +
                "userID=" + userID +
                ", avatar='" + avatar + '\'' +
                ", nickname='" + nickname + '\'' +
                ", timeMs=" + timeMs +
                ", isFriend=" + isFriend +
                ", isFollow=" + isFollow +
                ", sex=" + sex +
                ", statusDesc='" + statusDesc + '\'' +
                ", relation=" + relation +
                '}';
    }
}
