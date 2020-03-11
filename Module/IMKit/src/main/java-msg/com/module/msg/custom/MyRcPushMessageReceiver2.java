package com.module.msg.custom;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.log.MyLog;
import com.common.utils.U;

import io.rong.push.PushErrorCode;
import io.rong.push.PushType;
import io.rong.push.RongPushClient;
import io.rong.push.common.RLog;
import io.rong.push.notification.PushMessageReceiver;
import io.rong.push.notification.PushNotificationMessage;
import io.rong.push.notification.RongNotificationInterface;

public class MyRcPushMessageReceiver2 extends BroadcastReceiver {
    public final String TAG = "MyRcPushMessageReceiver2";


    public final void onReceive(Context context, Intent intent) {
        RLog.d("PushMessageReceiver", "onReceive.action:" + intent.getAction());
        if (intent.getAction() == null) {
            RLog.e("PushMessageReceiver", "the intent action is null, return directly. ");
        } else {
            String name = intent.getStringExtra("pushType");
            int left = intent.getIntExtra("left", 0);
            PushNotificationMessage message = (PushNotificationMessage)intent.getParcelableExtra("message");
            if (message == null) {
                RLog.e("PushMessageReceiver", "message is null. Return directly!");
            } else {
                PushType pushType = PushType.getType(name);
                if (intent.getAction().equals("io.rong.push.intent.MESSAGE_ARRIVED")) {
                    if (!this.onNotificationMessageArrived(context, pushType, message) && (pushType.equals(PushType.RONG) || pushType.equals(PushType.GOOGLE_FCM) || pushType.equals(PushType.GOOGLE_GCM)) && !this.handleVoIPNotification(context, message)) {
                        RongNotificationInterface.sendNotification(context, message, left);
                    }
                } else if (intent.getAction().equals("io.rong.push.intent.MESSAGE_CLICKED")) {
                    if (!TextUtils.isEmpty(message.getPushId())) {
                        RongPushClient.recordNotificationEvent(message);
                    }

                    if (!this.onNotificationMessageClicked(context, pushType, message) && !this.handleVoIPNotification(context, message)) {
                        boolean isMulti = intent.getBooleanExtra("isMulti", false);
                        this.handleNotificationClickEvent(context, isMulti, message);
                    }
                } else if (intent.getAction().equals("io.rong.push.intent.THIRD_PARTY_PUSH_STATE")) {
                    String action = intent.getStringExtra("action");
                    long resultCode = intent.getLongExtra("resultCode", (long) PushErrorCode.UNKNOWN.getCode());
                    this.onThirdPartyPushState(pushType, action, resultCode);
                } else {
                    RLog.e("PushMessageReceiver", "Unknown action, do nothing!");
                }

            }
        }
    }

    public void onThirdPartyPushState(PushType pushType, String action, long resultCode) {
        RLog.e("PushMessageReceiver", "onThirdPartyPushState pushType: " + pushType + " action: " + action + " resultCode: " + resultCode);
    }

    private void handleNotificationClickEvent(Context context, boolean isMulti, PushNotificationMessage notificationMessage) {
        MyLog.d(TAG,"handleNotificationClickEvent" + " context=" + context + " isMulti=" + isMulti + " notificationMessage=" + notificationMessage);
        String isFromPush = !notificationMessage.getSourceType().equals(PushNotificationMessage.PushSourceType.FROM_OFFLINE_MESSAGE) && !notificationMessage.getSourceType().equals(PushNotificationMessage.PushSourceType.FROM_ADMIN) ? "false" : "true";
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri.Builder builder = Uri.parse("rong://" + context.getPackageName()).buildUpon();
        if (notificationMessage.getSourceType().equals(PushNotificationMessage.PushSourceType.FROM_ADMIN)) {
            builder.appendPath("push_message").appendQueryParameter("targetId", notificationMessage.getTargetId()).appendQueryParameter("pushContent", notificationMessage.getPushContent()).appendQueryParameter("pushData", notificationMessage.getPushData()).appendQueryParameter("extra", notificationMessage.getExtra()).appendQueryParameter("isFromPush", isFromPush);
        } else if (isMulti) {
            builder.appendPath("conversationlist").appendQueryParameter("isFromPush", isFromPush);
        } else {
            builder.appendPath("conversation").appendPath(notificationMessage.getConversationType().getName()).appendQueryParameter("targetId", notificationMessage.getTargetId()).appendQueryParameter("title", TextUtils.isEmpty(notificationMessage.getPushTitle()) ? notificationMessage.getTargetUserName() : notificationMessage.getPushTitle()).appendQueryParameter("isFromPush", isFromPush);
        }

        intent.setData(builder.build());
        intent.setPackage(context.getPackageName());
        U.app().startActivity(intent);
        new Handler().postDelayed(() -> {
            if(U.getActivityUtils().getActivityList().size()==0){
                MyLog.d(TAG,"handleNotificationClickEvent 再次启动");
                U.app().startActivity(intent);
            }
        },3000);
    }

    public boolean handleVoIPNotification(Context context, PushNotificationMessage notificationMessage) {
        if (Build.VERSION.SDK_INT >= 29) {
            return false;
        } else {
            String objName = notificationMessage.getObjectName();
            if (objName != null && objName.equals("RC:VCInvite")) {
                RLog.d("PushMessageReceiver", "handle VoIP event.");
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.parse("rong://" + context.getPackageName()).buildUpon().appendPath("conversationlist").appendQueryParameter("isFromPush", "false").build();
                intent.setData(uri);
                intent.setPackage(context.getPackageName());
                context.startActivity(intent);
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean onNotificationMessageArrived(Context context, PushType pushType, PushNotificationMessage pushNotificationMessage) {
        // false 代表不自己处理
        return false;
    }

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
