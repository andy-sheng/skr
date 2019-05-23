package com.zq.dialog.event;

import com.common.core.userinfo.model.UserInfoModel;

public class ShowEditRemarkEvent {

    UserInfoModel mUserInfoModel;

    public ShowEditRemarkEvent(UserInfoModel userInfoModel) {
        this.mUserInfoModel = userInfoModel;
    }
}
