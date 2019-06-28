package com.module.playways.doubleplay.event;

public class UpdateNextSongDecEvent {
    String dec;
    boolean hasNext;

    public String getDec() {
        return dec;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public UpdateNextSongDecEvent(String dec, boolean hasNext) {
        this.dec = dec;
        this.hasNext = hasNext;
    }
}
