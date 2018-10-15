package com.wali.live.watchsdk.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.base.event.SdkEventClass;
import com.base.log.MyLog;

public class ScreenStateReceiver extends BroadcastReceiver {
    private static final String TAG = ScreenStateReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        MyLog.d(TAG, "action=" + action);
        switch (action) {
            case Intent.ACTION_SCREEN_OFF:
                SdkEventClass.postScreenState(SdkEventClass.ScreenStateEvent.ACTION_SCREEN_OFF);
                break;
            case Intent.ACTION_SCREEN_ON:
                SdkEventClass.postScreenState(SdkEventClass.ScreenStateEvent.ACTION_SCREEN_ON);
                break;
            case Intent.ACTION_USER_PRESENT:
                SdkEventClass.postScreenState(SdkEventClass.ScreenStateEvent.ACTION_USER_PRESENT);
                break;
        }
    }

    public static ScreenStateReceiver registerReceiver(Activity activity) {
        ScreenStateReceiver receiver = new ScreenStateReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);

        activity.registerReceiver(receiver, intentFilter);
        return receiver;
    }

    public static void unregisterReceiver(Activity activity, ScreenStateReceiver receiver) {
        if (activity != null && receiver != null) {
            activity.unregisterReceiver(receiver);
        }
    }
}
