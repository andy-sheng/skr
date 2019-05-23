package com.module.playways.grab.room.invite.view;

import com.common.core.userinfo.model.UserInfoModel;
import com.dialog.view.StrokeTextView;

import java.util.List;

public interface IInviteSearchView {
    void showUserInfoList(List<UserInfoModel> list);

    void updateInvited(StrokeTextView view);
}
