package com.module.home.view;

import com.component.person.model.RelationNumModel;

import java.util.List;

public interface IPersonView {
    // 展示homepage回来的结果
    void showHomePageInfo(List<RelationNumModel> relationNumModels,
                          int meiLiCntTotal);

    void loadHomePageFailed();

    // 展示好友，粉丝和关注数量
//    void showRelationNum(List<RelationNumModel> list);

//    // 展示个人基本信息
//    void showUserInfo(UserInfoModel userInfoModel);
//
//    // 展示地区排名
//    void showReginRank(List<UserRankModel> list);
//
//    // 展示段位信息
//    void showUserLevel(List<UserLevelModel> list);
//
//    // 展示游戏数据
//    void showGameStatic(List<GameStatisModel> list);

    // 展示排行数据
//    void showRankView(UserRankModel userRankModel);
}
