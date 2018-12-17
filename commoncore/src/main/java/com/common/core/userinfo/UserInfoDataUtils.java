package com.common.core.userinfo;

import android.text.TextUtils;

public class UserInfoDataUtils {
    /**
     * 如果 newUserInfo 中字段为空，则用userInfoDB的字段来填充
     * 防止 空数据写入数据库
     *
     * @param newUserInfo
     * @param userInfoDB
     */
    public static void fill(UserInfo newUserInfo, UserInfo userInfoDB) {
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

        if (newUserInfo.getLetter() == null) {
            newUserInfo.setLetter(userInfoDB.getLetter());
        }

        if (newUserInfo.getBlock() == -1) {
            newUserInfo.setBlock(userInfoDB.getBlock());
        }

        if (newUserInfo.getSex() == -1) {
            newUserInfo.setSex(userInfoDB.getSex());
        }

        if (newUserInfo.getBlock() == -1) {
            newUserInfo.setBlock(userInfoDB.getBlock());
        }

        if (newUserInfo.getExt() == null) {
            newUserInfo.setExt(userInfoDB.getExt());
        }
    }
}
