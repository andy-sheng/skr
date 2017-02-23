package com.wali.live.watchsdk.ipc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wali.live.sdk.manager.MiLiveSdkController;
import com.wali.live.sdk.manager.global.GlobalData;
import com.wali.live.sdk.manager.log.Logger;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkEvent;

/**
 * Created by chengsimin on 2016/12/27.
 */
public class MiLiveSdkReceiver extends BroadcastReceiver {
    public final static String TAG = MiLiveSdkReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            String packageName = intent.getPackage();
            int channelId = intent.getIntExtra(ReceiverConstant.EXTRA_CHANNEL_ID, 0);
            Logger.d(TAG, "action:" + action + ";package=" + packageName + ";channelId=" + channelId);

            // 保证是自己app和action发出的闹钟
            if (action.equals(ReceiverConstant.ACTION_WANT_LOGIN) &&
                    packageName.equals(GlobalData.app().getPackageName()) &&
                    channelId == MiLiveSdkController.getInstance().getChannelId()) {
                Logger.d(TAG, "want login");
                MiLiveSdkEvent.postWantLogin();
            }
        }
    }
}
