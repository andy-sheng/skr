package com.common.core.userinfo.event;

import com.common.core.userinfo.UserInfo;

public class UserInfoDBChangeEvent {

    public static final int EVENT_INIT = 1; //初始化
    public static final int EVENT_DB_INSERT = 2; // 插入
    public static final int EVENT_DB_UPDATE = 3; // 更新数据
    public static final int EVENT_DB_REMOVE = 4; // 删除数据

    public int type;
    public UserInfo mUserInfo;

    public UserInfoDBChangeEvent(int type, UserInfo userInfo) {
        this.type = type;
        this.mUserInfo = userInfo;
    }
}
