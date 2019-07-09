package com.common.core.userinfo.event;

import com.common.core.userinfo.model.UserInfoModel;

// 关系改变的event 都是我主动发起的事件
public class RelationChangeEvent {

    public static final int FOLLOW_TYPE = 1;
    public static final int UNFOLLOW_TYPE = 2;

    public boolean isFriend;
    public boolean isFollow;
    public int useId;
    public int type;

    public RelationChangeEvent(int type, int userId, boolean isFriend, boolean isFollow) {
        this.type = type;
        this.useId = userId;
        this.isFriend = isFriend;
        this.isFollow = isFollow;
    }

}
