package com.wali.live.watchsdk.ipc.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.wali.live.sdk.manager.IMiLiveSdk;
import com.wali.live.sdk.manager.MiLiveSdkController;
import com.wali.live.sdk.manager.global.GlobalData;
import com.wali.live.sdk.manager.version.VersionCheckManager;

/**
 * Created by chengsimin on 2016/12/27.
 */
public class MiLiveSdkServiceProxy implements ServiceConnection {
    public final static String TAG = MiLiveSdkServiceProxy.class.getSimpleName();

    private Intent mIntent;
    private IMiLiveSdkService remoteService;

    private long mMiId;
    private String mServiceToken;
    private String mAuthCode;

    private boolean mClearAccountFlag = false;

    private IMiLiveSdk.ICallback mCallback;

    private IMiLiveSdkEventCallback mLiveSdkEventCallback = new IMiLiveSdkEventCallback.Stub() {
        @Override
        public void onEventLogin(int code) throws RemoteException {
            Log.d(TAG, "onEventLoginResult:" + code);
            MiLiveSdkEvent.postLogin(code);
        }

        @Override
        public void onEventLogoff(int code) throws RemoteException {
            Log.d(TAG, "onEventLogoff:" + code);
            MiLiveSdkEvent.postLogoff(code);
        }

        @Override
        public void onEventWantLogin() throws RemoteException {
            Log.d(TAG, "onEventWantLogin");
            MiLiveSdkEvent.postWantLogin();
        }

        @Override
        public void onEventVerifyFailure(int code) throws RemoteException {
            Log.d(TAG, "onEventVerifyFailure code=" + code);
            MiLiveSdkEvent.postVerifyFailure(code);
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
        Log.w(TAG, "bindService " + mIntent);
        GlobalData.app().bindService(mIntent, this, Context.BIND_AUTO_CREATE);
    }

    private void stopService() {
        GlobalData.app().stopService(mIntent);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        Log.w(TAG, "onServiceConnected");
        remoteService = IMiLiveSdkService.Stub.asInterface(service);
        try {
            remoteService.setEventCallBack(
                    MiLiveSdkController.getInstance().getChannelId(),
                    mLiveSdkEventCallback);
            // 尝试处理登录
            if (!TextUtils.isEmpty(mServiceToken)) {
                remoteService.loginByMiAccountSso(
                        MiLiveSdkController.getInstance().getChannelId(),
                        GlobalData.app().getPackageName(),
                        mMiId, mServiceToken);
                mServiceToken = "";
            } else if (!TextUtils.isEmpty(mAuthCode)) {
                remoteService.loginByMiAccountOAuth(
                        MiLiveSdkController.getInstance().getChannelId(),
                        GlobalData.app().getPackageName(),
                        mAuthCode);
                mAuthCode = "";
            } else if (mClearAccountFlag) {
                remoteService.clearAccount(
                        MiLiveSdkController.getInstance().getChannelId(),
                        GlobalData.app().getPackageName());
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

//    @Deprecated
//    public void openWatch(long playerId, String liveId, String videoUrl) {
//        Log.w(TAG, "openWatch");
//        if (remoteService == null) {
//            bindService();
//        } else {
//            try {
//                remoteService.openWatch(
//                        MiLiveSdkController.getInstance().getChannelId(),
//                        GlobalData.app().getPackageName(),
//                        playerId, liveId, videoUrl);
//            } catch (RemoteException e) {
//                bindService();
//            }
//        }
//    }
//
//    @Deprecated
//    public void openReplay(long playerId, String liveId, String videoUrl) {
//        Log.w(TAG, "openReplay");
//        if (remoteService == null) {
//            bindService();
//        } else {
//            try {
//                remoteService.openReplay(
//                        MiLiveSdkController.getInstance().getChannelId(),
//                        GlobalData.app().getPackageName(),
//                        playerId, liveId, videoUrl);
//            } catch (RemoteException e) {
//                bindService();
//            }
//        }
//    }
//
//    @Deprecated
//    public void openGameLive() {
//        Log.w(TAG, "openGameLive");
//        if (remoteService == null) {
//            bindService();
//        } else {
//            try {
//                remoteService.openGameLive();
//            } catch (RemoteException e) {
//                bindService();
//            }
//        }
//    }

    public void loginByMiAccountOAuth(String authCode) {
        Log.w(TAG, "loginByMiAccount authCode=" + authCode);
        if (remoteService == null) {
            mAuthCode = authCode;
            notifyServiceNull(IMiLiveSdk.ICallback.LOGIN_OAUTH_AIDL);
            bindService();
        } else {
            try {
                remoteService.loginByMiAccountOAuth(
                        MiLiveSdkController.getInstance().getChannelId(),
                        GlobalData.app().getPackageName(),
                        authCode);
            } catch (RemoteException e) {
                mAuthCode = authCode;
                notifyAidlFailure(IMiLiveSdk.ICallback.LOGIN_OAUTH_AIDL);
                bindService();
            }
        }
    }

    public void loginByMiAccountSso(long miid, String serviceToken) {
        Log.w(TAG, "loginByMiAccountSso miid=" + miid + ",serviceToken=" + serviceToken);
        if (remoteService == null) {
            mMiId = miid;
            mServiceToken = serviceToken;
            notifyServiceNull(IMiLiveSdk.ICallback.LOGIN_SSO_AIDL);
            bindService();
        } else {
            try {
                remoteService.loginByMiAccountSso(
                        MiLiveSdkController.getInstance().getChannelId(),
                        GlobalData.app().getPackageName(),
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
        Log.w(TAG, "clearAccount");
        if (remoteService == null) {
            mClearAccountFlag = true;
            notifyServiceNull(IMiLiveSdk.ICallback.CLEAR_ACCOUNT_AIDL);
            bindService();
        } else {
            try {
                remoteService.clearAccount(
                        MiLiveSdkController.getInstance().getChannelId(),
                        GlobalData.app().getPackageName());
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
