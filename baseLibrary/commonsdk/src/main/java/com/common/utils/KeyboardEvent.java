package com.common.utils;


/**
 * Created by chengsimin on 16/9/6.
 */
public class KeyboardEvent {
    public static final int EVENT_TYPE_KEYBOARD_VISIBLE = 0;
    public static final int EVENT_TYPE_KEYBOARD_HIDDEN = 1;
    public int eventType;
    public int keybordHeight;
    public String from;


    public KeyboardEvent(String from, int type, int keybordHeight) {
        this.from = from;
        this.eventType = type;
        this.keybordHeight = keybordHeight;
    }

    @Override
    public String toString() {
        return "KeyboardEvent{" +
                "eventType=" + eventType +
                ", keybordHeight=" + keybordHeight +
                ", from='" + from + '\'' +
                '}';
    }
}
