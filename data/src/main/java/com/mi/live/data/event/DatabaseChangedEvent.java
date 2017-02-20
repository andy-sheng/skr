package com.mi.live.data.event;

/**
 * Created by chengsimin on 16/10/17.
 */
public  class DatabaseChangedEvent {
    public int eventType;
    public int actionType;
    public Object object;

    public static final int EVENT_TYPE_DB_SONG = 1;
    public static final int EVENT_TYPE_DB_RELATION = 2;

    public static final int ACTION_ADD = 1;
    public static final int ACTION_UPDATE = 2;
    public static final int ACTION_REMOVE = 3;

    public DatabaseChangedEvent(int type, int actionType, Object object) {
        this.eventType = type;
        this.actionType = actionType;
        this.object = object;
    }
}