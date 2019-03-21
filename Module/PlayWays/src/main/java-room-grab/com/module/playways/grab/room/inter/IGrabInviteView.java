package com.module.playways.grab.room.inter;

import com.module.playways.grab.room.model.GrabFriendModel;
import java.util.List;

public interface IGrabInviteView {
    void updateFriendList(List<GrabFriendModel> grabFriendModelList);

    void hasMore(boolean hasMore);

    void finishRefresh();
}
