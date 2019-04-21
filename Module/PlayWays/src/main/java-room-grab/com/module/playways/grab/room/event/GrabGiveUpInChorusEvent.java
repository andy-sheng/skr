package com.module.playways.grab.room.event;

public class GrabGiveUpInChorusEvent {
    public int uid;

    public GrabGiveUpInChorusEvent(int uid) {
        this.uid = uid;
    }

    @Override
    public String toString() {
        return "GrabGiveUpInChorusEvent{" +
                "uid=" + uid +
                '}';
    }
}
