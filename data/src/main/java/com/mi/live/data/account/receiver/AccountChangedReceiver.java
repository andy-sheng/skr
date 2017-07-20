package com.mi.live.data.account.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.account.channel.SpecialChannel;

public class AccountChangedReceiver extends BroadcastReceiver {
    private final static String TAG = AccountChangedReceiver.class.getSimpleName();

    public static final String LOGIN_ACCOUNTS_PRE_CHANGED_ACTION = "android.accounts.LOGIN_ACCOUNTS_PRE_CHANGED";
    public static final String LOGIN_ACCOUNTS_POST_CHANGED_ACTION = "android.accounts.LOGIN_ACCOUNTS_POST_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        MyLog.d(TAG, "intent.getAction()=" + action);
        if (LOGIN_ACCOUNTS_POST_CHANGED_ACTION.equals(action)) {
            for (int channelId : SpecialChannel.sChannelSet) {
                UserAccountManager.getInstance().logoff(channelId);
            }
        }
    }
}