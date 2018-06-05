package com.wali.live.watchsdk.ipc.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.wali.live.sdk.manager.IMiLiveSdk;
import com.wali.live.sdk.manager.MiLiveSdkController;
import com.wali.live.sdk.manager.global.GlobalData;
import com.wali.live.sdk.manager.log.Logger;
import com.wali.live.sdk.manager.version.VersionCheckManager;

import java.util.List;

/**
 * Created by chengsimin on 2016/12/27.
 */
public class MiLiveSdkServiceProxy implements ServiceConnection {
    public final static String TAG = "MiLiveSdkServiceProxy";

    private Intent mIntent;
    private IMiLiveSdkService mRemoteService;

    private long mMiId;
    private String mServiceToken;
    private String mAuthCode;
    private ThirdPartLoginData mThirdPartLoginData;

    private boolean mClearAccountFlag = false;

    private String mStatisticsKey = null;
    private long mStatisticsTime = 0;

    private IMiLiveSdk.ICallback mCallback;
    private IMiLiveSdk.IChannelAssistantCallback mChannelCallback;
    private IMiLiveSdk.IFollowingUsersCallback mFollowingListCallback;
    private IMiLiveSdk.IFollowingLivesCallback mFollowingLivesCallback;
    private IMiLiveSdk.IGetBarrageCallback mGetBarrageCallback;

