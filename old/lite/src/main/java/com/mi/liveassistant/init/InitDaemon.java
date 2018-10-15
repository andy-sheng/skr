package com.mi.liveassistant.init;

import com.mi.liveassistant.data.config.GetConfigManager;
import com.mi.liveassistant.milink.event.MiLinkEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by yangli on 17-5-3.
 *
 * @module 初始化辅助类
 */
public enum InitDaemon {

    INSTANCE;

    private static final String TAG = "InitDaemon";

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(MiLinkEvent.StatusConnected event) {
        GetConfigManager.getInstance();
    }
}
