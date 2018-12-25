package com.common.core.userinfo;

import com.common.core.myinfo.Location;

public class JsonUserInfo {

    /**
     * userID : 11
     * nickname : 不可抗力好吧
     * sex : 1
     * birthday : 1993-05-06
     * avatar : http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/f488412aad91c18e.jpg
     * signature : 怎么的了哈哈
     * location : {"province":"","city":"","district":""}
     */

    private int userID;
    private String nickname;
    private int sex;
    private String birthday;
    private String avatar;
    private String signature;
    private Location location;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
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

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public static UserInfo toUserInfo(JsonUserInfo jsonUserInfo){
        UserInfo userInfo = new UserInfo();
        if (jsonUserInfo != null){
            userInfo.setUserId(jsonUserInfo.getUserID());
            userInfo.setUserNickname(jsonUserInfo.getNickname());
            userInfo.setSex(jsonUserInfo.getSex());
            userInfo.setBirthday(jsonUserInfo.getBirthday());
            userInfo.setAvatar(jsonUserInfo.getAvatar());
            userInfo.setSignature(jsonUserInfo.getSignature());
        }
        return userInfo;
    }
}
