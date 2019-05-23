package com.module.playways.grab.room.inter;

import com.common.core.userinfo.model.UserInfoModel;
import com.dialog.view.StrokeTextView;

import java.util.List;

public interface IGrabInviteView {

    void addInviteModelList(List<UserInfoModel> list, int newOffset);

    void updateInvited(StrokeTextView view);

    void finishRefresh();
}
