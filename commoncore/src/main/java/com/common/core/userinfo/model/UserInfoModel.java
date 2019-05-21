package com.common.core.userinfo.model;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.Location;
import com.common.core.userinfo.UserInfoDB;
import com.common.core.userinfo.UserInfoManager;
import com.common.utils.U;
import com.zq.live.proto.Common.ESex;
import com.zq.live.proto.Common.UserInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.common.core.userinfo.UserInfoLocalApi.INTER_FOLLOW;
import static com.common.core.userinfo.UserInfoLocalApi.ONE_FOLLOW;
import static com.common.core.userinfo.UserInfoLocalApi.UN_FOLLOW;

// TODO: 2019/1/2 该类会作为json来解析，不要改变量名 
public class UserInfoModel implements Serializable, Cloneable {

    public static final int EF_OnLine = 1; //在线
    public static final int EF_OffLine = 2; //离线

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
    private String nickname;
    private int sex;
    private String birthday;
    private String avatar;
    private String signature;
    private Location location;
    private String letter;
    private boolean mIsSystem;
    private boolean isFriend;
    private boolean isFollow;
    private int mainLevel; // 主段位
    private int status;    // 状态
    private String statusDesc;  //状态描述

    public UserInfoModel() {
    }

    public UserInfoModel(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String userNickname) {
        this.nickname = userNickname;
    }

    public String getNicknameRemark() {
        String remark = UserInfoManager.getInstance().getRemarkName(userId,nickname);
        return remark;
    }

    public String getNicknameRemark(String defaultName) {
        String remark = UserInfoManager.getInstance().getRemarkName(userId,defaultName);
        return remark;
    }

    public int getSex() {
        return sex;
    }

