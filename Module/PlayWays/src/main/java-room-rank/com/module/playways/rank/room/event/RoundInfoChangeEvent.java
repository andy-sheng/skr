package com.module.playways.rank.room.event;

public class RoundInfoChangeEvent {
    public boolean myturn;

    public RoundInfoChangeEvent(boolean myturn) {
        this.myturn = myturn;
    }
}
