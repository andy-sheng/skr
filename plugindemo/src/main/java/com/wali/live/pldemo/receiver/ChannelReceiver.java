package com.wali.live.pldemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.common.utils.U;

public class ChannelReceiver extends BroadcastReceiver {
    public static final String ACTION = "com.replugin.datachange";

    public static final String DATA_KEY = "data_key";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ACTION)){
            // 数据通道 receiver
            U.getToastUtil().showToast(intent.getStringExtra(DATA_KEY));
        }
    }
}
