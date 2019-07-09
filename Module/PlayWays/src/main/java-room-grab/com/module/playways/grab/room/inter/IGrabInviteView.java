package com.module.playways.grab.room.inter;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.view.ex.ExTextView;
import com.dialog.view.StrokeTextView;

import java.util.List;

public interface IGrabInviteView {

    void addInviteModelList(List<UserInfoModel> list,int oldOffset, int newOffset);

    void updateInvited(ExTextView view);

    void finishRefresh();
}
