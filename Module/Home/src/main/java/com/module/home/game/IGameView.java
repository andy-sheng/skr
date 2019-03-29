package com.module.home.game;

import com.common.core.userinfo.model.UserLevelModel;
import com.common.core.userinfo.model.UserRankModel;
import com.component.busilib.friends.FriendRoomModel;
import com.component.busilib.friends.SpecialModel;
import com.module.home.model.SlideShowModel;

import java.util.List;

public interface IGameView {

    void setBannerImage(List<SlideShowModel> slideShowModelList);

    void setFriendRoom(List<FriendRoomModel> list, int offset, int totalNum);

    void setQuickRoom(List<SpecialModel> list, int offset);

    void setRankInfo(UserRankModel userRankModel);

    void setScoreInfo(List<UserLevelModel> userLevelModels);
}
