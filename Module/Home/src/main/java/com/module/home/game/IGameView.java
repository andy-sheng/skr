package com.module.home.game;

import com.component.busilib.friends.RecommendModel;
import com.component.busilib.friends.SpecialModel;
import com.module.home.model.GameKConfigModel;
import com.module.home.model.SlideShowModel;

import java.util.List;

public interface IGameView {

    void setBannerImage(List<SlideShowModel> slideShowModelList);

    void setRecommendInfo(List<RecommendModel> list);

    void setQuickRoom(List<SpecialModel> list, int offset);

    void setGameConfig(GameKConfigModel gameKConfigModel);

    void setGrabCoinNum(int coinNum);

    //显示红包运营为
    void showRedOperationView(GameKConfigModel.HomepagesitefirstBean homepagesitefirstBean);

    void hideRedOperationView();

    void showTaskRedDot(boolean show);
}
