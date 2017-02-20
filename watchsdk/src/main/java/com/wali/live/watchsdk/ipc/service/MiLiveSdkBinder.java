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
import com.wali.live.watchsdk.ipc.receiver.ReceiverConstant;

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

    private MiLiveSdkBinder() {
//        EventBus.getDefault().register(this);
    }

    public static synchronized MiLiveSdkBinder getInstance() {
        if (sInstance == null) {
            sInstance = new MiLiveSdkBinder();
        }
        return sInstance;
    }

    private final HashMap<Integer, RemoteCallbackList<IMiLiveSdkEventCallback>> mEventCallBackListMap = new HashMap<Integer, RemoteCallbackList<IMiLiveSdkEventCallback>>();

    @Override
    public void setEventCallBack(int channelid, IMiLiveSdkEventCallback callback) throws RemoteException {
        RemoteCallbackList<IMiLiveSdkEventCallback> list = mEventCallBackListMap.get(channelid);
        if (list == null) {
            list = new RemoteCallbackList<>();
            mEventCallBackListMap.put(channelid, list);
        }
        list.register(callback);
    }


    @Override
    public void loginByMiAccountSso(final int channelid, final long miid, String code) throws RemoteException {
        // TODO 这里只是接口保持统一，但是逻辑是不对的，采用的OAuthCode登录方式
        AccountCaller.login(channelid, LoginType.LOGIN_XIAOMI, code, null, null, null, null)
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
                        onEventLogin(channelid, MiLiveSdkEvent.FAILED);
                    }

                    @Override
                    public void onNext(ActionParam actionParam) {
                        MyLog.w(TAG, "miLoginByCode login onNext");
                        if (actionParam != null) {
                            onEventLogin(channelid, actionParam.getErrCode());
                        }
                    }
                });
    }

    @Override
    public void loginByMiAccountOAuth(final int channelid, String code) throws RemoteException {
        AccountCaller.login(channelid, LoginType.LOGIN_XIAOMI, code, null, null, null, null)
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
                        onEventLogin(channelid, MiLiveSdkEvent.FAILED);
                    }

                    @Override
                    public void onNext(ActionParam actionParam) {
                        MyLog.w(TAG, "miLoginByCode login onNext");
                        if (actionParam != null) {
                            onEventLogin(channelid, actionParam.getErrCode());
                        }
                    }
                });
    }

    @Override
    public void clearAccount(int channelid) throws RemoteException {
        // 账号这一块
        UserAccountManager.getInstance().logoff(channelid);
        onEventLogoff(channelid, MiLiveSdkEvent.SUCCESS);
    }

    /**
     * 登出的结果
     *
     * @param channelId
     * @param code
     */
    private void onEventLogoff(int channelId, int code) {
        List<IMiLiveSdkEventCallback> deadCallback = new ArrayList<>(1);
        boolean aidlSuccess = false;
        RemoteCallbackList<IMiLiveSdkEventCallback> mEventCallBackList = mEventCallBackListMap.get(channelId);
        if (mEventCallBackList != null) {
            MyLog.w(TAG, "mEventCallBackList==null");
            int n = mEventCallBackList.beginBroadcast();
            for (int i = 0; i < n; i++) {
                IMiLiveSdkEventCallback callback = mEventCallBackList.getBroadcastItem(i);
                try {
                    callback.onEventLogoff(code);
                    aidlSuccess = true;
                } catch (Exception e) {
                    MyLog.v(TAG, "dead callback.");
                    deadCallback.add(callback);
                }
            }
            mEventCallBackList.finishBroadcast();
            for (IMiLiveSdkEventCallback callback : deadCallback) {
                MyLog.v(TAG, "unregister event callback.");
                mEventCallBackList.unregister(callback);
            }
        }
    }

    /**
     * 登录的结果
     *
     * @param channelId
     * @param code
     */
    public void onEventLogin(int channelId, int code) {
        List<IMiLiveSdkEventCallback> deadCallback = new ArrayList<>(1);
        boolean aidlSuccess = false;
        RemoteCallbackList<IMiLiveSdkEventCallback> mEventCallBackList = mEventCallBackListMap.get(channelId);
        if (mEventCallBackList != null) {
            MyLog.w(TAG, "mEventCallBackList==null");
            int n = mEventCallBackList.beginBroadcast();
            for (int i = 0; i < n; i++) {
                IMiLiveSdkEventCallback callback = mEventCallBackList.getBroadcastItem(i);
                try {
                    callback.onEventLogin(code);
                    aidlSuccess = true;
                } catch (Exception e) {
                    MyLog.v(TAG, "dead callback.");
                    deadCallback.add(callback);
                }
            }
            mEventCallBackList.finishBroadcast();
            for (IMiLiveSdkEventCallback callback : deadCallback) {
                MyLog.v(TAG, "unregister event callback.");
                mEventCallBackList.unregister(callback);
            }
        }
    }

    /**
     * 用户请求登录，要考虑宿主死亡的情况，看需要不需要通过广播通知
     *
     * @param channelId
     */
    public void onEventWantLogin(int channelId) {
        List<IMiLiveSdkEventCallback> deadCallback = new ArrayList<>(1);
        boolean aidlSuccess = false;
        RemoteCallbackList<IMiLiveSdkEventCallback> mEventCallBackList = mEventCallBackListMap.get(channelId);
        if (mEventCallBackList != null) {
            MyLog.w(TAG, "mEventCallBackList==null");

            int n = mEventCallBackList.beginBroadcast();
            for (int i = 0; i < n; i++) {
                IMiLiveSdkEventCallback callback = mEventCallBackList.getBroadcastItem(i);
                try {
                    callback.onEventWantLogin();
                    aidlSuccess = true;
                } catch (Exception e) {
                    MyLog.v(TAG, "dead callback.");
                    deadCallback.add(callback);
                }
            }
            mEventCallBackList.finishBroadcast();
            for (IMiLiveSdkEventCallback callback : deadCallback) {
                MyLog.v(TAG, "unregister event callback.");
                mEventCallBackList.unregister(callback);
            }
        }
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
