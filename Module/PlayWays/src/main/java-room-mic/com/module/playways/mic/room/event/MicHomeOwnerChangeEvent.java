package com.module.playways.mic.room.event;

public class MicHomeOwnerChangeEvent {
    int ownerId;

    public int getOwnerId() {
        return ownerId;
    }

    public MicHomeOwnerChangeEvent(int ownerId) {
        this.ownerId = ownerId;
    }
}
