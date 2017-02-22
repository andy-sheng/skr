package com.wali.live.watchsdk.ipc.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.wali.live.sdk.manager.IMiLiveSdk;
import com.wali.live.sdk.manager.MiLiveSdkController;
import com.wali.live.sdk.manager.global.GlobalData;
import com.wali.live.sdk.manager.log.Logger;
import com.wali.live.sdk.manager.version.VersionCheckManager;

/**
 * Created by chengsimin on 2016/12/27.
 */
public class MiLiveSdkServiceProxy implements ServiceConnection {
    public final static String TAG = MiLiveSdkServiceProxy.class.getSimpleName();

    private Intent mIntent;
    private IMiLiveSdkService mRemoteService;

    private long mMiId;
    private String mServiceToken;
    private String mAuthCode;

    private boolean mClearAccountFlag = false;

    private IMiLiveSdk.ICallback mCallback;

    private IMiLiveSdkEventCallback mLiveSdkEventCallback = new IMiLiveSdkEventCallback.Stub() {
        @Override
        public void onEventLogin(int code) throws RemoteException {
            Logger.d(TAG, "onEventLoginResult:" + code);
            if (mCallback != null) {
                mCallback.notifyLogin(code);
            }
        }

        @Override
        public void onEventLogoff(int code) throws RemoteException {
            Logger.d(TAG, "onEventLogoff:" + code);
            if (mCallback != null) {
                mCallback.notifyLogoff(code);
            }
        }

        @Override
        public void onEventWantLogin() throws RemoteException {
            Logger.d(TAG, "onEventWantLogin");
            if (mCallback != null) {
                mCallback.notifyWantLogin();
            }
        }

        @Override
        public void onEventVerifyFailure(int code) throws RemoteException {
            Logger.d(TAG, "onEventVerifyFailure code=" + code);
            if (mCallback != null) {
                mCallback.notifyVerifyFailure(code);
            }
        }
    };

    private static MiLiveSdkServiceProxy sInstance;

    public static synchronized MiLiveSdkServiceProxy getInstance() {
        if (sInstance == null) {
            sInstance = new MiLiveSdkServiceProxy();
        }
        return sInstance;
    }

    private MiLiveSdkServiceProxy() {
        mIntent = new Intent();
        mIntent.setPackage(VersionCheckManager.PACKAGE_NAME);
        mIntent.setClassName(VersionCheckManager.PACKAGE_NAME, "com.wali.live.watchsdk.ipc.service.MiLiveSdkService");
    }

    public void setCallback(IMiLiveSdk.ICallback callback) {
        mCallback = callback;
    }

    private void bindService() {
        Logger.w(TAG, "bindService " + mIntent);
        GlobalData.app().bindService(mIntent, this, Context.BIND_AUTO_CREATE);
    }

    private void stopService() {
        GlobalData.app().stopService(mIntent);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        Logger.w(TAG, "onServiceConnected");
        mRemoteService = IMiLiveSdkService.Stub.asInterface(service);
        try {
            mRemoteService.setEventCallBack(
                    MiLiveSdkController.getInstance().getChannelId(),
                    mLiveSdkEventCallback);
            // 尝试处理登录
            if (!TextUtils.isEmpty(mServiceToken)) {
                mRemoteService.loginByMiAccountSso(
                        MiLiveSdkController.getInstance().getChannelId(),
                        GlobalData.app().getPackageName(),
                        MiLiveSdkController.getInstance().getChannelSecret(),
                        mMiId, mServiceToken);
                mServiceToken = "";
            } else if (!TextUtils.isEmpty(mAuthCode)) {
                mRemoteService.loginByMiAccountOAuth(
                        MiLiveSdkController.getInstance().getChannelId(),
                        GlobalData.app().getPackageName(),
                        MiLiveSdkController.getInstance().getChannelSecret(),
                        mAuthCode);
                mAuthCode = "";
            } else if (mClearAccountFlag) {
                mRemoteService.clearAccount(
                        MiLiveSdkController.getInstance().getChannelId(),
                        GlobalData.app().getPackageName(),
                        MiLiveSdkController.getInstance().getChannelSecret());
                mClearAccountFlag = false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Logger.w(TAG, "onServiceDisconnected");
    }

    public void tryInit() {
        if (mRemoteService == null) {
            bindService();
        }
    }

    public void loginByMiAccountOAuth(String authCode) {
        Logger.w(TAG, "loginByMiAccount authCode=" + authCode);
        if (mRemoteService == null) {
            mAuthCode = authCode;
            notifyServiceNull(IMiLiveSdk.ICallback.LOGIN_OAUTH_AIDL);
            bindService();
        } else {
            try {
                mRemoteService.loginByMiAccountOAuth(
                        MiLiveSdkController.getInstance().getChannelId(),
                        GlobalData.app().getPackageName(),
                        MiLiveSdkController.getInstance().getChannelSecret(),
                        authCode);
            } catch (RemoteException e) {
                mAuthCode = authCode;
                notifyAidlFailure(IMiLiveSdk.ICallback.LOGIN_OAUTH_AIDL);
                bindService();
            }
        }
    }

    public void loginByMiAccountSso(long miid, String serviceToken) {
        Logger.w(TAG, "loginByMiAccountSso miid=" + miid + ",serviceToken=" + serviceToken);
        if (mRemoteService == null) {
            mMiId = miid;
            mServiceToken = serviceToken;
            notifyServiceNull(IMiLiveSdk.ICallback.LOGIN_SSO_AIDL);
            bindService();
        } else {
            try {
                mRemoteService.loginByMiAccountSso(
                        MiLiveSdkController.getInstance().getChannelId(),
                        GlobalData.app().getPackageName(),
                        MiLiveSdkController.getInstance().getChannelSecret(),
                        miid, serviceToken);
            } catch (RemoteException e) {
                mMiId = miid;
                mServiceToken = serviceToken;
                notifyAidlFailure(IMiLiveSdk.ICallback.LOGIN_SSO_AIDL);
                bindService();
            }
        }
    }

    public void clearAccount() {
        Logger.w(TAG, "clearAccount");
        if (mRemoteService == null) {
            mClearAccountFlag = true;
            notifyServiceNull(IMiLiveSdk.ICallback.CLEAR_ACCOUNT_AIDL);
            bindService();
        } else {
            try {
                mRemoteService.clearAccount(
                        MiLiveSdkController.getInstance().getChannelId(),
                        GlobalData.app().getPackageName(),
                        MiLiveSdkController.getInstance().getChannelSecret());
            } catch (RemoteException e) {
                mClearAccountFlag = true;
                notifyAidlFailure(IMiLiveSdk.ICallback.CLEAR_ACCOUNT_AIDL);
                bindService();
            }
        }
    }

    private void notifyServiceNull(int aidlFlag) {
        if (mCallback != null) {
            mCallback.notifyServiceNull(aidlFlag);
        }
    }

    private void notifyAidlFailure(int aidlFlag) {
        if (mCallback != null) {
            mCallback.notifyAidlFailure(aidlFlag);
        }
    }
}
