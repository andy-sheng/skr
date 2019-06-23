package com.module.home.game.view;

import com.module.home.model.GameKConfigModel;

public interface IGameView3 {
    void setGameConfig(GameKConfigModel gameKConfigModel);

    //显示红包运营为
    void showRedOperationView(GameKConfigModel.HomepagesitefirstBean homepagesitefirstBean);

    void hideRedOperationView();
}
