package com.module.home.view;

import com.module.home.model.RelationNumMode;

import java.util.List;

public interface IPersonView {
    // 展示好友，粉丝和关注数量
    void showRelationNum(List<RelationNumMode> list);
}
