package com.mi.live.data.milink.callback;

import com.base.activity.BaseSdkActivity;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.milink.event.MiLinkEvent;
import com.mi.live.data.milink.event.MiLinkEvent.Account;
import com.base.log.MyLog;
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
        EventBus.getDefault().post(new Account(Account.KICK, type, null));
    }

    @Override
    public void onEventGetServiceToken() {
        MyLog.w(TAG, "onEventGetServiceToken");
        EventBus.getDefault().post(new Account(Account.GET_SERVICE_TOKEN, null, null));
    }

    @Override
    public void onEventServiceTokenExpired() {
        MyLog.w(TAG, "onEventServiceTokenExpired  service token expired, passToken to get serviceToken");
        EventBus.getDefault().post(new Account(Account.SERVICE_TOKEN_EXPIRED, null, null));
    }

    @Override
    public void onEventShouldCheckUpdate() {
        MyLog.w(TAG, "onEventShouldCheckUpdate");
    }

    @Override
    public void onEventInvalidPacket() {
        MyLog.w(TAG, "onEventInvalidPacket invalid packet");
    }

    @Override
    public void onEventPermissionDenied() {
        MyLog.d(TAG,"onEventPermissionDenied" );
        // 移动网络开启，但是禁止app联网时会触发此提示，保证提示在app在前台时才弹出
        EventBus.getDefault().post(new BaseSdkActivity.PermissionDenied());
    }
}
