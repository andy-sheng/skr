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

import org.jetbrains.annotations.Nullable;

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
    private boolean isSPFollow;  // 是否特别关注
    @Deprecated
    private int mainLevel; // 主段位 (被废弃了)
    private int status;    // 状态 在线  离线
    private long statusTs;// 在线或者离线的时间
    private String statusDesc;  //状态描述
    private int ageStage;   // 年龄段（目前只有homepage的接口里面带）

    private VerifyInfo vipInfo;  // 加V的信息
    private ScoreStateModel ranking;  // 段位描述
    private HonorInfo honorInfo;      // 会员信息
    private ClubMemberInfo clubInfo;  // 家族信息

    private int intimacy = -1;   // 亲密度

    public int getIntimacy() {
        return intimacy;
    }

    public void setIntimacy(int intimacy) {
        this.intimacy = intimacy;
    }

    public boolean hasIntimacy() {
        return intimacy > 0;
    }

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

    public void setLocation2(Location location2) {
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

    public boolean isSPFollow() {
        return isSPFollow;
    }

    public void setSPFollow(boolean SPFollow) {
        isSPFollow = SPFollow;
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

    public ScoreStateModel getRanking() {
        return ranking;
    }

    public void setRanking(ScoreStateModel ranking) {
        this.ranking = ranking;
    }

    public VerifyInfo getVipInfo() {
        return vipInfo;
    }

    public void setVipInfo(VerifyInfo vipInfo) {
        this.vipInfo = vipInfo;
    }

    public HonorInfo getHonorInfo() {
        return honorInfo;
    }

    public void setHonorInfo(HonorInfo honorInfo) {
        this.honorInfo = honorInfo;
    }

    public ClubMemberInfo getClubInfo() {
        return clubInfo;
    }

    public void setClubInfo(ClubMemberInfo clubInfo) {
        this.clubInfo = clubInfo;
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

    /**
     * 比如你有属性 A B C 有值， info 有属性 C D E 有值。
     * 调用此方法后 你有属性 A B C D E ，且 C 的值以 info 的为准
     * @param info
     */
    public void tryUpdate(@Nullable UserInfoModel info) {
        if(!TextUtils.isEmpty(info.nickname)){
            this.nickname = info.nickname;
        }
        if(info.sex>0){
            this.sex = info.sex;
        }
        if(!TextUtils.isEmpty(info.birthday)){
            this.birthday = info.birthday;
        }
        if(!TextUtils.isEmpty(info.avatar)){
            this.avatar = info.avatar;
        }
        if(!TextUtils.isEmpty(info.signature)){
            this.signature = info.signature;
        }
        if(!TextUtils.isEmpty(info.signature)){
            this.signature = info.signature;
        }
        if(info.location!=null){
            this.location = info.location;
        }
        if(info.location2!=null){
            this.location2 = info.location2;
        }
        if(!TextUtils.isEmpty(info.letter)){
            this.letter = info.letter;
        }
        if(info.mIsSystem == true){
            this.mIsSystem = true;
        }
        if(info.isFriend == true){
            this.isFriend = true;
        }
        if(info.isFollow == true){
            this.isFollow = true;
        }
        if(info.isSPFollow == true){
            this.isSPFollow = true;
        }
        if(info.mainLevel > 0){
            this.mainLevel = info.mainLevel;
        }
        if(info.status > 0){
            this.status = info.status;
        }
        if(info.statusTs > 0){
            this.statusTs = info.statusTs;
        }
        if(!TextUtils.isEmpty(info.statusDesc)){
            this.statusDesc = info.statusDesc;
        }
        if(info.ageStage > 0){
            this.ageStage = info.ageStage;
        }
        if(info.vipInfo != null){
            this.vipInfo = info.vipInfo;
        }
        if(info.ranking != null){
            this.ranking = info.ranking;
        }
        if(info.honorInfo != null){
            this.honorInfo = info.honorInfo;
        }
        if(info.clubInfo != null){
            this.clubInfo = info.clubInfo;
        }
        if(info.intimacy != -1){
            this.intimacy = info.intimacy;
        }
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
            userInfoModel.setVipInfo(VerifyInfo.Companion.parseFromPB(model.getVipInfo()));
            userInfoModel.setRanking(ScoreStateModel.Companion.parseFromPB(model.getUserID(), model.getRanking()));
            userInfoModel.setHonorInfo(HonorInfo.Companion.parseFromPB(model.getUserID(), model.getHonorInfo()));
            userInfoModel.setClubInfo(ClubMemberInfo.Companion.parseFromPB(model.getClubInfo()));
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
            jsonObject.put("isSPFollow", userInfModel.isSPFollow());
            jsonObject.put("location", userInfModel.getLocation());
            jsonObject.put("location2", userInfModel.getLocation2());
            jsonObject.put("mainLevel", userInfModel.getMainLevel());
            jsonObject.put("vipInfo", userInfModel.getVipInfo());
            jsonObject.put("ranking", userInfModel.getRanking());
            jsonObject.put("honorInfo", userInfModel.getHonorInfo());
            jsonObject.put("clubInfo", userInfModel.getClubInfo());
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
                userInfoModel.setSPFollow(jsonObject.getBooleanValue("isSPFollow"));
                userInfoModel.setLocation(jsonObject.getObject("location", Location.class));
                userInfoModel.setLocation2(jsonObject.getObject("location2", Location.class));
                userInfoModel.setMainLevel(jsonObject.getIntValue("mainLevel"));
                userInfoModel.setVipInfo(jsonObject.getObject("vipInfo", VerifyInfo.class));
                userInfoModel.setRanking(jsonObject.getObject("ranking", ScoreStateModel.class));
                userInfoModel.setHonorInfo(jsonObject.getObject("honorInfo", HonorInfo.class));
                userInfoModel.setClubInfo(jsonObject.getObject("clubInfo", ClubMemberInfo.class));
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
                ", isSPFollow=" + isSPFollow +
                ", mainLevel=" + mainLevel +
                ", status=" + status +
                ", statusTs=" + statusTs +
                ", statusDesc='" + statusDesc + '\'' +
                ", ageStage=" + ageStage +
                ", vipInfo=" + vipInfo +
                ", ranking=" + ranking +
                ", honorInfo=" + honorInfo +
                ", clubInfo=" + clubInfo +
                ", intimacy=" + intimacy +
                '}';
    }

    public String toSimpleString() {
        return "UserInfoModel{" +
                "userId=" + userId +
                ", nickname='" + nickname + '\'' +
                '}';
    }


}
