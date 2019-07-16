package com.module.msg.custom;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.log.MyLog;

import io.rong.push.PushType;
import io.rong.push.notification.PushMessageReceiver;
import io.rong.push.notification.PushNotificationMessage;

public class MyRcPushMessageReceiver extends PushMessageReceiver {
    public final String TAG = "MyRcPushMessageReceiver";

    @Override
    public boolean onNotificationMessageArrived(Context context, PushType pushType, PushNotificationMessage pushNotificationMessage) {
        // false 代表不自己处理
        return false;
    }

    @Override
    public boolean onNotificationMessageClicked(Context context, PushType pushType, PushNotificationMessage pushNotificationMessage) {
        // false 代表不自己处理
        MyLog.d(TAG,"onNotificationMessageClicked" + " context=" + context + " pushType=" + pushType + " pushNotificationMessage=" + pushNotificationMessage);
        if (pushNotificationMessage != null) {
            String objectName = pushNotificationMessage.getObjectName();
            if ("SKR:NotificationMsg".equals(objectName)) {
                // 通知类的消息
                String pushData = pushNotificationMessage.getPushData();
                JSONObject jsonObject = JSON.parseObject(pushData);
                MyLog.d(TAG,"onNotificationMessageClicked" + " jsonObject=" + jsonObject);
                if (jsonObject != null) {
                    String url = jsonObject.getString("url");
                    if (!TextUtils.isEmpty(url)) {
                        // 有schema 跳转到自己处理
                        ARouter.getInstance().build("/core/SchemeSdkActivity")
                                .withString("uri", url)
                                .navigation();
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