    private IMiLiveSdkEventCallback mLiveSdkEventCallback = new IMiLiveSdkEventCallback.Stub() {
        @Override
        public void onEventLogin(int code) throws RemoteException {
            Logger.w(TAG, "onEventLoginResult:" + code);
            if (mCallback != null) {
                mCallback.notifyLogin(code);
            }
        }

        @Override
        public void onEventLogoff(int code) throws RemoteException {
            Logger.w(TAG, "onEventLogoff:" + code);
            if (mCallback != null) {
                mCallback.notifyLogoff(code);
            }
        }

        @Override
        public void onEventWantLogin() throws RemoteException {
            Logger.w(TAG, "onEventWantLogin");
            if (mCallback != null) {
                mCallback.notifyWantLogin();
            }
        }

        @Override
        public void onEventVerifyFailure(int code) throws RemoteException {
            Logger.w(TAG, "onEventVerifyFailure code=" + code);
            if (mCallback != null) {
                mCallback.notifyVerifyFailure(code);
            }
        }

        @Override
        public void onEventOtherAppActive() throws RemoteException {
            Logger.w(TAG, "onEventOtherAppActive");
            if (mCallback != null) {
                mCallback.notifyOtherAppActive();
            }
        }

        @Override
        public void onEventGetRecommendLives(int errCode, List<LiveInfo> liveInfos) throws RemoteException {
            Logger.w(TAG, "onEventGetRecommendLives errCode=" + errCode);
            if (mChannelCallback != null) {
                mChannelCallback.notifyGetChannelLives(errCode, liveInfos);
                mChannelCallback = null;
            }
        }

        @Override
        public void onEventGetFollowingUserList(int errCode, List<UserInfo> userInfos, int total, long timeStamp) throws RemoteException {
            Logger.w(TAG, "onEventGetFollowingUserList");
            if (mFollowingListCallback != null) {
                mFollowingListCallback.notifyGetFollowingUserList(errCode, userInfos, total, timeStamp);
                mFollowingListCallback = null;
            }
        }

        @Override
        public void onEventGetFollowingLiveList(int errCode, List<LiveInfo> liveInfos) throws RemoteException {
            Logger.w(TAG, "onEventGetFollowingLiveList");
            if (mFollowingLivesCallback != null) {
                mFollowingLivesCallback.notifyGetFollowingLiveList(errCode, liveInfos);
                mFollowingLivesCallback = null;
            }
        }

        @Override
        public void onEventShare(ShareInfo shareInfo) throws RemoteException {
            Logger.w(TAG, "onEventVerifyFailure");
            if (mCallback != null) {
                mCallback.notifyWantShare(shareInfo);
            }
        }

        @Override
        public void onEventRecvBarrage(List<BarrageInfo> barrageInfos) throws RemoteException {
            Logger.w(TAG, "onEventVerifyFailure");
            if (mGetBarrageCallback != null) {
                mGetBarrageCallback.notifyGetBarrageList(barrageInfos);
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
        mRemoteService = null;
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
            }
            if (!TextUtils.isEmpty(mAuthCode)) {
                mRemoteService.loginByMiAccountOAuth(
                        MiLiveSdkController.getInstance().getChannelId(),
                        GlobalData.app().getPackageName(),
                        MiLiveSdkController.getInstance().getChannelSecret(),
                        mAuthCode);
                mAuthCode = "";
            }
            if (mClearAccountFlag) {
                mRemoteService.clearAccount(
                        MiLiveSdkController.getInstance().getChannelId(),
                        GlobalData.app().getPackageName(),
                        MiLiveSdkController.getInstance().getChannelSecret());
                mClearAccountFlag = false;
            }
            if (mThirdPartLoginData != null) {
                mRemoteService.thirdPartLogin(GlobalData.app().getPackageName(), MiLiveSdkController.getInstance().getChannelSecret(), mThirdPartLoginData);
                mThirdPartLoginData = null;
            }
            if (mStatisticsKey != null) {
                mRemoteService.statistic(mStatisticsKey, mStatisticsTime);
                mStatisticsKey = null;
                mStatisticsTime = 0;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Logger.w(TAG, "onServiceDisconnected");
    }

    public void initService() {
        if (mRemoteService == null) {
            bindService();
        }
    }

    public void clearService() {
        mRemoteService = null;
    }

    public void loginByMiAccountOAuth(String authCode) {
        Logger.w(TAG, "loginByMiAccount authCode=" + authCode);
        if (mRemoteService == null) {
            mAuthCode = authCode;
            resolveNullService(IMiLiveSdk.ICallback.LOGIN_OAUTH_AIDL);
        } else {
            try {
                mRemoteService.loginByMiAccountOAuth(
                        MiLiveSdkController.getInstance().getChannelId(),
                        GlobalData.app().getPackageName(),
                        MiLiveSdkController.getInstance().getChannelSecret(),
                        authCode);
            } catch (RemoteException e) {
                mAuthCode = authCode;
                resolveException(e, IMiLiveSdk.ICallback.LOGIN_OAUTH_AIDL);
            }
        }
    }

    public void loginByMiAccountSso(long miid, String serviceToken) {
        Logger.w(TAG, "loginByMiAccountSso miid=" + miid + ",serviceToken=" + serviceToken);
        if (mRemoteService == null) {
            mMiId = miid;
            mServiceToken = serviceToken;
            resolveNullService(IMiLiveSdk.ICallback.LOGIN_SSO_AIDL);
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
                resolveException(e, IMiLiveSdk.ICallback.LOGIN_SSO_AIDL);
            }
        }
    }

    public void thirdPartLogin(int channelId, String xuid, int sex, String nickname, String headUrl, String sign) {
        Logger.w(TAG, "thirdPartLogin channelId=" + channelId + ", xuid=" + xuid);
        ThirdPartLoginData data = new ThirdPartLoginData(channelId, xuid, sex, nickname, headUrl, sign);
        if (mRemoteService == null) {
            mThirdPartLoginData = data;
            resolveNullService(IMiLiveSdk.ICallback.THIRD_PART_LOGIN);
        } else {
            try {
                mRemoteService.thirdPartLogin(GlobalData.app().getPackageName(), MiLiveSdkController.getInstance().getChannelSecret(), data);
            } catch (RemoteException e) {
                mThirdPartLoginData = data;
                resolveException(e, IMiLiveSdk.ICallback.THIRD_PART_LOGIN);
            }
        }
    }

    public void editUserInfo(int channelId, String xuid, int sex, String nickname, String headUrl) {
        Logger.w(TAG, "editUserInfo channelId=" + channelId + ", xuid=" + xuid);
        ThirdPartLoginData data = new ThirdPartLoginData(channelId, xuid, sex, nickname, headUrl, "");
        if (mRemoteService == null) {
            resolveNullService(IMiLiveSdk.ICallback.EDIT_USER_INFO);
        } else {
            try {
                mRemoteService.editUserInfo(GlobalData.app().getPackageName(), MiLiveSdkController.getInstance().getChannelSecret(), data);
            } catch (RemoteException e) {
                mThirdPartLoginData = data;
                resolveException(e, IMiLiveSdk.ICallback.EDIT_USER_INFO);
            }
        }
    }

    public void doFeedBack() {
        Logger.w(TAG, "doFeedBack");
        if (mRemoteService == null) {
            resolveNullService(IMiLiveSdk.ICallback.DO_FEED_BACK);
        } else {
            try {
                mRemoteService.doFeedBack(MiLiveSdkController.getInstance().getChannelId(),
                        GlobalData.app().getPackageName(),
                        MiLiveSdkController.getInstance().getChannelSecret());
            } catch (RemoteException e) {
                resolveException(e, IMiLiveSdk.ICallback.DO_FEED_BACK);
            }
        }
    }


    public void getChannelLives(IMiLiveSdk.IChannelAssistantCallback channelCallback) {
        Logger.w(TAG, "getChannelLives");
        if (channelCallback == null) {
            Logger.w(TAG, "getChannelLives callback is null");
            return;
        }
        mChannelCallback = channelCallback;
        if (mRemoteService == null) {
            resolveNullService(IMiLiveSdk.ICallback.GET_CHANNEL_LIVES);
        } else {
            try {
                mRemoteService.getChannelLives(MiLiveSdkController.getInstance().getChannelId(), GlobalData.app().getPackageName(),
                        MiLiveSdkController.getInstance().getChannelSecret());
            } catch (RemoteException e) {
                resolveException(e, IMiLiveSdk.ICallback.GET_CHANNEL_LIVES);
            }
        }
    }

    public void getFollowingUsers(boolean isBothWay, long timeStamp, IMiLiveSdk.IFollowingUsersCallback followingUsersCallback) {
        Logger.w(TAG, "getFollowingUsers");
        if (followingUsersCallback == null) {
            Logger.w(TAG, "getFollowingUsers callback is null");
            return;
        }
        mFollowingListCallback = followingUsersCallback;
        if (mRemoteService == null) {
            resolveNullService(IMiLiveSdk.ICallback.GET_FOLLOWING_USERS);
        } else {
            try {
                mRemoteService.getFollowingUserList(MiLiveSdkController.getInstance().getChannelId(), GlobalData.app().getPackageName(),
                        MiLiveSdkController.getInstance().getChannelSecret(), isBothWay, timeStamp);
            } catch (RemoteException e) {
                resolveException(e, IMiLiveSdk.ICallback.GET_FOLLOWING_USERS);
            }
        }
    }

    public void getFollowingLives(IMiLiveSdk.IFollowingLivesCallback followingLivesCallback) {
        Logger.w(TAG, "getFollowingLives");
        if (followingLivesCallback == null) {
            Logger.w(TAG, "getFollowingLives callback is null");
            return;
        }
        mFollowingLivesCallback = followingLivesCallback;
        if (mRemoteService == null) {
            resolveNullService(IMiLiveSdk.ICallback.GET_FOLLOWING_LIVES);
        } else {
            try {
                mRemoteService.getFollowingLiveList(MiLiveSdkController.getInstance().getChannelId(), GlobalData.app().getPackageName(),
                        MiLiveSdkController.getInstance().getChannelSecret());
            } catch (RemoteException e) {
                resolveException(e, IMiLiveSdk.ICallback.GET_FOLLOWING_LIVES);
            }
        }
    }

    public void startBarragePull(String roomId, IMiLiveSdk.IGetBarrageCallback getBarrageCallback) {
        Logger.w(TAG, "startBarragePull");
        if (getBarrageCallback == null) {
            Logger.w(TAG, "startBarragePull callback is null");
            return;
        }
        mGetBarrageCallback = getBarrageCallback;
        if (mRemoteService == null) {
            resolveNullService(IMiLiveSdk.ICallback.GET_BARRAGE);
        } else {
            try {
                mRemoteService.startBarragePull(MiLiveSdkController.getInstance().getChannelId(), GlobalData.app().getPackageName(),
                        MiLiveSdkController.getInstance().getChannelSecret(), roomId);
            } catch (RemoteException e) {
                resolveException(e, IMiLiveSdk.ICallback.GET_BARRAGE);
            }
        }
    }

    public void stopBarragePull() {
        Logger.w(TAG, "stopBarragePull");
        mGetBarrageCallback = null;
        if (mRemoteService == null) {
            resolveNullService(IMiLiveSdk.ICallback.GET_BARRAGE);
        } else {
            try {
                mRemoteService.stopBarragePull(MiLiveSdkController.getInstance().getChannelId(), GlobalData.app().getPackageName(),
                        MiLiveSdkController.getInstance().getChannelSecret());
            } catch (RemoteException e) {
                resolveException(e, IMiLiveSdk.ICallback.GET_BARRAGE);
            }
        }
    }

    public void notifyShare(boolean success, int type) {
        if (mRemoteService == null) {
            resolveNullService(IMiLiveSdk.ICallback.NOTIFY_SHARE_AIDL);
        } else {
            try {
                mRemoteService.notifyShare(MiLiveSdkController.getInstance().getChannelId(), GlobalData.app().getPackageName(),
                        MiLiveSdkController.getInstance().getChannelSecret(), success, type);
            } catch (RemoteException e) {
                resolveException(e, IMiLiveSdk.ICallback.NOTIFY_SHARE_AIDL);
            }
        }
    }


    public void clearAccount() {
        Logger.w(TAG, "clearAccount");
        if (mRemoteService == null) {
            mClearAccountFlag = true;
            resolveNullService(IMiLiveSdk.ICallback.CLEAR_ACCOUNT_AIDL);
        } else {
            try {
                mRemoteService.clearAccount(
                        MiLiveSdkController.getInstance().getChannelId(),
                        GlobalData.app().getPackageName(),
                        MiLiveSdkController.getInstance().getChannelSecret());
            } catch (RemoteException e) {
                mClearAccountFlag = true;
                resolveException(e, IMiLiveSdk.ICallback.CLEAR_ACCOUNT_AIDL);
            }
        }
    }

    public void checkService() {
        Logger.w(TAG, "checkService");
        if (mRemoteService == null) {
            bindService();
        } else {
            try {
                mRemoteService.checkService();
            } catch (RemoteException e) {
                if (e instanceof DeadObjectException) {
                    bindService();
                }
            }
        }
    }

    public void statistic(String key, long time) {
        Logger.w(TAG, "statistic key=" + key);
        if (mRemoteService == null) {
            mStatisticsKey = key;
            mStatisticsTime = time;
            bindService();
        } else {
            try {
                mRemoteService.statistic(key, time);
            } catch (RemoteException e) {
                if (e instanceof DeadObjectException) {
                    mStatisticsKey = key;
                    mStatisticsTime = time;
                    bindService();
                }
            }
        }
    }

    private void resolveNullService(int aidlFlag) {
        notifyServiceNull(aidlFlag);
        bindService();
    }

    private void resolveException(RemoteException e, int aidlFlag) {
        Logger.e(TAG, "remote exception=", e);
        notifyAidlFailure(aidlFlag);
        if (e instanceof DeadObjectException) {
            bindService();
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
