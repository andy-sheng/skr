package com.wali.live.watchsdk.login;

import android.app.IntentService;
import android.content.Intent;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkBinder;
import com.wali.live.watchsdk.scheme.SchemeSdkActivity;

/**
 * Created by lan on 2017/7/7.
 *
 * @description 因为小米视频不接我们jar，所以这里单独起个服务用来给小米视频作登录
 */
public class LoginService extends IntentService {
    private static final String TAG = "LoginService";

    public LoginService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        int channelId = intent.getIntExtra(SchemeSdkActivity.EXTRA_CHANNEL_ID, 0);
        String packageName = intent.getStringExtra(SchemeSdkActivity.EXTRA_PACKAGE_NAME);
        String channelSecret = intent.getStringExtra(SchemeSdkActivity.EXTRA_CHANNEL_SECRET);
        if (channelSecret == null) {
            channelSecret = "";
        }

        long miid = intent.getLongExtra(SchemeSdkActivity.EXTRA_MI_ID, 0);
        String serviceToken = intent.getStringExtra(SchemeSdkActivity.EXTRA_SERVICE_TOKEN);

        MyLog.d(TAG, channelId + ":" + packageName + ":" + miid);
        try {
            MiLiveSdkBinder.getInstance().loginByMiAccountSso(channelId, packageName, channelSecret, miid, serviceToken);
        } catch (RemoteException e) {
            MyLog.e(TAG, e);
        }
    }
}
