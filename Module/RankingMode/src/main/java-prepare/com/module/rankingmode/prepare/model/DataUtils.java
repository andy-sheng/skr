package com.module.rankingmode.prepare.model;

import com.common.core.userinfo.UserInfo;

public class DataUtils {
    public static UserInfo parse2UserInfo(com.zq.live.proto.Common.UserInfo pbUserInfo) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(pbUserInfo.userID);
        userInfo.setUserNickname(pbUserInfo.getNickName());
        userInfo.setAvatar(pbUserInfo.getAvatar());
        userInfo.setSex(pbUserInfo.getSex().getValue());
        userInfo.setBirthday(pbUserInfo.getDescription());
        userInfo.setIsSystem(pbUserInfo.getIsSystem() ? 1 : 0);
        return userInfo;
    }
}
