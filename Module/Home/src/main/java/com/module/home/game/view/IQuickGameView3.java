package com.module.home.game.view;

import com.component.busilib.friends.SpecialModel;
import com.module.home.model.GameKConfigModel;
import com.module.home.model.SlideShowModel;

import java.util.List;

public interface IQuickGameView3 {
    void setBannerImage(List<SlideShowModel> slideShowModelList);

//    void setRecommendInfo(List<RecommendModel> list);

    void setQuickRoom(List<SpecialModel> list, int offset);

//    void setGameConfig(GameKConfigModel gameKConfigModel);

    void showTaskRedDot(boolean show);
}
