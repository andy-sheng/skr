package com.module.playways.room.gift.event;

public class UpdateCoinAndDiamondEvent {

    /**
     * coinBalance : 207
     * zuanBalance : 14586340
     */

    private int coinBalance;
    private float zuanBalance;

    public UpdateCoinAndDiamondEvent(int coinBalance, float zuanBalance) {
        this.coinBalance = coinBalance;
        this.zuanBalance = zuanBalance;
    }

    public int getCoinBalance() {
        return coinBalance;
    }

    public float getZuanBalance() {
        return zuanBalance;
    }
}
