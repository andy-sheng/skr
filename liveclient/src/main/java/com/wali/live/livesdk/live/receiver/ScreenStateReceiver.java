package com.wali.live.livesdk.live.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.base.log.MyLog;
import com.wali.live.livesdk.live.eventbus.LiveEventClass;

import org.greenrobot.eventbus.EventBus;

public class ScreenStateReceiver extends BroadcastReceiver {
    private static final String TAG = ScreenStateReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        MyLog.d(TAG, "action=" + action);
        switch (action) {
            case Intent.ACTION_SCREEN_OFF:
                EventBus.getDefault().post(new LiveEventClass.ScreenStateEvent(LiveEventClass.ScreenStateEvent.ACTION_SCREEN_OFF));
                break;
            case Intent.ACTION_SCREEN_ON:
                EventBus.getDefault().post(new LiveEventClass.ScreenStateEvent(LiveEventClass.ScreenStateEvent.ACTION_SCREEN_ON));
                break;
            case Intent.ACTION_USER_PRESENT:
                EventBus.getDefault().post(new LiveEventClass.ScreenStateEvent(LiveEventClass.ScreenStateEvent.ACTION_USER_PRESENT));
                break;
        }
    }
}
