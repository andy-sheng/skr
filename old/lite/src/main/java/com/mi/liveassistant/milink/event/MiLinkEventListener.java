package com.mi.liveassistant.milink.event;

import com.mi.liveassistant.common.log.MyLog;
import com.mi.milink.sdk.client.IEventListener;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by chengsimin on 16/7/1.
 */
public class MiLinkEventListener implements IEventListener {
    private static final java.lang.String TAG = "MiLinkEventListener";

    @Override
    public void onEventKickedByServer(int type, long l, String s) {
        MyLog.w(TAG, "onEventKickedByServer type = " + type);
        EventBus.getDefault().post(new MiLinkEvent.Account(MiLinkEvent.Account.KICK, type, null));
    }

    @Override
    public void onEventGetServiceToken() {
        MyLog.w(TAG, "onEventGetServiceToken");
        EventBus.getDefault().post(new MiLinkEvent.Account(MiLinkEvent.Account.GET_SERVICE_TOKEN, null, null));
    }

    @Override
    public void onEventServiceTokenExpired() {
        MyLog.w(TAG, "onEventServiceTokenExpired  service token expired, passToken to get serviceToken");
        EventBus.getDefault().post(new MiLinkEvent.Account(MiLinkEvent.Account.SERVICE_TOKEN_EXPIRED, null, null));
    }

    @Override
    public void onEventShouldCheckUpdate() {
        MyLog.w(TAG, "onEventShouldCheckUpdate");
    }

    @Override
    public void onEventInvalidPacket() {
        MyLog.w(TAG, "onEventInvalidPacket invalid packet");
    }
}
