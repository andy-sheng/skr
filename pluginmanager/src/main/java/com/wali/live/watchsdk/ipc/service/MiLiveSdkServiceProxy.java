package com.wali.live.watchsdk.ipc.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.wali.live.sdk.manager.MiLiveSdkController;
import com.wali.live.sdk.manager.global.GlobalData;
import com.wali.live.sdk.manager.version.VersionCheckManager;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by chengsimin on 2016/12/27.
 */

public class MiLiveSdkServiceProxy implements ServiceConnection {
    public final static String TAG = MiLiveSdkServiceProxy.class.getSimpleName();

    Intent mIntent;

    IMiLiveSdkService remoteService;

    IMiLiveSdkEventCallback miLiveSdkEventCallback = new IMiLiveSdkEventCallback.Stub() {
        @Override
        public void onEventLogin(int code) throws RemoteException {
            Log.d(TAG, "onEventLoginResult:" + code);
            // 抛出eventbus事件通知
            EventBus.getDefault().post(new MiLiveSdkEvent.LoginResult(code));
        }

        @Override
        public void onEventLogoff(int code) throws RemoteException {
            Log.d(TAG, "onEventLogoff:" + code);
            // 登出了
            EventBus.getDefault().post(new MiLiveSdkEvent.LogoffResult(code));
        }

        @Override
        public void onEventWantLogin() throws RemoteException {
            Log.d(TAG, "onEventWantLogin");
            // 请求登录
            EventBus.getDefault().post(new MiLiveSdkEvent.WantLogin());
        }

    };
    long mMiId;

    String mSsoToken;

    String mAuthCode;

    boolean mClearAccountFlag = false;

    private static MiLiveSdkServiceProxy sIntance;

    public static synchronized MiLiveSdkServiceProxy getInstance() {
        if (sIntance == null) {
            sIntance = new MiLiveSdkServiceProxy();
        }
        return sIntance;
    }

    private MiLiveSdkServiceProxy() {
        mIntent = new Intent();
        mIntent.setPackage(VersionCheckManager.PACKAGE_NAME);
        mIntent.setClassName(VersionCheckManager.PACKAGE_NAME, "com.wali.live.watchsdk.ipc.service.MiLiveSdkService");
    }

    void bindService() {
        Log.w(TAG, "bindService " + mIntent);
        GlobalData.app().bindService(mIntent, this, Context.BIND_AUTO_CREATE);
    }

    void stopService() {
        GlobalData.app().stopService(mIntent);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        Log.w(TAG, "onServiceConnected");
        remoteService = IMiLiveSdkService.Stub.asInterface(service);
        try {
            remoteService.setEventCallBack(MiLiveSdkController.getChannelId(), miLiveSdkEventCallback);
            // 尝试处理登录
            if (!TextUtils.isEmpty(mSsoToken)) {
                remoteService.loginByMiAccountSso(MiLiveSdkController.getChannelId(), mMiId, mSsoToken);
                mSsoToken = "";
            } else if (!TextUtils.isEmpty(mAuthCode)) {
                remoteService.loginByMiAccountOAuth(MiLiveSdkController.getChannelId(), mAuthCode);
                mAuthCode = "";
            } else if (mClearAccountFlag) {
                remoteService.clearAccount(MiLiveSdkController.getChannelId());
                mClearAccountFlag = false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.w(TAG, "onServiceDisconnected");
    }

    public void tryInit() {
        if (remoteService == null) {
            bindService();
        } else {
            //TODO 可以写个 aidl test 方法，当这个方法跑异常时也重新绑定一下
        }
    }

    public void openLive(RoomInfo roomInfo){
        if (remoteService == null) {
            bindService();
        } else {
            try {
                remoteService.openLive(roomInfo.getPlayerId(), roomInfo.getLiveId(), roomInfo.getVideoUrl());
            } catch (RemoteException e) {
                bindService();
            }
        }
    }

    public void loginByMiAccount(String code) {
        Log.w(TAG, "loginByMiAccount code:" + code);
        if (remoteService == null) {
            mAuthCode = code;
            bindService();
        } else {
            try {
                remoteService.loginByMiAccountOAuth(MiLiveSdkController.getChannelId(), code);
            } catch (RemoteException e) {
                mAuthCode = code;
                bindService();
            }
        }
    }

    public void loginByMiAccountSso(long miid, String ssoToken) {
        Log.w(TAG, "loginByMiAccountSso miid:" + miid + ",authCode:" + ssoToken);
        if (remoteService == null) {
            mMiId = miid;
            mSsoToken = ssoToken;
            bindService();
        } else {
            try {
                remoteService.loginByMiAccountSso(MiLiveSdkController.getChannelId(), miid, ssoToken);
            } catch (RemoteException e) {
                mMiId = miid;
                mSsoToken = ssoToken;
                bindService();
            }
        }
    }

    public void clearAccount() {
        Log.w(TAG, "clearAccount");
        if (remoteService == null) {
            mClearAccountFlag = true;
            bindService();
        } else {
            try {
                remoteService.clearAccount(MiLiveSdkController.getChannelId());
            } catch (RemoteException e) {
                mClearAccountFlag = true;
                bindService();
            }
        }
    }
}
