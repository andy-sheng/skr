package com.common.jiguang;

import android.content.Context;

import com.common.log.MyLog;

import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.service.JPushMessageReceiver;

public class JGOpReceiver extends JPushMessageReceiver {
    public final static String TAG = "OpReceiver";
    @Override
    public void onTagOperatorResult(Context context, JPushMessage jPushMessage) {
        MyLog.d(TAG,"onTagOperatorResult" + " context=" + context + " jPushMessage=" + jPushMessage);
        super.onTagOperatorResult(context, jPushMessage);
        if(jPushMessage.getSequence()==4){
            JiGuangPush.joinSkrRoomId2();
        }
    }

    @Override
    public void onCheckTagOperatorResult(Context context, JPushMessage jPushMessage) {
        MyLog.d(TAG,"onCheckTagOperatorResult" + " context=" + context + " jPushMessage=" + jPushMessage);
        super.onCheckTagOperatorResult(context, jPushMessage);
    }

    @Override
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        MyLog.d(TAG,"onAliasOperatorResult" + " context=" + context + " jPushMessage=" + jPushMessage);
        super.onAliasOperatorResult(context, jPushMessage);
    }

    @Override
    public void onMobileNumberOperatorResult(Context context, JPushMessage jPushMessage) {
        MyLog.d(TAG,"onMobileNumberOperatorResult" + " context=" + context + " jPushMessage=" + jPushMessage);
        super.onMobileNumberOperatorResult(context, jPushMessage);
    }
}
