package com.wali.live.watchsdk.ipc.service;

import android.app.Activity;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.callback.ICommonCallBack;
import com.google.protobuf.ByteString;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.account.login.LoginType;
import com.mi.live.data.account.task.AccountCaller;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.event.EventClass;
import com.wali.live.proto.AccountProto;
import com.wali.live.proto.CommonChannelProto;
import com.wali.live.proto.ListProto;
import com.wali.live.proto.RelationProto;
import com.wali.live.proto.SecurityProto;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.watchsdk.AarCallback;
import com.wali.live.watchsdk.callback.ISecureCallBack;
import com.wali.live.watchsdk.callback.SecureCommonCallBack;
import com.wali.live.watchsdk.callback.SecureLoginCallback;
import com.wali.live.watchsdk.list.ChannelLiveCaller;
import com.wali.live.watchsdk.list.RelationCaller;
import com.wali.live.watchsdk.login.UploadService;
import com.wali.live.watchsdk.request.VerifyRequest;
import com.wali.live.watchsdk.statistics.MilinkStatistics;

import org.greenrobot.eventbus.EventBus;

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
    private AarCallback mAARCallback;

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

    public void setCallback(AarCallback callback) {
        mAARCallback = callback;
    }

    @Override
    public void checkService() throws RemoteException {
        //nothing to do
    }

    @Override
    public void getChannelLives(final int channelId, String packageName, final String channelSecret) throws RemoteException {
        MyLog.w(TAG, "getChannelLives");
        secureOperate(channelId, packageName, channelSecret, new SecureCommonCallBack() {
            @Override
            public void postSuccess() {
                ChannelLiveCaller.getChannelLive(channelId)
                        .subscribe(new Observer<ListProto.GetChannelLiveDetailRsp>() {
                            @Override
                            public void onCompleted() {
                                MyLog.w(TAG, "getChannelLives onCompleted");
                            }

                            @Override
                            public void onError(Throwable e) {
                                MyLog.e(e);
                                onEventGetRecommendLives(channelId, ErrorCode.CODE_ERROR_NORMAL, null);
                            }

                            @Override
                            public void onNext(ListProto.GetChannelLiveDetailRsp getChannelLiveDetailRsp) {
                                int errCode = ErrorCode.CODE_ERROR_NORMAL;
                                if (getChannelLiveDetailRsp == null || (errCode = getChannelLiveDetailRsp.getRet()) != ErrorCode.CODE_SUCCESS) {
                                    //拉列表失败
                                    MyLog.e(TAG, "getChannelLive failed channelId=" + channelId);
                                    onEventGetRecommendLives(channelId, errCode, null);
                                    return;
                                }
                                List<LiveInfo> liveInfos = new ArrayList<LiveInfo>();
                                List<ListProto.Item> items = getChannelLiveDetailRsp.getItemList();
                                for (ListProto.Item item : items) {
                                    if (item != null && item.getType() == ChannelLiveCaller.TYPE_LIVE) {
                                        List<ByteString> list = item.getDataList();
                                        if (list != null) {
                                            for (ByteString bs : list) {
                                                try {
                                                    CommonChannelProto.LiveInfo liveInfoPb = CommonChannelProto.LiveInfo.parseFrom(bs.toByteArray());
                                                    liveInfos.add(new LiveInfo(liveInfoPb));
                                                } catch (Exception e) {
                                                    MyLog.e(TAG, e);
                                                }
                                            }

                                        }
                                    }
                                }
                                onEventGetRecommendLives(channelId, errCode, liveInfos);
                            }
                        });
            }

            @Override
            public void postError() {
            }

            @Override
            public void processFailure() {
            }
        });
    }

    @Override
    public void getFollowingUserList(final int channelId, final String packageName, final String channelSecret, final boolean isBothWay, final long timeStamp) throws RemoteException {
        MyLog.w(TAG, "getFollowingUserList channelId=" + channelId + " isBothWay=" + isBothWay + " timeStamp=" + timeStamp);
        secureOperate(channelId, packageName, channelSecret, new SecureCommonCallBack() {
            @Override
            public void postSuccess() {
                RelationCaller.getFollowingList(UserAccountManager.getInstance().getUuidAsLong(), isBothWay, timeStamp)
                        .subscribe(new Observer<RelationProto.FollowingListResponse>() {
                            @Override
                            public void onCompleted() {
                                MyLog.w(TAG, "getFollowingUserList onCompleted");
                            }

                            @Override
                            public void onError(Throwable e) {
                                MyLog.e(e);
                                onEventGetFollowingUserList(channelId, ErrorCode.CODE_ERROR_NORMAL, null, 0, 0);
                            }

                            @Override
                            public void onNext(RelationProto.FollowingListResponse followingListResponse) {
                                int errCode = ErrorCode.CODE_ERROR_NORMAL;
                                if (followingListResponse == null || (errCode = followingListResponse.getCode()) != ErrorCode.CODE_SUCCESS) {
                                    //拉列表失败
                                    MyLog.e(TAG, "getFollowingUserList failed channelId=" + channelId);
                                    onEventGetFollowingUserList(channelId, errCode, null, 0, 0);
                                    return;
                                }
                                List<UserInfo> userInfos = new ArrayList<UserInfo>();
                                List<RelationProto.UserInfo> items = followingListResponse.getUsersList();
                                for (RelationProto.UserInfo item : items) {
                                    userInfos.add(new UserInfo(item));
                                }
                                onEventGetFollowingUserList(channelId, errCode, userInfos, followingListResponse.getTotal(), followingListResponse.getSyncTime());
                            }
                        });

            }

            @Override
            public void postError() {
            }

            @Override
            public void processFailure() {

            }
        });
    }

    @Override
    public void getFollowingLiveList(final int channelId, final String packageName, final String channelSecret) throws RemoteException {
        MyLog.w(TAG, "getFollowingLiveList channelId=" + channelId + " packageName=" + packageName + " channelSecret=" + channelSecret);
        secureOperate(channelId, packageName, channelSecret, new SecureCommonCallBack() {
            @Override
            public void postSuccess() {
                ChannelLiveCaller.getFollowingLives()
                        .subscribe(new Observer<ListProto.GetFollowLiveRsp>() {
                            @Override
                            public void onCompleted() {
                                MyLog.w(TAG, "getFollowingLiveList onCompleted");
                            }

                            @Override
                            public void onError(Throwable e) {
                                MyLog.e(e);
                                onEventGetFollowingLiveList(channelId, ErrorCode.CODE_ERROR_NORMAL, null);
                            }

                            @Override
                            public void onNext(ListProto.GetFollowLiveRsp getFollowLiveRsp) {
                                int errCode = ErrorCode.CODE_ERROR_NORMAL;
                                if (getFollowLiveRsp == null || (errCode = getFollowLiveRsp.getRet()) != ErrorCode.CODE_SUCCESS) {
                                    //拉列表失败
                                    MyLog.e(TAG, "getFollowingLiveList failed channelId=" + channelId);
                                    onEventGetFollowingLiveList(channelId, errCode, null);
                                    return;
                                }
                                List<LiveInfo> liveInfos = new ArrayList<>();
                                for (CommonChannelProto.LiveInfo liveInfo : getFollowLiveRsp.getLivesList()) {
                                    liveInfos.add(new LiveInfo(liveInfo));
                                }
                                onEventGetFollowingLiveList(channelId, errCode, liveInfos);
                            }
                        });
            }

            @Override
            public void postError() {

            }

            @Override
            public void processFailure() {

            }
        });
    }

    @Override
    public void notifyShare(final int channelId, final String packageName, final String channelSecret, final boolean success, final int type) throws RemoteException {
        secureOperate(channelId, packageName, channelSecret, new SecureCommonCallBack() {
            @Override
            public void postSuccess() {
                MyLog.w(TAG, "notifyShareSuc type=" + type + " success=" + success);
                EventBus.getDefault().post(new EventClass.ShareEvent(success ? EventClass.ShareEvent.TYPE_SUCCESS :
                        EventClass.ShareEvent.TYPE_FAILED, type));
            }

            @Override
            public void postError() {
            }

            @Override
            public void processFailure() {
            }
        });
    }

    @Override
    public void statistic(String key, long time) throws RemoteException {
        MyLog.w(TAG, "statistic key" + key);
        //场包cta弹窗之前，不能有网络访问。
        if (CommonUtils.isNeedShowCtaDialog()) {
            MyLog.w(TAG, "statistic isNeedShowCtaDialog true");
            return;
        }
        StatisticsAlmightyWorker.getsInstance().recordImmediatelyDefault(key, time);
        MilinkStatistics.getInstance().statisticsGameActive(key, time);
    }

    @Override
    public void loginByMiAccountSso(final int channelId, final String packageName, final String channelSecret,
                                    final long miid, final String serviceToken) throws RemoteException {
        MyLog.w(TAG, "loginByMiAccountSso channelId=" + channelId);
        reportLoginEntrance(channelId, miid);

        secureOperate(channelId, packageName, channelSecret, new SecureLoginCallback(miid) {
            @Override
            public void postSuccess() {
                MyLog.w(TAG, "loginByMiAccountSso success callback");

                AccountCaller.miSsoLogin(miid, serviceToken, channelId)
                        .subscribe(new Observer<AccountProto.MiSsoLoginRsp>() {
                            @Override
                            public void onCompleted() {
                                MyLog.w(TAG, "miSsoLogin on completed");
                            }

                            @Override
                            public void onError(Throwable e) {
                                MyLog.e(TAG, "miSsoLogin error", e);
                                onEventLogin(channelId, ErrorCode.CODE_EXCEPTION);
                            }

                            @Override
                            public void onNext(AccountProto.MiSsoLoginRsp miSsoLoginRsp) {
                                try {
                                    if (miSsoLoginRsp == null) {
                                        MyLog.w(TAG, "miSsoLoginRsp is null");
                                        onEventLogin(channelId, ErrorCode.CODE_TIME_OUT);
                                        return;
                                    }

                                    int code = miSsoLoginRsp.getRetCode();
                                    MyLog.w(TAG, "miSsoLogin retCode=" + code);
                                    if (code == ErrorCode.CODE_ACCOUT_FORBIDDEN) {
                                        onEventLogin(channelId, code);
                                        return;
                                    } else if (code != ErrorCode.CODE_SUCCESS) {
                                        onEventLogin(channelId, code);
                                        return;
                                    }

                                    reportLoginSuccess(channelId, miid);
                                    UploadService.toUpload(new UploadService.UploadInfo(miSsoLoginRsp, channelId));
                                    onEventLogin(channelId, code);
                                } catch (Exception e) {
                                    MyLog.w(TAG, "miSsoLogin error", e);
                                    onEventLogin(channelId, ErrorCode.CODE_EXCEPTION);
                                    return;
                                }
                            }
                        });
            }

            @Override
            public void postSame() {
                MyLog.w(TAG, "loginByMiAccountSso postSame callback");
                onEventLogin(channelId, ErrorCode.CODE_SUCCESS);
            }

            @Override
            public void postActive() {
                MyLog.w(TAG, "loginByMiAccountSso postActive callback");
                onEventOtherAppActive(channelId);
            }

            @Override
            public void processFailure() {
                MyLog.w(TAG, "loginByMiAccountSso failure callback");
                onEventLogin(channelId, ErrorCode.CODE_ERROR_NORMAL);
            }
        });
    }

    private void reportLoginEntrance(int channelId, long miid) {
        //场包cta弹窗之前，不能有网络访问。
        if (CommonUtils.isNeedShowCtaDialog()) {
            MyLog.w(TAG, "reportLoginEntrance isNeedShowCtaDialog true");
            return;
        }
        try {
            String key = String.format(StatisticsKey.KEY_SDK_LOGIN_ENTRANCE, channelId, miid);
            MyLog.w(TAG, "reportLoginEntrance key=" + key);
            StatisticsAlmightyWorker.getsInstance().recordImmediatelyDefault(key, 1);
        } catch (Exception e) {
            MyLog.e(TAG, "reportLoginEntrance e", e);
        }
    }

    private void reportLoginSuccess(int channelId, long miid) {
        try {
            String key = String.format(StatisticsKey.KEY_SDK_LOGIN_SUCCESS, channelId, miid);
            MyLog.w(TAG, "reportLoginSuccess key=" + key);
            StatisticsAlmightyWorker.getsInstance().recordImmediatelyDefault(key, 1);
        } catch (Exception e) {
            MyLog.e(TAG, "reportLoginSuccess e", e);
        }
    }

    @Override
    public void loginByMiAccountOAuth(final int channelId, String packageName, String channelSecret, final String code) throws RemoteException {
        MyLog.w(TAG, "loginByMiAccountOAuth channelId=" + channelId);
        secureOperate(channelId, packageName, channelSecret, new SecureLoginCallback() {
            @Override
            public void postSuccess() {
                MyLog.w(TAG, "loginByMiAccountOAuth success callback");

                AccountCaller.login(channelId, LoginType.LOGIN_XIAOMI, code, null, null, null, null)
                        .subscribe(new Observer<AccountProto.LoginRsp>() {
                            @Override
                            public void onCompleted() {
                                MyLog.w(TAG, "miLoginByCode login onCompleted");
                            }

                            @Override
                            public void onError(Throwable e) {
                                MyLog.w(TAG, "miLoginByCode login onError=" + e.getMessage());
                                onEventLogin(channelId, ErrorCode.CODE_EXCEPTION);
                            }

                            @Override
                            public void onNext(AccountProto.LoginRsp rsp) {
                                MyLog.w(TAG, "miLoginByCode login onNext");
                                if (rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
                                    UploadService.toUpload(new UploadService.UploadInfo(rsp, channelId));
                                }
                                if (rsp != null) {
                                    onEventLogin(channelId, rsp.getRetCode());
                                } else {
                                    onEventLogin(channelId, ErrorCode.CODE_TIME_OUT);
                                }
                            }
                        });
            }

            @Override
            public void postSame() {
                MyLog.w(TAG, "loginByMiAccountOAuth post callback");
                onEventLogin(channelId, ErrorCode.CODE_SUCCESS);
            }

            @Override
            public void postActive() {
                MyLog.w(TAG, "loginByMiAccountOAuth postActive callback");
                onEventOtherAppActive(channelId);
            }

            @Override
            public void processFailure() {
                MyLog.d(TAG, "loginByMiAccountOAuth failure callback");
                onEventLogin(channelId, ErrorCode.CODE_ERROR_NORMAL);
            }
        });
    }

    @Override
    public void clearAccount(final int channelId, String packageName, String channelSecret) throws RemoteException {
        MyLog.w(TAG, "clearAccount channelId=" + channelId);

        secureOperate(channelId, packageName, channelSecret, new ISecureCallBack() {
            @Override
            public void process(Object... objects) {
                MyLog.w(TAG, "clearAccount success before callback channelId=" + channelId);

                // 账号这一块
                UserAccountManager.getInstance().logoff(channelId);
                onEventLogoff(channelId, ErrorCode.CODE_SUCCESS);
            }

            @Override
            public void processFailure() {
                MyLog.w(TAG, "clearAccount failure callback");
            }
        });
    }

    public void openWatch(final Activity activity, final int channelId, final String packageName, String channelSecret,
                          final ICommonCallBack callback, final boolean needFinish) {
        MyLog.w(TAG, "openWatch by activity channelId=" + channelId);

        secureOperate(channelId, packageName, channelSecret, new SecureCommonCallBack() {
            @Override
            public void postSuccess() {
                MyLog.w(TAG, "openWatch by activity success callback");
                // 上层回调跳转
                if (callback != null) {
                    callback.process(null);
                }
                if (needFinish) {
                    activity.finish();
                }
            }

            @Override
            public void postError() {
                MyLog.w(TAG, "openWatch by activity postError callback");
                if (needFinish) {
                    activity.finish();
                }
            }

            @Override
            public void processFailure() {
                MyLog.w(TAG, "openWatch by activity failure callback");
                if (needFinish) {
                    activity.finish();
                }
            }
        });
    }

    public void openReplay(final Activity activity, final int channelId, final String packageName, String channelSecret,
                           final ICommonCallBack callback, final boolean needFinish) {
        MyLog.w(TAG, "openReplay by activity channelId=" + channelId);

        secureOperate(channelId, packageName, channelSecret, new SecureCommonCallBack() {
            @Override
            public void postSuccess() {
                MyLog.w(TAG, "openReplay by activity success callback");
                // 上层回调跳转
                if (callback != null) {
                    callback.process(null);
                }
                if (needFinish) {
                    activity.finish();
                }
            }

            @Override
            public void postError() {
                MyLog.w(TAG, "openReplay by activity postError callback");
                if (needFinish) {
                    activity.finish();
                }
            }

            @Override
            public void processFailure() {
                MyLog.w(TAG, "openReplay by activity failure callback");
                if (needFinish) {
                    activity.finish();
                }
            }
        });
    }

    public void openNormalLive(final Activity activity, final int channelId, final String packageName, String channelSecret,
                               final ICommonCallBack callback, final boolean needFinish) {
        MyLog.w(TAG, "openNormalLive by activity channelId=" + channelId);
        secureOperate(channelId, packageName, channelSecret, new SecureCommonCallBack() {
            @Override
            public void postSuccess() {
                MyLog.w(TAG, "openNormalLive by activity success callback");
                // 上层回调跳转
                if (callback != null) {
                    callback.process(null);
                }
                if (needFinish) {
                    activity.finish();
                }
            }

            @Override
            public void postError() {
                MyLog.w(TAG, "openNormalLive by activity postError callback");
                if (needFinish) {
                    activity.finish();
                }
            }

            @Override
            public void processFailure() {
                MyLog.w(TAG, "openNormalLive by activity failure callback");
                if (needFinish) {
                    activity.finish();
                }
            }
        });
    }

    public void openGameLive(final Activity activity, final int channelId, final String packageName, final String channelSecret,
                             final ICommonCallBack callback, final boolean needFinish) {
        MyLog.w(TAG, "openGameLive by activity channelId=" + channelId);

        secureOperate(channelId, packageName, channelSecret, new SecureCommonCallBack() {
            @Override
            public void postSuccess() {
                MyLog.w(TAG, "openGameLive by activity success callback");
                // 上层回调跳转
                if (callback != null) {
                    callback.process(null);
                }
                if (needFinish) {
                    activity.finish();
                }
            }

            @Override
            public void postError() {
                MyLog.w(TAG, "openGameLive by activity postError callback");
                if (needFinish) {
                    activity.finish();
                }
            }

            @Override
            public void processFailure() {
                MyLog.w(TAG, "openGameLive by activity failure callback");
                if (needFinish) {
                    activity.finish();
                }
            }
        });
    }

    public void secureOperate(final int channelId, final String packageName,
                              final String channelSecret, final ISecureCallBack callback) {
        if (callback == null) {
            MyLog.w(TAG, " secureOperate callback is null");
            return;
        }
        //场包cta弹窗之前，不能有网络访问。
        if (CommonUtils.isNeedShowCtaDialog()) {
            MyLog.w(TAG, "secureOperate isNeedShowCtaDialog true");
            callback.processFailure();
            return;
        }
        if (mAuthMap.containsKey(channelId) && mAuthMap.get(channelId).equals(packageName)) {
            callback.process(channelId, packageName);
            return;
        }
        rx.Observable.just(0)
                .map(new Func1<Object, Integer>() {
                    @Override
                    public Integer call(Object o) {
                        SecurityProto.VerifyAssistantRsp rsp = new VerifyRequest(channelId, packageName, channelSecret).syncRsp();
                        if (rsp == null) {
                            MyLog.e(TAG, "verify rsp is null");
                            return null;
                        }
                        MyLog.e(TAG, "errMsg=" + rsp.getErrMsg() + " errCode=" + rsp.getRetCode());
                        return rsp.getRetCode();
                    }
                })
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "verify failure", e);
                        onEventVerifyFailure(channelId, -1);
                        callback.processFailure();
                    }

                    @Override
                    public void onNext(Integer integer) {
                        MyLog.w(TAG, "onNext integer=" + integer);
                        if (integer != null && integer == 0) {
                            callback.process(channelId, packageName);
                            mAuthMap.put(channelId, packageName);
                            return;
                        }
                        onEventVerifyFailure(channelId, integer == null ? -1 : integer);
                        callback.processFailure();
                    }
                });
    }

    /**
     * 登出的结果
     */
    private void onEventLogoff(int channelId, int code) {
        if (mAARCallback != null) {
            mAARCallback.notifyLogoff(code);
            return;
        }
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
        if (mAARCallback != null) {
            mAARCallback.notifyLogin(code);
            return;
        }
        MyLog.w(TAG, "onEventLogin channelId=" + channelId);
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
        MyLog.w(TAG, "onEventLogin aidl success=" + aidlSuccess);
    }

    /**
     * 用户请求登录，要考虑宿主死亡的情况，看需要不需要通过广播通知
     */
    public void onEventWantLogin(int channelId) {
        if (mAARCallback != null) {
            mAARCallback.notifyWantLogin();
            return;
        }
        MyLog.w(TAG, "onEventWantLogin channelId=" + channelId);
        List<IMiLiveSdkEventCallback> deadCallback = new ArrayList(1);
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
        MyLog.w(TAG, "onEventWantLogin aidl success=" + aidlSuccess);
    }

    public void onEventVerifyFailure(int channelId, int code) {
        if (mAARCallback != null) {
            mAARCallback.notifyVerifyFailure(code);
            return;
        }
        MyLog.w(TAG, "onEventVerifyFailure channelId=" + channelId);
        List<IMiLiveSdkEventCallback> deadCallback = new ArrayList(1);
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
        MyLog.w(TAG, "onEventVerifyFailure aidl success=" + aidlSuccess);
    }

    public void onEventOtherAppActive(int channelId) {
        if (mAARCallback != null) {
            mAARCallback.notifyOtherAppActive();
            return;
        }
        MyLog.w(TAG, "onEventOtherApp channelId=" + channelId);
        List<IMiLiveSdkEventCallback> deadCallback = new ArrayList(1);
        boolean aidlSuccess = false;
        RemoteCallbackList<IMiLiveSdkEventCallback> callbackList = mEventCallBackListMap.get(channelId);
        if (callbackList != null) {
            MyLog.w(TAG, "callbackList != null");
            int n = callbackList.beginBroadcast();
            for (int i = 0; i < n; i++) {
                IMiLiveSdkEventCallback callback = callbackList.getBroadcastItem(i);
                try {
                    callback.onEventOtherAppActive();
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
        MyLog.w(TAG, "onEventOtherApp aidl success=" + aidlSuccess);
    }

    @Override
    public void thirdPartLogin(String packageName, String channelSecret, final ThirdPartLoginData loginData) throws RemoteException {
        if (loginData == null) {
            MyLog.w(TAG, "thirdPartLogin loginData is null");
            return;
        }
        MyLog.w(TAG, "thirdPartLogin channelId=" + loginData.getChannelId());
        final int channelId = loginData.getChannelId();
        secureOperate(loginData.getChannelId(), packageName, channelSecret, new SecureLoginCallback() {
            @Override
            public void postSuccess() {
                AccountCaller.login(loginData.getChannelId(), loginData.getXuid(), loginData.getSex(), loginData.getNickname(), loginData.getHeadUrl(), loginData.getSign())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<AccountProto.ThirdPartSignLoginRsp>() {
                            @Override
                            public void onCompleted() {
                                MyLog.w(TAG, "thirdPartLogin on completed");
                            }

                            @Override
                            public void onError(Throwable e) {
                                MyLog.e(TAG, "thirdPartLogin error", e);
                                onEventLogin(channelId, ErrorCode.CODE_EXCEPTION);
                            }

                            @Override
                            public void onNext(AccountProto.ThirdPartSignLoginRsp rsp) {
                                MyLog.w(TAG, "thirdPartLogin onNext,retCode:" + rsp.getRetCode());
                                if (rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
                                    UploadService.toUpload(new UploadService.UploadInfo(rsp, loginData));
                                }
                                if (rsp != null) {
                                    onEventLogin(channelId, rsp.getRetCode());
                                } else {
                                    onEventLogin(channelId, ErrorCode.CODE_TIME_OUT);
                                }

                            }
                        });
            }

            @Override
            public void postSame() {
                MyLog.w(TAG, "thirdPartLogin postSame callback");
                onEventLogin(channelId, ErrorCode.CODE_SUCCESS);
            }

            @Override
            public void postActive() {
                MyLog.w(TAG, "thirdPartLogin postActive callback");
                onEventOtherAppActive(channelId);
            }

            @Override
            public void processFailure() {
                MyLog.d(TAG, "thirdPartLogin failure callback");
                onEventLogin(channelId, ErrorCode.CODE_ERROR_NORMAL);
            }
        });
    }

    public void onEventGetRecommendLives(int channelId, int errCode, List<LiveInfo> liveInfos) {
        MyLog.w(TAG, "onEventGetRecommendLives errCode" + errCode);
        if (mAARCallback != null) {
            mAARCallback.notifyGetChannelLives(errCode, liveInfos);
            return;
        }
        List<IMiLiveSdkEventCallback> deadCallback = new ArrayList(1);
        boolean aidlSuccess = false;
        RemoteCallbackList<IMiLiveSdkEventCallback> callbackList = mEventCallBackListMap.get(channelId);
        if (callbackList != null) {
            MyLog.w(TAG, "callbackList != null");
            int n = callbackList.beginBroadcast();
            for (int i = 0; i < n; i++) {
                IMiLiveSdkEventCallback callback = callbackList.getBroadcastItem(i);
                try {
                    callback.onEventGetRecommendLives(errCode, liveInfos);
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
        MyLog.w(TAG, "onEventGetRecommendLives aidl success=" + aidlSuccess);
    }


    public void onEventGetFollowingUserList(int channelId, int errCode, List<UserInfo> userInfos, int total, long timeStamp) {
        MyLog.w(TAG, "onEventGetFollowingUserList channelId=" + channelId);
        if (mAARCallback != null) {
            mAARCallback.notifyGetFollowingUserList(errCode, userInfos, total, timeStamp);
            return;
        }
        List<IMiLiveSdkEventCallback> deadCallback = new ArrayList(1);
        boolean aidlSuccess = false;
        RemoteCallbackList<IMiLiveSdkEventCallback> callbackList = mEventCallBackListMap.get(channelId);
        if (callbackList != null) {
            MyLog.w(TAG, "callbackList != null");
            int n = callbackList.beginBroadcast();
            for (int i = 0; i < n; i++) {
                IMiLiveSdkEventCallback callback = callbackList.getBroadcastItem(i);
                try {
                    callback.onEventGetFollowingUserList(errCode, userInfos, total, timeStamp);
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
        MyLog.w(TAG, "onEventGetFollowingUserList aidl success=" + aidlSuccess);
    }

    public void onEventGetFollowingLiveList(int channelId, int errCode, List<LiveInfo> liveInfos) {
        MyLog.w(TAG, "onEventGetFollowingLiveList errCode" + errCode);
        if (mAARCallback != null) {
            mAARCallback.notifyGetFollowingLiveList(errCode, liveInfos);
            return;
        }
        List<IMiLiveSdkEventCallback> deadCallback = new ArrayList(1);
        boolean aidlSuccess = false;
        RemoteCallbackList<IMiLiveSdkEventCallback> callbackList = mEventCallBackListMap.get(channelId);
        if (callbackList != null) {
            MyLog.w(TAG, "callbackList != null");
            int n = callbackList.beginBroadcast();
            for (int i = 0; i < n; i++) {
                IMiLiveSdkEventCallback callback = callbackList.getBroadcastItem(i);
                try {
                    callback.onEventGetFollowingLiveList(errCode, liveInfos);
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
        MyLog.w(TAG, "onEventGetFollowingLiveList aidl success=" + aidlSuccess);
    }


    public void onEventShare(int channelId, ShareInfo shareInfo) {
        MyLog.w(TAG, "onEventShare");
        if (mAARCallback != null) {
            mAARCallback.notifyWantShare(shareInfo);
            return;
        }
        List<IMiLiveSdkEventCallback> deadCallback = new ArrayList<>(1);
        boolean aidlSuccess = false;
        RemoteCallbackList<IMiLiveSdkEventCallback> callbackList = mEventCallBackListMap.get(channelId);
        if (callbackList != null) {
            MyLog.w(TAG, "callbackList != null");
            int n = callbackList.beginBroadcast();
            for (int i = 0; i < n; i++) {
                IMiLiveSdkEventCallback callback = callbackList.getBroadcastItem(i);
                try {
                    MyLog.w(TAG, "onEventShare sub for i=" + i);
                    callback.onEventShare(shareInfo);
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
        MyLog.w(TAG, "onEventShare aidl success=" + aidlSuccess);
    }
}
