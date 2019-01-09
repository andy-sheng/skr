package com.zq.person.view;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserRankModel;

import java.util.List;

import model.RelationNumMode;
import model.UserScoreModel;

public interface IOtherPersonView {

    // 个人基本信息
    void showUserInfo(UserInfoModel model);

    // 展示好友，粉丝和关注数量
    void showRelationNum(List<RelationNumMode> list);

    // 展示地区排名
    void showReginRank(List<UserRankModel> list);

    // 展示段位信息
    void showUserScore(List<UserScoreModel> list);

    // 和自己的关系
    void showUserRelation(boolean isFriend, boolean isFollow);
}
