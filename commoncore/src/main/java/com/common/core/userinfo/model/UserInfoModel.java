package com.common.core.userinfo.model;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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

// TODO: 2019/1/2 该类会作为json来解析，不要改变量名
public class UserInfoModel implements Serializable, Cloneable {

    public static final int USER_ID_XIAOZHUSHOU = 4;

    public static final int EF_OFFLINE = 10;      //离线
    public static final int EF_ONLINE = 20;       //在线
    public static final int EF_ONLINE_BUSY = EF_ONLINE + 1;  //在线忙碌中
    public static final int EF_ONLiNE_JOINED = EF_ONLINE + 2;//在线已加入游戏

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
    private Location location;  //展示位置
    private Location location2;  //真实位置
    private String letter;
    private boolean mIsSystem;
    private boolean isFriend;
    private boolean isFollow;
    private int mainLevel; // 主段位
    private int status;    // 状态 在线  离线
    private long statusTs;// 在线或者离线的时间
    private String statusDesc;  //状态描述
    private int ageStage;   // 年龄段（目前只有homepage的接口里面带）

    private int vipType;  // 加v
    private ScoreStateModel ranking;  // 段位描述

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
        String remark = UserInfoManager.getInstance().getRemarkName(userId, nickname);
        return remark;
    }

    public String getNicknameRemark(String defaultName) {
        String remark = UserInfoManager.getInstance().getRemarkName(userId, defaultName);
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

    public Location getLocation2() {
        return location2;
    }

    public void setLoaction2(Location location2) {
        this.location2 = location2;
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

    public int getAgeStage() {
        return ageStage;
    }

    public int getVipType() {
        return vipType;
    }

    public void setVipType(int vipType) {
        this.vipType = vipType;
    }

    public ScoreStateModel getRanking() {
        return ranking;
    }

    public void setRanking(ScoreStateModel ranking) {
        this.ranking = ranking;
    }

    public String getAgeStageString() {
        if (ageStage != 0) {
            if (ageStage == 1) {
                return "小学党";
            } else if (ageStage == 2) {
                return "中学党";
            } else if (ageStage == 3) {
                return "大学党";
            } else if (ageStage == 4) {
                return "工作党";
            }
        }
        return "";
    }

    public void setAgeStage(int ageStage) {
        this.ageStage = ageStage;
    }

    public int getAge() {
        if (!TextUtils.isEmpty(birthday)) {
            String[] array = this.birthday.split("-");
            if (array != null && array.length > 0) {
                if (!TextUtils.isEmpty(array[0])) {
                    int year = Integer.valueOf(array[0]);
                    return Calendar.getInstance().get(Calendar.YEAR) - year;
                }
            }
        }
        return 0;
    }

    public String getConstellation() {
        if (!TextUtils.isEmpty(this.birthday)) {
            String[] array = this.birthday.split("-");
            if (array != null && array.length >= 3) {
                if (!TextUtils.isEmpty(array[1]) && !TextUtils.isEmpty(array[2])) {
                    int month = Integer.valueOf(array[1]);
                    int day = Integer.valueOf(array[2]);
                    return U.getDateTimeUtils().getConstellation(month, day);
                }
            }
        }
        return "";
    }

    public int getMainLevel() {
        return mainLevel;
    }

    public void setMainLevel(int mainLevel) {
        this.mainLevel = mainLevel;
    }

    public long getStatusTs() {
        return statusTs;
    }

    public void setStatusTs(long statusTs) {
        this.statusTs = statusTs;
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
            userInfoModel.setVipType(model.getVipType().getValue());
            userInfoModel.setRanking(ScoreStateModel.Companion.parseFromPB(model.getRanking()));
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
                userInfoDB.setRelative(UserInfoManager.RELATION.FRIENDS.getValue());
            } else if (userInfModel.isFollow()) {
                userInfoDB.setRelative(UserInfoManager.RELATION.FOLLOW.getValue());
            } else {
                userInfoDB.setRelative(UserInfoManager.RELATION.NO_RELATION.getValue());
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("location", userInfModel.getLocation());
            jsonObject.put("location2", userInfModel.getLocation2());
            jsonObject.put("mainLevel", userInfModel.getMainLevel());
            jsonObject.put("vipType",userInfModel.getVipType());
            jsonObject.put("ranking",userInfModel.getRanking());
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
            if (userInDB.getRelative() == UserInfoManager.RELATION.FRIENDS.getValue()) {
                userInfoModel.setFriend(true);
                userInfoModel.setFollow(true);
            } else if (userInDB.getRelative() == UserInfoManager.RELATION.FOLLOW.getValue()) {
                userInfoModel.setFollow(true);
            }

            String extJSon = userInDB.getExt();
            if (!TextUtils.isEmpty(extJSon)) {
                JSONObject jsonObject = JSON.parseObject(extJSon, JSONObject.class);
                Location location = jsonObject.getObject("location", Location.class);
                userInfoModel.setLocation(location);
                Location location2 = jsonObject.getObject("location2", Location.class);
                userInfoModel.setLoaction2(location2);
                int mainLevel = jsonObject.getIntValue("mainLevel");
                userInfoModel.setMainLevel(mainLevel);
                int vipType = jsonObject.getIntValue("vipType");
                userInfoModel.setVipType(vipType);
                ScoreStateModel stateModel = jsonObject.getObject("ranking", ScoreStateModel.class);
                userInfoModel.setRanking(stateModel);
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
                ", location2=" + location2 +
                ", letter='" + letter + '\'' +
                ", mIsSystem=" + mIsSystem +
                ", isFriend=" + isFriend +
                ", isFollow=" + isFollow +
                ", mainLevel=" + mainLevel +
                ", status=" + status +
                ", statusTs=" + statusTs +
                ", statusDesc='" + statusDesc + '\'' +
                ", ageStage=" + ageStage +
                '}';
    }

    public String toSimpleString() {
        return "UserInfoModel{" +
                "userId=" + userId +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
