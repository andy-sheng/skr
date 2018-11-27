package com.engine;

public class EngineEvent {
    public static final int TYPE_USER_JOIN = 1;
    public static final int TYPE_USER_LEAVE = 1;
    int type;
    int userId;
    Object obj;

    public EngineEvent(int type, int userId) {
        this.type = type;
        this.userId = userId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
