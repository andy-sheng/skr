package com.mi.live.data.event;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by chengsimin on 16/10/17.
 */
public class DatabaseChangedEvent {
    public static final int TYPE_DB_RELATION = 1;

    public static final int ACTION_ADD = 1;
    public static final int ACTION_UPDATE = 2;
    public static final int ACTION_REMOVE = 3;

    public int type;
    public int action;
    public Object data;

    private DatabaseChangedEvent(int type, int action, Object data) {
        this.type = type;
        this.action = action;
        this.data = data;
    }

    public static void post(int type, int action, Object data) {
        EventBus.getDefault().post(new DatabaseChangedEvent(type, action, data));
    }
}