    public boolean getIsMale() {
        return sex == ESex.SX_MALE.getValue();
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

    public void setIsSystem(boolean isSystem) {
        mIsSystem = isSystem;
    }

    public boolean getIsSystem() {
        return mIsSystem;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public boolean isFollow() {
        return isFollow;
    }

    public void setFollow(boolean follow) {
        isFollow = follow;
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

    public int getAge() {
        String[] array = this.birthday.split("-");
        if (!TextUtils.isEmpty(array[0])) {
            int year = Integer.valueOf(array[0]);
            return Calendar.getInstance().get(Calendar.YEAR) - year;
        }
        return 0;
    }

    public String getConstellation() {
        String[] array = this.birthday.split("-");
        if (!TextUtils.isEmpty(array[1]) && !TextUtils.isEmpty(array[2])) {
            int month = Integer.valueOf(array[1]);
            int day = Integer.valueOf(array[2]);
            return U.getDateTimeUtils().getConstellation(month, day);
        }
        return "";
    }

    public int getMainLevel() {
        return mainLevel;
    }

    public void setMainLevel(int mainLevel) {
        this.mainLevel = mainLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfoModel that = (UserInfoModel) o;

        return userId == that.userId;
    }

    @Override
    public int hashCode() {
        return userId;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        UserInfoModel userInfoModel = null;
        try {
            userInfoModel = (UserInfoModel) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return userInfoModel;
    }

    public static List<UserInfoModel> parseFromPB(List<UserInfo> userInfoList) {
        ArrayList<UserInfoModel> modelArrayList = new ArrayList<>();
        if (userInfoList == null) {
            return modelArrayList;
        }

        for (int i = 0; i < userInfoList.size(); i++) {
            modelArrayList.add(parseFromPB(userInfoList.get(i)));
        }

        return modelArrayList;
    }

    public static UserInfoModel parseFromPB(UserInfo model) {
        UserInfoModel userInfoModel = new UserInfoModel();
        if (model != null) {
            userInfoModel.setUserId(model.getUserID());
            userInfoModel.setNickname(model.getNickName());
//            String remarkName = UserInfoManager.getInstance().getRemarkName(model.getUserID(),null);
//            if(!TextUtils.isEmpty(remarkName)){
//                userInfoModel.setNicknameRemark(remarkName);
//            }
            userInfoModel.setSex(model.getSex().getValue());
            userInfoModel.setAvatar(model.getAvatar());
            userInfoModel.setSignature(model.getDescription());
            userInfoModel.setIsSystem(model.getIsSystem());
            userInfoModel.setMainLevel(model.getMainLevel());
        }
        return userInfoModel;
    }

    public static UserInfoDB toUserInfoDB(UserInfoModel userInfModel) {
        UserInfoDB userInfoDB = new UserInfoDB();
        if (userInfModel != null) {
            userInfoDB.setUserId(Long.valueOf((long) userInfModel.getUserId()));
            userInfoDB.setUserNickname(userInfModel.getNickname());
            userInfoDB.setUserDisplayname(userInfModel.getNicknameRemark(null));
            userInfoDB.setSex(userInfModel.getSex());
            userInfoDB.setBirthday(userInfModel.getBirthday());
            userInfoDB.setAvatar(userInfModel.getAvatar());
            userInfoDB.setSignature(userInfModel.getSignature());
            userInfoDB.setLetter(userInfModel.getLetter());
            userInfoDB.setIsSystem(userInfModel.getIsSystem() ? 1 : 0);

            if (userInfModel.isFriend()) {
                userInfoDB.setRelative(INTER_FOLLOW);
            } else if (userInfModel.isFollow()) {
                userInfoDB.setRelative(ONE_FOLLOW);
            } else {
                userInfoDB.setRelative(UN_FOLLOW);
            }

            JSONObject jsonObject = new JSONObject();
            Location location = userInfModel.getLocation();
            jsonObject.put("location", location);
            jsonObject.put("mainLevel", userInfModel.getMainLevel());
            jsonObject.put("status", userInfModel.getStatus());
            jsonObject.put("statusDesc", userInfModel.getStatusDesc());
            userInfoDB.setExt(jsonObject.toJSONString());
        }
        return userInfoDB;
    }

    public static UserInfoModel parseFromDB(UserInfoDB userInDB) {
        UserInfoModel userInfoModel = new UserInfoModel();
        if (userInDB != null) {
            userInfoModel.setUserId(userInDB.getUserId().intValue());
            userInfoModel.setNickname(userInDB.getUserNickname());
//            userInfoModel.setNicknameRemark(userInDB.getUserDisplayname());

            userInfoModel.setSex(userInDB.getSex());
            userInfoModel.setBirthday(userInDB.getBirthday());
            userInfoModel.setAvatar(userInDB.getAvatar());
            userInfoModel.setSignature(userInDB.getSignature());
            userInfoModel.setLetter(userInDB.getLetter());
            userInfoModel.setIsSystem(userInDB.getIsSystem() == 1);
            String extJSon = userInDB.getExt();
            if (!TextUtils.isEmpty(extJSon)) {
                JSONObject jsonObject = JSON.parseObject(extJSon, JSONObject.class);
                Location location = jsonObject.getObject("location", Location.class);
                userInfoModel.setLocation(location);
                int mainLevel = jsonObject.getIntValue("mainLevel");
                userInfoModel.setMainLevel(mainLevel);
                int status = jsonObject.getIntValue("status");
                userInfoModel.setStatus(status);
                String statusDesc = jsonObject.getString("statusDesc");
                userInfoModel.setStatusDesc(statusDesc);
            }
        }
        return userInfoModel;
    }

    @Override
    public String toString() {
        return "UserInfoModel{" +
                "userId=" + userId +
                ", nickname='" + nickname + '\'' +
                ", sex=" + sex +
                ", birthday='" + birthday + '\'' +
                ", avatar='" + avatar + '\'' +
                ", signature='" + signature + '\'' +
                ", location=" + location +
                ", letter='" + letter + '\'' +
                ", mIsSystem=" + mIsSystem +
                ", isFriend=" + isFriend +
                ", isFollow=" + isFollow +
                ", mainLevel=" + mainLevel +
                ", status=" + status +
                ", statusDesc='" + statusDesc + '\'' +
                '}';
    }

    public String toSimpleString() {
        return "UserInfoModel{" +
                "userId=" + userId +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
