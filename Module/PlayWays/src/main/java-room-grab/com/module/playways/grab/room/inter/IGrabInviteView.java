package com.module.playways.grab.room.inter;

import com.module.playways.grab.room.invite.model.GrabFriendModel;

import java.util.List;

public interface IGrabInviteView {
    void addInviteModelList(List<GrabFriendModel> grabFriendModelList, int newOffset);

    void updateInviteModel(GrabFriendModel grabFriendModel);

    void finishRefresh();
}
