package com.common.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.common.log.MyLog;

/**
 * 静态广播
 * 监听网络变化
 */
public class NetworkChangeReceiver extends BroadcastReceiver {
    public final static String TAG = "NetworkChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        MyLog.w("NetworkReceiver", "network changed, NetworkReceiver action=" + intent.getAction());
        U.getNetworkUtils().notifyNetworkChange();
    }



}
