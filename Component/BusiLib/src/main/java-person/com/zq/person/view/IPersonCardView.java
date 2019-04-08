package com.zq.person.view;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.model.UserLevelModel;
import com.zq.person.model.PhotoModel;

import java.util.List;

public interface IPersonCardView {

    // 个人基本信息
    void showUserInfo(UserInfoModel model);

    // 展示段位信息
    void showUserLevel(List<UserLevelModel> list);

    // 和自己的关系
    void showUserRelation(boolean isFriend, boolean isFollow);

    // 展示照片
    void showPhotos(List<PhotoModel> list, int offset);
}
