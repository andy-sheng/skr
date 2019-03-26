package com.module.msg.friend;

import com.common.core.userinfo.model.UserInfoModel;

import java.io.Serializable;

public class FriendStatusModel implements Serializable {
    public static final int EF_OnLine = 1; //在线
    public static final int EF_OffLine = 2; //离线

    /**
     * avatar : string
     * isFollow : true
     * isFriend : true
     * nickname : string
     * sex : 0
     * status : 0
     * statusDesc : string
     * userID : 0
     */

    private int userID;
    private String avatar;
    private boolean isFollow;
    private boolean isFriend;
    private String nickname;
    private int sex;
    private int status;
    private String statusDesc;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isIsFollow() {
        return isFollow;
    }

    public void setIsFollow(boolean isFollow) {
        this.isFollow = isFollow;
    }

    public boolean isIsFriend() {
        return isFriend;
    }

    public void setIsFriend(boolean isFriend) {
        this.isFriend = isFriend;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public UserInfoModel toUserInfoModel() {
        UserInfoModel userInfoModel = new UserInfoModel();
        userInfoModel.setUserId(userID);
        userInfoModel.setAvatar(avatar);
        userInfoModel.setSex(sex);
        userInfoModel.setNickname(nickname);
        userInfoModel.setFollow(isFollow);
        userInfoModel.setFriend(isFriend);
        return userInfoModel;
    }

    @Override
    public String toString() {
        return "FriendStatusModel{" +
                "avatar='" + avatar + '\'' +
                ", isFollow=" + isFollow +
                ", isFriend=" + isFriend +
                ", nickname='" + nickname + '\'' +
                ", sex=" + sex +
                ", status=" + status +
                ", statusDesc='" + statusDesc + '\'' +
                ", userID=" + userID +
                '}';
    }
}
