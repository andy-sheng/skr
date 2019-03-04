package com.module.msg;

import android.content.Context;

import com.common.log.MyLog;

import io.rong.push.PushType;
import io.rong.push.notification.PushMessageReceiver;
import io.rong.push.notification.PushNotificationMessage;

public class RongNotifationReceiver extends PushMessageReceiver {

    public final static String TAG = "RongNotifationReceiver";

    @Override
    public boolean onNotificationMessageArrived(Context context, PushType pushType, PushNotificationMessage pushNotificationMessage) {
        MyLog.d(TAG, "onNotificationMessageArrived" + " context=" + context + " pushType=" + pushType + " pushNotificationMessage=" + pushNotificationMessage);
        return false;
    }

    @Override
    public boolean onNotificationMessageClicked(Context context, PushType pushType, PushNotificationMessage pushNotificationMessage) {
        MyLog.d(TAG, "onNotificationMessageClicked" + " context=" + context + " pushType=" + pushType + " pushNotificationMessage=" + pushNotificationMessage);
        return false;
    }
}
