package com.module.playways.room.gift.event;

public class UpdateMeiliEvent {
    public int userID;
    public int value;
    public long ts;

    public UpdateMeiliEvent(int userId,int coinBalance, long ts) {
        this.userID = userId;
        this.value = coinBalance;
        this.ts = ts;
    }

    @Override
    public String toString() {
        return "UpdateMeiliEvent{" +
                "userID=" + userID +
                ", value=" + value +
                ", ts=" + ts +
                '}';
    }
}
