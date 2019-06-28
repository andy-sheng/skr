package com.module.playways.room.gift.event;

import org.greenrobot.eventbus.EventBus;

public class UpdateCoinEvent {
    private int coinBalance;
    static long lastTs = 0;

    public UpdateCoinEvent(int coinBalance) {
        this.coinBalance = coinBalance;
    }

    public int getCoinBalance() {
        return coinBalance;
    }

    public static void sendEvent(int coin, long newTs) {
        if (newTs > lastTs) {
            lastTs = newTs;
            EventBus.getDefault().post(new UpdateCoinEvent(coin));
        }
    }
}
