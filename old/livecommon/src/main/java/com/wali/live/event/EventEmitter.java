package com.wali.live.event;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by lan on 2017/7/27.
 */
public class EventEmitter {
    public static class EnterRoomList {
        private EnterRoomList() {
        }
    }

    public static void postEnterRoomList() {
        EventBus.getDefault().post(new EnterRoomList());
    }
}
