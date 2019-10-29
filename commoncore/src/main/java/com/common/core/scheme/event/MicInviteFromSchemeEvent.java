package com.common.core.scheme.event;

public class MicInviteFromSchemeEvent {
    public int ownerId;
    public int roomId;
    public int ask;

    public MicInviteFromSchemeEvent(int ownerId, int roomId, int ask) {
        this.ownerId = ownerId;
        this.roomId = roomId;
        this.ask = ask;
    }
}
