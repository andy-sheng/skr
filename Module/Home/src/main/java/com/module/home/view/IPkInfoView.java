package com.module.home.view;

import com.common.core.userinfo.model.GameStatisModel;
import com.common.core.userinfo.model.UserLevelModel;
import com.common.core.userinfo.model.UserRankModel;

import java.util.List;

public interface IPkInfoView {
    // 展示段位信息
    void showUserLevel(List<UserLevelModel> list);

    // 展示游戏数据
    void showGameStatic(List<GameStatisModel> list);

    // 展示排行数据
    void showRankView(UserRankModel userRankModel);

    // 刷新个人信息
    void refreshBaseInfo();
}
