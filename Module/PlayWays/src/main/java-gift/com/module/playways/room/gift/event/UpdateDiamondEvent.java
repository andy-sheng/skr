package com.module.playways.room.gift.event;

import org.greenrobot.eventbus.EventBus;

public class UpdateDiamondEvent {
    static long lastTs = 0;
    /**
     * zuanBalance : 14586340
     */

    private float zuanBalance;

    private UpdateDiamondEvent(float zuanBalance) {
        this.zuanBalance = zuanBalance;
    }

    public float getZuanBalance() {
        return zuanBalance;
    }

    public static void sendEvent(float zuanBalance, long newTs) {
        if (newTs > lastTs) {
            lastTs = newTs;
            EventBus.getDefault().post(new UpdateDiamondEvent(zuanBalance));
        }
    }
}
