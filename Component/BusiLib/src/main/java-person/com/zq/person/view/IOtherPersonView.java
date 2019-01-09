package com.zq.person.view;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserRankModel;

import java.util.List;

import model.RelationNumModel;
import model.UserLevelModel;

public interface IOtherPersonView {

    // 个人基本信息
    void showUserInfo(UserInfoModel model);

    // 展示好友，粉丝和关注数量
    void showRelationNum(List<RelationNumModel> list);

    // 展示地区排名
    void showReginRank(List<UserRankModel> list);

    // 展示段位信息
    void showUserLevel(List<UserLevelModel> list);

    // 和自己的关系
    void showUserRelation(boolean isFriend, boolean isFollow);
}
