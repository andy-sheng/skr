package com.common.core.userinfo;

import android.text.TextUtils;

import com.common.core.myinfo.MyUserInfo;

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

        if (TextUtils.isEmpty(newUserInfo.getBirthday())){
            newUserInfo.setBirthday(userInfoDB.getBirthday());
        }

        if (TextUtils.isEmpty(newUserInfo.getSignature())){
            newUserInfo.setSignature(userInfoDB.getSignature());
        }

        if (newUserInfo.getLocation() == null){
            newUserInfo.setLocation(userInfoDB.getLocation());
        }
        if (newUserInfo.getLetter()==null){
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

        if (TextUtils.isEmpty(newUserInfo.getBirthday())){
            newUserInfo.setBirthday(userInfoDB.getBirthday());
        }

        if (TextUtils.isEmpty(newUserInfo.getSignature())){
            newUserInfo.setSignature(userInfoDB.getSignature());
        }

        if (TextUtils.isEmpty(newUserInfo.getPhoneNum())){
            newUserInfo.setPhoneNum(userInfoDB.getPhoneNum());
        }
    }
}
