package com.wali.live.modulewatch.base.event;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by chengsimin on 2016/12/21.
 */
public class WatchOrReplayActivityCreated {
    private WatchOrReplayActivityCreated() {
    }

    public static void post() {
        EventBus.getDefault().post(new WatchOrReplayActivityCreated());
    }
}
