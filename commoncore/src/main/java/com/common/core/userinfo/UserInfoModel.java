package com.common.core.userinfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.myinfo.Location;

public class UserInfoModel {

    /**
     * userID : 11
     * nickname : 不可抗力好吧
     * sex : 1
     * birthday : 1993-05-06
     * avatar : http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/f488412aad91c18e.jpg
     * signature : 怎么的了哈哈
     * location : {"province":"","city":"","district":""}
     */

    private int userId;
    private String userNickname;
    private int sex;
    private String birthday;
    private String avatar;
    private String signature;
    private Location location;
    private String letter;
    private int mIsSystem;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
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

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public void setIsSystem(int isSystem) {
        mIsSystem = isSystem;
    }

    public int getIsSystem() {
        return mIsSystem;
    }

    public static UserInfoDB toUserInfoDB(UserInfoModel userInfModel) {
        UserInfoDB userInfoDB = new UserInfoDB();
        if (userInfModel != null) {
            userInfoDB.setUserId(userInfModel.getUserId());
            userInfoDB.setUserNickname(userInfModel.getUserNickname());
            userInfoDB.setSex(userInfModel.getSex());
            userInfoDB.setBirthday(userInfModel.getBirthday());
            userInfoDB.setAvatar(userInfModel.getAvatar());
            userInfoDB.setSignature(userInfModel.getSignature());
            userInfoDB.setLetter(userInfModel.getLetter());
            userInfoDB.setIsSystem(userInfModel.getIsSystem());
            Location location = userInfModel.getLocation();
            String locationJsonStr = JSON.toJSONString(location);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("location", locationJsonStr);
            userInfoDB.setExt(jsonObject.toJSONString());
        }
        return userInfoDB;
    }

    public static UserInfoModel parseFromDB(UserInfoDB userInDB) {
        UserInfoModel userInfoModel = new UserInfoModel();
        if (userInDB != null) {
            userInfoModel.setUserId((int) userInDB.getUserId());
            userInfoModel.setUserNickname(userInDB.getUserNickname());
            userInfoModel.setSex(userInDB.getSex());
            userInfoModel.setBirthday(userInDB.getBirthday());
            userInfoModel.setAvatar(userInDB.getAvatar());
            userInfoModel.setSignature(userInDB.getSignature());
            userInfoModel.setLetter(userInDB.getLetter());
            userInfoModel.setIsSystem(userInDB.getIsSystem());
            JSONObject jsonObject = JSON.parseObject(userInDB.getExt(), JSONObject.class);
            String locationJsonStr = jsonObject.getString("location");
            Location location = JSON.parseObject(locationJsonStr, Location.class);
            userInfoModel.setLocation(location);
        }
        return userInfoModel;
    }


}
