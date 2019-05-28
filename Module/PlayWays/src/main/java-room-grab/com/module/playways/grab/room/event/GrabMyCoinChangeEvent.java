package com.module.playways.grab.room.event;

public class GrabMyCoinChangeEvent {
    public int coin;
    public int coinChange;

    public GrabMyCoinChangeEvent(int coin, int coinChange) {
        this.coin = coin;
        this.coinChange = coinChange;
    }
}
