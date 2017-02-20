package com.wali.live.livesdk.live.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;

import com.base.log.MyLog;
import com.wali.live.livesdk.live.eventbus.LiveEventClass;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by chengsimin on 16/4/12.
 */
public class TelephoneStateReceiver extends BroadcastReceiver {
    public static final String TAG = "TelephoneStateReceiver";

    public TelephoneStateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        if (context == null) {
            return;
        }

        String action = intent.getAction();
        MyLog.v(TAG + " onReceive action : " + action);
        if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager == null) {
                MyLog.e(TAG, "telephonyManager == null");
                return;
            }

            int state = telephonyManager.getCallState();
            MyLog.v(TAG, "state == " + state);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    EventBus.getDefault().post(new LiveEventClass.SystemEvent(LiveEventClass.SystemEvent.EVENT_TYPE_ACTION_NEW_OUTGOING_CALL, null, null));
                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                    EventBus.getDefault().post(new LiveEventClass.SystemEvent(LiveEventClass.SystemEvent.EVENT_TYPE_ACTION_NEW_OUTGOING_CALL, null, null));
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    EventBus.getDefault().post(new LiveEventClass.SystemEvent(LiveEventClass.SystemEvent.EVENT_TYPE_ACTION_NEW_OUTGOING_CALL, null, null));
                    break;
            }
        } else if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {     //来电电话
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager == null) {
                MyLog.e(TAG, "telephonyManager == null");
                return;
            }

            int state = telephonyManager.getCallState();
            MyLog.v(TAG, "state == " + state);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    EventBus.getDefault().post(new LiveEventClass.SystemEvent(LiveEventClass.SystemEvent.EVENT_TYPE_ACTION_PHONE_STATE_CHANGED_RING, null, null));
                    break;

                case TelephonyManager.CALL_STATE_IDLE:
                    EventBus.getDefault().post(new LiveEventClass.SystemEvent(LiveEventClass.SystemEvent.EVENT_TYPE_ACTION_PHONE_STATE_CHANGED_IDLE, null, null));
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    EventBus.getDefault().post(new LiveEventClass.SystemEvent(LiveEventClass.SystemEvent.EVENT_TYPE_ACTION_NEW_OUTGOING_CALL, null, null));
                    break;
            }

        }
    }

    public static TelephoneStateReceiver registerReceiver(Activity activity) {
        TelephoneStateReceiver mTelephoneStateReceiver = new TelephoneStateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        intentFilter.setPriority(Integer.MAX_VALUE);
        activity.registerReceiver(mTelephoneStateReceiver, intentFilter);
        return mTelephoneStateReceiver;
    }

    public static void unregisterReceiver(Activity activity, TelephoneStateReceiver receiver) {
        if (receiver != null) {
            activity.unregisterReceiver(receiver);
        }
    }
}