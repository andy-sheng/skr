package com.common.core.userinfo.event;

import com.common.core.userinfo.UserInfoModel;

// 关系改变的event
public class RelationChangeEvent {

    public static final int FOLLOW_TYPE = 1;
    public static final int UNFOLLOW_TYPE = 2;

    public boolean isFriend;
    public UserInfoModel userInfoModel;
    public int type;

    public RelationChangeEvent(int type, UserInfoModel userInfoModel, boolean isFriend) {
        this.type = type;
        this.userInfoModel = userInfoModel;
        this.isFriend = isFriend;
    }

}
