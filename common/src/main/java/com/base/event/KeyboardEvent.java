package com.base.event;

/**
 * Created by chengsimin on 16/9/6.
 */
public class KeyboardEvent {
    public static final int EVENT_TYPE_KEYBOARD_VISIBLE = 0;
    public static final int EVENT_TYPE_KEYBOARD_HIDDEN = 1;
    public static final int EVENT_TYPE_KEYBOARD_VISIBLE_ALWAYS_SEND = 2;// 无论任何softmode都发送
    public int eventType;
    public Object obj1;


    public KeyboardEvent(int type) {
        this.eventType = type;
    }

    public KeyboardEvent(int type, Object obj1) {
        this.eventType = type;
        this.obj1 = obj1;
    }
}
