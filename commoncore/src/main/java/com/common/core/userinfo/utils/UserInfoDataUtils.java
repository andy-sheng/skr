package com.common.core.userinfo.utils;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.common.core.myinfo.MyUserInfo;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;

import java.util.ArrayList;
import java.util.List;

public class UserInfoDataUtils {
    /**
     * 如果 newUserInfo 中字段为空，则用userInfoDB的字段来填充
     * 防止 空数据写入数据库
     *
     * @param newUserInfo
     * @param userInfoDB
     */
    public static void fill(UserInfoModel newUserInfo, UserInfoModel userInfoDB) {
        /**
         * 对数据中更新数据进行校验
         *
         * @param userInfoDB 数据库存储
         */
        if (newUserInfo.getUserId() == -1) {
            newUserInfo.setUserId(userInfoDB.getUserId());
        }

        if (newUserInfo.getAvatar() == null) {
            newUserInfo.setAvatar(userInfoDB.getAvatar());
        }

        if (newUserInfo.getNickname() == null) {
            newUserInfo.setNickname(userInfoDB.getNickname());
        }

        if (newUserInfo.getSex() == -1) {
            newUserInfo.setSex(userInfoDB.getSex());
        }

        if (TextUtils.isEmpty(newUserInfo.getBirthday())) {
            newUserInfo.setBirthday(userInfoDB.getBirthday());
        }

        if (TextUtils.isEmpty(newUserInfo.getSignature())) {
            newUserInfo.setSignature(userInfoDB.getSignature());
        }

        if (newUserInfo.getLocation() == null) {
            newUserInfo.setLocation(userInfoDB.getLocation());
        }
        if (newUserInfo.getLocation2() == null) {
            newUserInfo.setLoaction2(userInfoDB.getLocation2());
        }
        if (newUserInfo.getLetter() == null) {
            newUserInfo.setLetter(userInfoDB.getLetter());
        }
    }


    public static void fill(MyUserInfo newUserInfo, MyUserInfo userInfoDB) {
        /**
         * 对数据中更新数据进行校验
         *
         * @param userInfoDB 数据库存储
         */
        if (newUserInfo.getUserId() == -1) {
            newUserInfo.setUserId(userInfoDB.getUserId());
        }

        if (newUserInfo.getAvatar() == null) {
            newUserInfo.setAvatar(userInfoDB.getAvatar());
        }

        if (newUserInfo.getUserNickname() == null) {
            newUserInfo.setUserNickname(userInfoDB.getUserNickname());
        }

        if (newUserInfo.getUserDisplayname() == null) {
            newUserInfo.setUserDisplayname(userInfoDB.getUserDisplayname());
        }

        if (newUserInfo.getSex() == -1) {
            newUserInfo.setSex(userInfoDB.getSex());
        }

        if (TextUtils.isEmpty(newUserInfo.getBirthday())) {
            newUserInfo.setBirthday(userInfoDB.getBirthday());
        }

        if (TextUtils.isEmpty(newUserInfo.getSignature())) {
            newUserInfo.setSignature(userInfoDB.getSignature());
        }

        if (TextUtils.isEmpty(newUserInfo.getPhoneNum())) {
            newUserInfo.setPhoneNum(userInfoDB.getPhoneNum());
        }

        if (newUserInfo.getLocation2() == null) {
            newUserInfo.setLocation2(userInfoDB.getLocation2());
        }
    }


    /**
     * 注意必须保持 属性名的一致
     *
     * @param list
     * @return
     */
    public static List<UserInfoModel> parseRoomUserInfo(List<JSONObject> list) {
        if (list != null && list.size() > 0) {
            List<UserInfoModel> userInfoModels = new ArrayList<>();
            for (JSONObject jsonObject : list) {
                UserInfoModel userInfoModel = new UserInfoModel();
                userInfoModel.setUserId(jsonObject.getIntValue("userID"));
                userInfoModel.setAvatar(jsonObject.getString("avatar"));
                userInfoModel.setFollow(jsonObject.getBooleanValue("isFollow"));
                userInfoModel.setFriend(jsonObject.getBooleanValue("isFriend"));
                userInfoModel.setNickname(jsonObject.getString("nickname"));
                userInfoModel.setSex(jsonObject.getIntValue("sex"));

                int statusFromServer = jsonObject.getIntValue("status");
                boolean isOnline = jsonObject.getBooleanValue("isOnline");
                int status = 0;
                if (isOnline) {
                    // status: 1.可邀请 2.忙碌中 3.已加入游戏
                    if (statusFromServer == 2) {
                        status = UserInfoModel.EF_ONLINE_BUSY;
                        userInfoModel.setStatusDesc("忙碌中");
                    } else if (statusFromServer == 3) {
                        status = UserInfoModel.EF_ONLiNE_JOINED;
                        userInfoModel.setStatusDesc("已加入游戏");
                    } else {
                        status = UserInfoModel.EF_ONLINE;
                        userInfoModel.setStatusDesc("可邀请");
                    }
                } else {
                    status = UserInfoModel.EF_OFFLINE;
                    String timeDesc = "";
                    long offlineTime = jsonObject.getLongValue("offlineTime");
                    if (offlineTime > 0) {
                        timeDesc = U.getDateTimeUtils().formatHumanableDateForSkr(offlineTime, System.currentTimeMillis());
                    }
                    // 显示
                    userInfoModel.setStatusDesc("离线 " + timeDesc);
                }
                userInfoModel.setStatus(status);
                userInfoModels.add(userInfoModel);
            }
            return userInfoModels;
        }

        return null;
    }
}
