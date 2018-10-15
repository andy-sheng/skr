package com.wali.live.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;

import com.base.log.MyLog;
import com.wali.live.event.EventClass;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by lan on 16/4/12.
 */
public class PhoneStateReceiver extends BroadcastReceiver {
    public static final String TAG = PhoneStateReceiver.class.getSimpleName();

    public PhoneStateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || context == null) {
            return;
        }

        String action = intent.getAction();
        MyLog.v(TAG, "onReceive action : " + action);

        if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL) ||
                action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager == null) {
                MyLog.e(TAG, "telephonyManager == null");
                return;
            }

            int state = telephonyManager.getCallState();
            MyLog.v(TAG, "state == " + state);

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    EventBus.getDefault().post(new EventClass.PhoneStateEvent(EventClass.PhoneStateEvent.TYPE_PHONE_STATE_RING));
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    EventBus.getDefault().post(new EventClass.PhoneStateEvent(EventClass.PhoneStateEvent.TYPE_PHONE_STATE_OFFHOOK));
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    EventBus.getDefault().post(new EventClass.PhoneStateEvent(EventClass.PhoneStateEvent.TYPE_PHONE_STATE_IDLE));
                    break;
            }
        }
    }

    public static PhoneStateReceiver registerReceiver(Activity activity) {
        PhoneStateReceiver receiver = new PhoneStateReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        intentFilter.setPriority(Integer.MAX_VALUE);

        activity.registerReceiver(receiver, intentFilter);
        return receiver;
    }

    public static void unregisterReceiver(Activity activity, PhoneStateReceiver receiver) {
        if (activity != null && receiver != null) {
            activity.unregisterReceiver(receiver);
        }
    }
}