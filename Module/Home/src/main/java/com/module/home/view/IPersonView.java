package com.module.home.view;

import com.common.core.userinfo.model.UserRankModel;
import model.RelationNumMode;

import java.util.List;

public interface IPersonView {
    // 展示好友，粉丝和关注数量
    void showRelationNum(List<RelationNumMode> list);

    // 展示地区排名
    void showReginRank(List<UserRankModel> list);
}
