package com.common.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.common.log.MyLog;

/**
 * 静态广播 工具类广播 可以实现一些工具类信息的监听
 * 监听网络变化
 */
public class CommonReceiver extends BroadcastReceiver {
    public final static String TAG = "CommonReceiver";

    public static void register() {
        CommonReceiver commonReceiver = new CommonReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        U.app().registerReceiver(commonReceiver, intentFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        MyLog.w(TAG, "onReceive" + " context=" + context + " action=" + action + " intent=" + intent);
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
            // 网络变化
            MyLog.w("NetworkReceiver", "network changed, NetworkReceiver action=" + intent.getAction());
            U.getNetworkUtils().notifyNetworkChange();
        }
    }


}
