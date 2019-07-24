package com.component.person.view

import com.common.core.userinfo.model.GameStatisModel
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.userinfo.model.UserRankModel

import com.component.person.model.RelationNumModel

import com.common.core.userinfo.model.UserLevelModel

interface IOtherPersonView {
    // 展示homepage回来的结果
    fun showHomePageInfo(userInfoModel: UserInfoModel,
                         relationNumModels: List<RelationNumModel>?,
                         userRankModels: List<UserRankModel>?,
                         userLevelModels: List<UserLevelModel>?,
                         gameStatisModels: List<GameStatisModel>?,
                         isFriend: Boolean, isFollow: Boolean,
                         meiLiCntTotal: Int)

    fun getHomePageFail()
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
