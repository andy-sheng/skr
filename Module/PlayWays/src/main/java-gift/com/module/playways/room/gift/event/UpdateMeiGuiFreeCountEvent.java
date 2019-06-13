package com.module.playways.room.gift.event;

import org.greenrobot.eventbus.EventBus;

public class UpdateMeiGuiFreeCountEvent {
    int count;

    static long lastTs = 0;

    private UpdateMeiGuiFreeCountEvent(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public static void sendEvent(int count, long newTs) {
        if (newTs > lastTs) {
            lastTs = newTs;
            EventBus.getDefault().post(new UpdateMeiGuiFreeCountEvent(count));
        }
    }
}
