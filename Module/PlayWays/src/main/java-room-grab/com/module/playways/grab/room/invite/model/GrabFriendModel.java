package com.module.playways.grab.room.invite.model;

import com.zq.live.proto.Common.ESex;

import java.io.Serializable;

public class GrabFriendModel implements Serializable {

    /**
     * avatar : string
     * isOnline : true
     * nickName : string
     * sex : unknown
     * status : EF_UNKNOWN
     * userID : 0
     */

    private String avatar;
    private boolean isOnline;
    private String nickName;
    private int sex;
    private int status;
    private int userID;
    private boolean isInvited;

    public boolean isInvited() {
        return isInvited;
    }

    public void setInvited(boolean invited) {
        isInvited = invited;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isIsOnline() {
        return isOnline;
    }

    public boolean getIsMale() {
        return sex == ESex.SX_MALE.getValue();
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
}
