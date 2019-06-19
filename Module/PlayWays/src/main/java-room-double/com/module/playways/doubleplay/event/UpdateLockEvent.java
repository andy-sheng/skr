package com.module.playways.doubleplay.event;

public class UpdateLockEvent {
    int userID;
    boolean lock;

    public int getUserID() {
        return userID;
    }

    public boolean isLock() {
        return lock;
    }

    public UpdateLockEvent(int userID, boolean lock) {
        this.userID = userID;
        this.lock = lock;
    }
}
