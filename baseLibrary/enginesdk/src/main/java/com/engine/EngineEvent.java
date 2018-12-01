package com.engine;

public class EngineEvent {
    public static final int TYPE_USER_JOIN = 1;
    public static final int TYPE_USER_LEAVE = 2;
    public static final int TYPE_FIRST_VIDEO_DECODED = 3;
    public static final int TYPE_USER_MUTE_AUDIO = 4;
    public static final int TYPE_USER_MUTE_VIDEO = 5;
    public static final int TYPE_USER_REJOIN = 6;
    public static final int TYPE_USER_ROLE_CHANGE = 7;
    public static final int TYPE_USER_VIDEO_ENABLE = 8;

    public static final int TYPE_ENGINE_DESTROY= 9;

    int type;
    UserStatus userStatus;
    Object obj;

    public EngineEvent(int type, UserStatus userStatus) {
        this.type = type;
        this.userStatus = userStatus;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public UserStatus getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(UserStatus userStatus) {
        this.userStatus = userStatus;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
