package com.wali.live.watchsdk.ipc.service;

import android.content.Intent;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.account.HostChannelManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.account.login.LoginType;
import com.mi.live.data.account.task.AccountCaller;
import com.mi.live.data.account.task.ActionParam;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.AccountProto;
import com.wali.live.watchsdk.ipc.receiver.ReceiverConstant;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by chengsimin on 2016/12/26.
 */

public class MiLiveSdkBinder extends IMiLiveSdkService.Stub {
    public final static String TAG = MiLiveSdkBinder.class.getSimpleName();
    private static MiLiveSdkBinder sInstance;

    private final HashMap<Integer, RemoteCallbackList<IMiLiveSdkEventCallback>> mEventCallBackListMap;

    private MiLiveSdkBinder() {
        mEventCallBackListMap = new HashMap();
    }

    public static synchronized MiLiveSdkBinder getInstance() {
        if (sInstance == null) {
            sInstance = new MiLiveSdkBinder();
        }
        return sInstance;
    }

    @Override
    public void setEventCallBack(int channelId, IMiLiveSdkEventCallback callback) throws RemoteException {
        RemoteCallbackList<IMiLiveSdkEventCallback> list = mEventCallBackListMap.get(channelId);
        if (list == null) {
            list = new RemoteCallbackList();
            mEventCallBackListMap.put(channelId, list);
        }
        list.register(callback);
    }

    @Override
    public void openWatch(long playerId, String liveId, String videoUrl) {
        MyLog.d(TAG, "openWatch");

        /**
         * TODO FLAG_ACTIVITY_NEW_TASK，目前demo有停留在应用的问题，可以通过去除intent-filter实现
         */
        Intent intent = new Intent(GlobalData.app(), WatchSdkActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, videoUrl).build();
        intent.putExtra(WatchSdkActivity.EXTRA_ROOM_INFO, roomInfo);
        GlobalData.app().startActivity(intent);
    }

