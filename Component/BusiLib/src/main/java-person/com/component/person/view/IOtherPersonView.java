package com.component.person.view;

import com.common.core.userinfo.model.GameStatisModel;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserRankModel;

import java.util.List;

import model.RelationNumModel;

import com.common.core.userinfo.model.UserLevelModel;

public interface IOtherPersonView {
    // 展示homepage回来的结果
    void showHomePageInfo(UserInfoModel userInfoModel,
                          List<RelationNumModel> relationNumModels,
                          List<UserRankModel> userRankModels,
                          List<UserLevelModel> userLevelModels,
                          List<GameStatisModel> gameStatisModels,
                          boolean isFriend, boolean isFollow,
                          int meiLiCntTotal);

    void getHomePageFail();
//    // 展示照片墙
//    void addPhotos(List<PhotoModel> list, int newOffset, int totalNum, boolean clear);
//
//    // 照片墙请求出错
//    void addPhotosFail();
//
//    // 个人基本信息
//    void showUserInfo(UserInfoModel model);
//
//    // 展示好友，粉丝和关注数量
//    void showRelationNum(List<RelationNumModel> list);
//
//    // 展示地区排名
//    void showReginRank(List<UserRankModel> list);
//
//    // 展示段位信息
//    void showUserLevel(List<UserLevelModel> list);
//
//    // 和自己的关系
//    void showUserRelation(boolean isFriend, boolean isFollow);
//
//    // 展示游戏数据
//    void showGameStatic(List<GameStatisModel> list);
}
