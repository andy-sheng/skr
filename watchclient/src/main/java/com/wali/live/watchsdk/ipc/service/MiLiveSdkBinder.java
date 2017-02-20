package com.wali.live.watchsdk.ipc.service;

import android.content.Intent;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.callback.ICommonCallBack;
import com.mi.live.data.account.HostChannelManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.account.login.LoginType;
import com.mi.live.data.account.task.AccountCaller;
import com.mi.live.data.account.task.ActionParam;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.AccountProto;
import com.wali.live.proto.SecurityProto;
import com.wali.live.watchsdk.ipc.receiver.ReceiverConstant;
import com.wali.live.watchsdk.request.VerifyRequest;
import com.wali.live.watchsdk.watch.ReplaySdkActivity;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by chengsimin on 2016/12/26.
 */

public class MiLiveSdkBinder extends IMiLiveSdkService.Stub {
    public final static String TAG = MiLiveSdkBinder.class.getSimpleName();
    private static MiLiveSdkBinder sInstance;

    private final HashMap<Integer, String> mAuthMap;
    private final HashMap<Integer, RemoteCallbackList<IMiLiveSdkEventCallback>> mEventCallBackListMap;

    private MiLiveSdkBinder() {
        mAuthMap = new HashMap();
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
    public void openWatch(int channelId, String packageName,
                          final long playerId, final String liveId, final String videoUrl) {
        MyLog.d(TAG, "openWatch channelId=" + channelId);

        secureOperate(channelId, packageName, new ICommonCallBack() {
            @Override
            public void process(Object object) {
                MyLog.d(TAG, "openWatch success callback");

                // TODO FLAG_ACTIVITY_NEW_TASK，目前demo有停留在应用的问题，可以通过去除intent-filter实现
                Intent intent = new Intent(GlobalData.app(), WatchSdkActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, videoUrl).build();
                intent.putExtra(WatchSdkActivity.EXTRA_ROOM_INFO, roomInfo);
                GlobalData.app().startActivity(intent);
            }
        });
    }

    @Override
    public void openReplay(int channelId, String packageName,
                           final long playerId, final String liveId, final String videoUrl) {
        MyLog.d(TAG, "openReplay channelId=" + channelId);

        secureOperate(channelId, packageName, new ICommonCallBack() {
            @Override
            public void process(Object object) {
                MyLog.d(TAG, "openReplay success callback");

                // TODO FLAG_ACTIVITY_NEW_TASK，目前demo有停留在应用的问题，可以通过去除intent-filter实现
                Intent intent = new Intent(GlobalData.app(), ReplaySdkActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, videoUrl).build();
                intent.putExtra(ReplaySdkActivity.EXTRA_ROOM_INFO, roomInfo);
                GlobalData.app().startActivity(intent);
            }
        });
    }

    @Override
    public void openGameLive() {
        MyLog.d(TAG, "openGameLive");
        // TODO
    }

    @Override
    public void loginByMiAccountSso(final int channelId, String packageName,
                                    final long miid, final String serviceToken) throws RemoteException {
        MyLog.d(TAG, "loginByMiAccountSso channelId=" + channelId);

        secureOperate(channelId, packageName, new ICommonCallBack() {
            @Override
            public void process(Object object) {
                MyLog.d(TAG, "loginByMiAccountSso success callback");

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
        });
    }

    @Override
    public void loginByMiAccountOAuth(final int channelId, String packageName, final String code) throws RemoteException {
        MyLog.d(TAG, "loginByMiAccountOAuth channelId=" + channelId);

        secureOperate(channelId, packageName, new ICommonCallBack() {
            @Override
            public void process(Object object) {
                MyLog.d(TAG, "loginByMiAccountOAuth success callback");

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
        });
    }

    @Override
    public void clearAccount(final int channelId, String packageName) throws RemoteException {
        MyLog.d(TAG, "clearAccount channelId=" + channelId);

        secureOperate(channelId, packageName, new ICommonCallBack() {
            @Override
            public void process(Object object) {
                MyLog.d(TAG, "clearAccount success callback");

                // 账号这一块
                UserAccountManager.getInstance().logoff(channelId);
                onEventLogoff(channelId, MiLiveSdkEvent.SUCCESS);
            }
        });
    }

    private void secureOperate(final int channelId, final String packageName, final ICommonCallBack successCallback) {
        if (mAuthMap.containsKey(channelId) && mAuthMap.get(channelId).equals(packageName)) {
            if (successCallback != null) {
                successCallback.process(null);
            }
            return;
        }
        rx.Observable.just(null)
                .map(new Func1<Object, Integer>() {
                    @Override
                    public Integer call(Object o) {
                        SecurityProto.VerifyAssistantRsp rsp = new VerifyRequest(channelId, packageName).syncRsp();
                        if (rsp == null) {
                            return null;
                        }
                        MyLog.d(TAG, "errMsg = " + rsp.getErrMsg());
                        return rsp.getRetCode();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, "verify failure, ");
                        onEventVerifyFailure(channelId, -1);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        MyLog.d(TAG, "onNext integer=" + integer);
                        if (integer != null) {
                            if (integer == 0) {
                                if (successCallback != null) {
                                    successCallback.process(null);
                                }
                                mAuthMap.put(channelId, packageName);
                            } else {
                                onEventVerifyFailure(channelId, integer);
                            }
                        } else {
                            onEventVerifyFailure(channelId, -1);
                        }
                    }
                });
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

    public void onEventVerifyFailure(int channelId, int code) {
        MyLog.d(TAG, "onEventVerifyFailure channelId=" + channelId);

        List<IMiLiveSdkEventCallback> deadCallback = new ArrayList<>(1);
        boolean aidlSuccess = false;
        RemoteCallbackList<IMiLiveSdkEventCallback> callbackList = mEventCallBackListMap.get(channelId);
        if (callbackList != null) {
            MyLog.w(TAG, "callbackList != null");

            int n = callbackList.beginBroadcast();
            for (int i = 0; i < n; i++) {
                IMiLiveSdkEventCallback callback = callbackList.getBroadcastItem(i);
                try {
                    callback.onEventVerifyFailure(code);
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
    }
}
