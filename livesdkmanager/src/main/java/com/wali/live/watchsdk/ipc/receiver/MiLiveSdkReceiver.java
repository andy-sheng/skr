package com.wali.live.watchsdk.ipc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;

import com.wali.live.watchsdk.ipc.service.MiLiveSdkEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by chengsimin on 2016/12/27.
 */

public class MiLiveSdkReceiver extends BroadcastReceiver {

    public final static String TAG = MiLiveSdkReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            String packageName = intent.getPackage();
            Log.w(TAG, "action:" + action);
            // 保证是自己app和action发出的闹钟。
            if (action.equals(ReceiverConstant.ACTION_WANT_LOGIN)) {
                EventBus.getDefault().post(new MiLiveSdkEvent.WantLogin());
            }
        }
    }

}
