package com.module.home.view;

import com.common.core.userinfo.model.GameStatisModel;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserRankModel;

import model.RelationNumModel;

import com.common.core.userinfo.model.UserLevelModel;
import com.common.rxretrofit.ApiResult;
import com.zq.person.model.PhotoModel;

import java.util.List;

public interface IPersonView {
    // 展示homepage回来的结果
    void showHomePageInfo(List<RelationNumModel> relationNumModels,
                          List<UserRankModel> userRankModels,
                          List<UserLevelModel> userLevelModels,
                          List<GameStatisModel> gameStatisModels);

    // 展示好友，粉丝和关注数量
    void showRelationNum(List<RelationNumModel> list);

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
    void showRankView(UserRankModel userRankModel);

    void addPhoto(List<PhotoModel> list, boolean clear, int totalNum);

    void insertPhoto(PhotoModel photoModel);

    void deletePhoto(PhotoModel photoModel);

    void updatePhoto(PhotoModel imageItem);
}
