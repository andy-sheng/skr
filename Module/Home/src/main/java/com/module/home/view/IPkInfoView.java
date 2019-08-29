package com.module.home.view;

import com.common.core.userinfo.model.UserRankModel;
import com.component.person.model.ScoreStateModel;

import java.util.List;

public interface IPkInfoView {
    // 展示段位信息
    void showUserLevel(ScoreStateModel model);

    // 展示游戏数据
    void showGameStatic(long raceTicketCnt, long standLightCnt);

    // 展示排行数据
    void showRankView(UserRankModel userRankModel);

    // 刷新个人信息
    void refreshBaseInfo();
}
