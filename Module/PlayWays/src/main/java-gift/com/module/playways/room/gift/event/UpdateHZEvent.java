package com.module.playways.room.gift.event;

public class UpdateHZEvent {
    float hz;
    private long ts;

    public UpdateHZEvent(float hz, long ts) {
        this.hz = hz;
        this.ts = ts;
    }

    public float getHz() {
        return hz;
    }

    public long getTs() {
        return ts;
    }
}
