package com.wali.live.event;

public class AtEvent {
    public final String name;
    public final long targetUserId;

    private AtEvent(String name, long targetUserId) {
        this.name = name;
        this.targetUserId = targetUserId;
    }

    public static AtEvent newInstance(String name, long targetUserId) {
        return new AtEvent(name, targetUserId);
    }
}