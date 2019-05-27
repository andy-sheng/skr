package com.common.core.myinfo;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.common.core.userinfo.UserInfoDB;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;

import java.io.Serializable;


/**
 * 除了个人基本信息外
 * 还有许多额外信息，存在 UserInfo 的ext中？
 */
public class MyUserInfo implements Serializable {

    private long userId; // userId
    private String avatar;   // 头像时间戳
    private String userNickname;    // 昵称
    private String userDisplayname; // 备注
    private long updateTime = -1;        //更新时间，水位
    private int sex = -1;         // 性别
    private String birthday;      // 生日
    private String signature;     // 签名

    private Location location;

    private String phoneNum;
    private String ext; //待扩展


    public MyUserInfo() {
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar == null ? "" : avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getUserDisplayname() {
        return userDisplayname;
    }

    public void setUserDisplayname(String userDisplayname) {
        this.userDisplayname = userDisplayname;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }


    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public static MyUserInfo parseFromUserInfoModel(UserInfoModel userInfoModel) {
        MyUserInfo myUserInfo = new MyUserInfo();
        myUserInfo.setUserId(userInfoModel.getUserId());
        myUserInfo.setUserNickname(userInfoModel.getNickname());
        myUserInfo.setAvatar(userInfoModel.getAvatar());
        myUserInfo.setBirthday(userInfoModel.getBirthday());
        myUserInfo.setLocation(userInfoModel.getLocation());
        myUserInfo.setSex(userInfoModel.getSex());
        myUserInfo.setSignature(userInfoModel.getSignature());
        myUserInfo.setUserDisplayname(userInfoModel.getNickname());
        return myUserInfo;
    }

    public static UserInfoDB toUserInfoDB(MyUserInfo myUserInfo) {
        UserInfoDB userInfoDB = new UserInfoDB();
        if (myUserInfo != null) {
            userInfoDB.setUserId(myUserInfo.getUserId());
            userInfoDB.setAvatar(myUserInfo.getAvatar());
            userInfoDB.setUserNickname(myUserInfo.getUserNickname());
            userInfoDB.setUserDisplayname(myUserInfo.getUserDisplayname());

            userInfoDB.setSex(myUserInfo.getSex());
            userInfoDB.setBirthday(myUserInfo.getBirthday());
            userInfoDB.setSignature(myUserInfo.getSignature());

            JSONObject jsonObject = new JSONObject();
            try {
                Location location = myUserInfo.getLocation();
                if (location != null) {
                    jsonObject.put("location", location);
                }
                jsonObject.put("phoneNum", myUserInfo.getPhoneNum());
                jsonObject.put("ext", myUserInfo.getExt());

            } catch (JSONException e) {
                MyLog.d(e);
            }
            userInfoDB.setExt(jsonObject.toJSONString());
        }
        return userInfoDB;
    }

    public static MyUserInfo parseFromDB(UserInfoDB userInDB) {
        MyUserInfo myInfoModel = new MyUserInfo();
        if (userInDB != null) {
            myInfoModel.setUserId(userInDB.getUserId().intValue());
            myInfoModel.setAvatar(userInDB.getAvatar());
            myInfoModel.setUserNickname(userInDB.getUserNickname());
            myInfoModel.setUserDisplayname(userInDB.getUserDisplayname());

            myInfoModel.setSex(userInDB.getSex());
            myInfoModel.setBirthday(userInDB.getBirthday());
            myInfoModel.setSignature(userInDB.getSignature());

            String extJSon = userInDB.getExt();
            if (!TextUtils.isEmpty(extJSon)) {
                JSONObject jsonObject = JSON.parseObject(extJSon, JSONObject.class);
                String phoneNum = jsonObject.getString("phoneNum");
                myInfoModel.setPhoneNum(phoneNum);
                String ext = jsonObject.getString("ext");
                myInfoModel.setExt(ext);

                Location location = jsonObject.getObject("location", Location.class);
                myInfoModel.setLocation(location);
            }
        }
        return myInfoModel;
    }

    @Override
    public String toString() {
        return "MyUserInfo{" +
                "userId=" + userId +
                ", avatar='" + avatar + '\'' +
                ", userNickname='" + userNickname + '\'' +
                ", userDisplayname='" + userDisplayname + '\'' +
                ", updateTime=" + updateTime +
                ", sex=" + sex +
                ", birthday='" + birthday + '\'' +
                ", signature='" + signature + '\'' +
                ", location=" + location +
                ", phoneNum='" + phoneNum + '\'' +
                ", ext='" + ext + '\'' +
                '}';
    }
}
