package com.module.playways.room.gift.event;

public class UpdateCoinEvent {
    private int coinBalance;
    private long ts;

    public UpdateCoinEvent(int coinBalance, long ts) {
        this.ts = ts;
        this.coinBalance = coinBalance;
    }

    public UpdateCoinEvent(int coinBalance) {
        this.coinBalance = coinBalance;
    }

    public int getCoinBalance() {
        return coinBalance;
    }

    public long getTs() {
        return ts;
    }
}
