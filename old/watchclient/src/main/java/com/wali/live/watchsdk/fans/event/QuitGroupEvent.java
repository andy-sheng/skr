package com.wali.live.watchsdk.fans.event;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by lan on 2017/11/23.
 */
public class QuitGroupEvent {
    public long zuid;

    private QuitGroupEvent(long zuid) {
        this.zuid = zuid;
    }

    public static void post(long zuid) {
        EventBus.getDefault().post(new QuitGroupEvent(zuid));
    }
}
