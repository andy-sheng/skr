package com.module.home.game;

import com.component.busilib.friends.FriendRoomModel;
import com.component.busilib.friends.SpecialModel;
import com.module.home.model.SlideShowModel;

import java.util.List;

public interface IGameView {

    void setBannerImage(List<SlideShowModel> slideShowModelList);

    void setFriendRoom(List<FriendRoomModel> list, int offset, int totalNum);

    void setQuickRoom(List<SpecialModel> list, int offset);
}
