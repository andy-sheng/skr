package com.module.playways.room.gift.event;

public class UpdateDiamondEvent {

    /**
     * zuanBalance : 14586340
     */

    private float zuanBalance;

    public UpdateDiamondEvent(float zuanBalance) {
        this.zuanBalance = zuanBalance;
    }

    public float getZuanBalance() {
        return zuanBalance;
    }
}
