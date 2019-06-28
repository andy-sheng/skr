package com.module.playways.room.gift.event;

import org.greenrobot.eventbus.EventBus;

public class UpdateHZEvent {
    float hz;
    static long lastTs = 0;

    public UpdateHZEvent(float hz) {
        this.hz = hz;
    }

    public float getHz() {
        return hz;
    }

    public static void sendEvent(float hz, long newTs) {
        if (newTs > lastTs) {
            lastTs = newTs;
            EventBus.getDefault().post(new UpdateHZEvent(hz));
        }
    }
}