    @Override
    public void loginByMiAccountSso(final int channelId, final long miid, String serviceToken) throws RemoteException {
        MyLog.d(TAG, "loginByMiAccountSso channelId=" + channelId);

        AccountCaller.miSsoLogin(miid, serviceToken)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AccountProto.MiSsoLoginRsp>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(TAG, "miSsoLogin on completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "miSsoLogin error", e);
                        onEventLogin(channelId, MiLiveSdkEvent.FAILED);
                    }

                    @Override
                    public void onNext(AccountProto.MiSsoLoginRsp miSsoLoginRsp) {
                        try {
                            if (miSsoLoginRsp == null) {
                                MyLog.w(TAG, "miSsoLoginRsp is null");
                                onEventLogin(channelId, MiLiveSdkEvent.FAILED);
                                return;
                            }
                            MyLog.w(TAG, "miSsoLogin retCode=" + miSsoLoginRsp.getRetCode());
                            if (miSsoLoginRsp.getRetCode() == ErrorCode.CODE_ACCOUT_FORBIDDEN) {
                                onEventLogin(channelId, MiLiveSdkEvent.FAILED);
                                return;
                            } else if (miSsoLoginRsp.getRetCode() != ErrorCode.CODE_SUCCESS) {
                                onEventLogin(channelId, MiLiveSdkEvent.FAILED);
                                return;
                            }

                            onEventLogin(channelId, MiLiveSdkEvent.SUCCESS);
                        } catch (Exception e) {
                            MyLog.w(TAG, "miSsoLogin error", e);
                            onEventLogin(channelId, MiLiveSdkEvent.FAILED);
                            return;
                        }
                    }
                });
    }

    @Override
    public void loginByMiAccountOAuth(final int channelId, String code) throws RemoteException {
        MyLog.d(TAG, "loginByMiAccountOAuth channelId=" + channelId);
        AccountCaller.login(channelId, LoginType.LOGIN_XIAOMI, code, null, null, null, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ActionParam>() {
                    @Override
                    public void onCompleted() {
                        MyLog.w(TAG, "miLoginByCode login onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.w(TAG, "miLoginByCode login onError=" + e.getMessage());
                        onEventLogin(channelId, MiLiveSdkEvent.FAILED);
                    }

                    @Override
                    public void onNext(ActionParam actionParam) {
                        MyLog.w(TAG, "miLoginByCode login onNext");
                        if (actionParam != null) {
                            onEventLogin(channelId, actionParam.getErrCode());
                        } else {
                            onEventLogin(channelId, MiLiveSdkEvent.FAILED);
                        }
                    }
                });
    }

    @Override
    public void clearAccount(int channelId) throws RemoteException {
        MyLog.d(TAG, "clearAccount channelId=" + channelId);
        // 账号这一块
        UserAccountManager.getInstance().logoff(channelId);
        onEventLogoff(channelId, MiLiveSdkEvent.SUCCESS);
    }

    /**
     * 登出的结果
     */
    private void onEventLogoff(int channelId, int code) {
        MyLog.d(TAG, "onEventLogoff channelId=" + channelId);

        List<IMiLiveSdkEventCallback> deadCallback = new ArrayList(1);
        boolean aidlSuccess = false;
        RemoteCallbackList<IMiLiveSdkEventCallback> callbackList = mEventCallBackListMap.get(channelId);
        if (callbackList != null) {
            MyLog.w(TAG, "mEventCallBackList==null");

            int n = callbackList.beginBroadcast();
            for (int i = 0; i < n; i++) {
                IMiLiveSdkEventCallback callback = callbackList.getBroadcastItem(i);
                try {
                    callback.onEventLogoff(code);
                    aidlSuccess = true;
                } catch (Exception e) {
                    MyLog.v(TAG, "dead callback.");
                    deadCallback.add(callback);
                }
            }
            callbackList.finishBroadcast();
            for (IMiLiveSdkEventCallback callback : deadCallback) {
                MyLog.v(TAG, "unregister event callback.");
                callbackList.unregister(callback);
            }
        }
        MyLog.d(TAG, "onEventLogoff aidl success=" + aidlSuccess);
    }

    /**
     * 登录的结果
     */
    public void onEventLogin(int channelId, int code) {
        MyLog.d(TAG, "onEventLogin channelId=" + channelId);

        List<IMiLiveSdkEventCallback> deadCallback = new ArrayList(1);
        boolean aidlSuccess = false;
        RemoteCallbackList<IMiLiveSdkEventCallback> callbackList = mEventCallBackListMap.get(channelId);
        if (callbackList != null) {
            MyLog.w(TAG, "callbackList != null");

            int n = callbackList.beginBroadcast();
            for (int i = 0; i < n; i++) {
                IMiLiveSdkEventCallback callback = callbackList.getBroadcastItem(i);
                try {
                    callback.onEventLogin(code);
                    aidlSuccess = true;
                } catch (Exception e) {
                    MyLog.v(TAG, "dead callback.");
                    deadCallback.add(callback);
                }
            }
            callbackList.finishBroadcast();
            for (IMiLiveSdkEventCallback callback : deadCallback) {
                MyLog.v(TAG, "unregister event callback.");
                callbackList.unregister(callback);
            }
        }
        MyLog.d(TAG, "onEventLogin aidl success=" + aidlSuccess);
    }

    /**
     * 用户请求登录，要考虑宿主死亡的情况，看需要不需要通过广播通知
     */
    public void onEventWantLogin(int channelId) {
        MyLog.d(TAG, "onEventWantLogin channelId=" + channelId);

        List<IMiLiveSdkEventCallback> deadCallback = new ArrayList<>(1);
        boolean aidlSuccess = false;
        RemoteCallbackList<IMiLiveSdkEventCallback> callbackList = mEventCallBackListMap.get(channelId);
        if (callbackList != null) {
            MyLog.w(TAG, "callbackList != null");

            int n = callbackList.beginBroadcast();
            for (int i = 0; i < n; i++) {
                IMiLiveSdkEventCallback callback = callbackList.getBroadcastItem(i);
                try {
                    callback.onEventWantLogin();
                    aidlSuccess = true;
                } catch (Exception e) {
                    MyLog.v(TAG, "dead callback.");
                    deadCallback.add(callback);
                }
            }
            callbackList.finishBroadcast();
            for (IMiLiveSdkEventCallback callback : deadCallback) {
                MyLog.v(TAG, "unregister event callback.");
                callbackList.unregister(callback);
            }
        }
        MyLog.d(TAG, "onEventWantLogin aidl success=" + aidlSuccess);
        if (!aidlSuccess) {
            Intent intent = new Intent(ReceiverConstant.ACTION_WANT_LOGIN);
            intent.putExtra(ReceiverConstant.EXTRA_TS, System.currentTimeMillis());
            intent.putExtra(ReceiverConstant.EXTRA_CHANNEL_ID, HostChannelManager.getInstance().getmCurrentChannelId());
            String packagename = HostChannelManager.getInstance().getmPackageName();
            if (!TextUtils.isEmpty(packagename)) {
                intent.setPackage(packagename);
            }
            GlobalData.app().sendBroadcast(intent);
        }
    }
}
