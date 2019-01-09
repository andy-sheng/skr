package com.module.home.view;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserRankModel;

import model.RelationNumMode;
import model.UserScoreModel;

import java.util.List;

public interface IPersonView {
    // 展示个人基本信息
    void showUserInfo(UserInfoModel userInfoModel);

    // 展示好友，粉丝和关注数量
    void showRelationNum(List<RelationNumMode> list);

    // 展示地区排名
    void showReginRank(List<UserRankModel> list);

    // 展示段位信息
    void showUserScore(List<UserScoreModel> list);
}
