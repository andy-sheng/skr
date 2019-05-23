package com.module.playways.grab.room.inter;

import com.common.core.userinfo.model.UserInfoModel;

import java.util.List;

public interface IGrabInviteView {

    void addInviteModelList(List<UserInfoModel> list, int newOffset);

    void hasInvitedModel(UserInfoModel userInfoModel);

    void finishRefresh();
}
