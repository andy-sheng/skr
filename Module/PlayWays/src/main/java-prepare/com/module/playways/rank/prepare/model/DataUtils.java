package com.module.playways.rank.prepare.model;

import com.common.core.userinfo.model.UserInfoModel;

public class DataUtils {
    public static UserInfoModel parse2UserInfo(com.zq.live.proto.Common.UserInfo pbUserInfo) {
        UserInfoModel userInfo = new UserInfoModel();
        userInfo.setUserId(pbUserInfo.getUserID());
        userInfo.setNickname(pbUserInfo.getNickName());
        userInfo.setAvatar(pbUserInfo.getAvatar());
        userInfo.setSex(pbUserInfo.getSex().getValue());
        userInfo.setBirthday(pbUserInfo.getDescription());
        userInfo.setIsSystem(pbUserInfo.getIsSystem() ? 1 : 0);
        return userInfo;
    }
}
