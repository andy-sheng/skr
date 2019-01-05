package com.zq.person.view;

import com.common.core.userinfo.model.UserInfoModel;

public interface IOtherPersonView {

    void showUserInfo(UserInfoModel model);

    void showUserRelation(boolean isFriend, boolean isFollow);
}
