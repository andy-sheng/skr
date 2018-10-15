package com.wali.live.livesdk.live.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.base.event.SdkEventClass;

/**
 * Created by chenyong on 2017/3/30.
 */

public class GameLiveNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SdkEventClass.postBringFront();
    }
}
