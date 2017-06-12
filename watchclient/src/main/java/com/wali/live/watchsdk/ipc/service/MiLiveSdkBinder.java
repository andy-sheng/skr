package com.wali.live.watchsdk.ipc.service;

import android.app.Activity;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.base.log.MyLog;
import com.base.utils.callback.ICommonCallBack;
import com.google.protobuf.ByteString;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.account.login.LoginType;
import com.mi.live.data.account.task.AccountCaller;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.event.EventClass;
import com.wali.live.proto.AccountProto;
import com.wali.live.proto.CommonChannelProto;
import com.wali.live.proto.ListProto;
import com.wali.live.proto.RelationProto;
import com.wali.live.proto.SecurityProto;
import com.wali.live.watchsdk.AarCallback;
import com.wali.live.watchsdk.callback.ISecureCallBack;
import com.wali.live.watchsdk.callback.SecureCommonCallBack;
import com.wali.live.watchsdk.callback.SecureLoginCallback;
import com.wali.live.watchsdk.list.ChannelLiveCaller;
import com.wali.live.watchsdk.list.RelationCaller;
import com.wali.live.watchsdk.login.UploadService;
import com.wali.live.watchsdk.request.VerifyRequest;
import com.wali.live.watchsdk.watch.VideoDetailSdkActivity;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

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
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
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
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
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
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
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
    public void loginByMiAccountSso(final int channelId, String packageName, String channelSecret,
                                    final long miid, final String serviceToken) throws RemoteException {
        MyLog.w(TAG, "loginByMiAccountSso channelId=" + channelId);
        secureOperate(channelId, packageName, channelSecret, new SecureLoginCallback() {
            @Override
            public void postSuccess() {
                MyLog.w(TAG, "loginByMiAccountSso success callback");

                AccountCaller.miSsoLogin(miid, serviceToken, channelId)
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
                                    UploadService.toUpload(new UploadService.UploadInfo(miSsoLoginRsp, channelId));
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
            public void postActive() {
                MyLog.w(TAG, "loginByMiAccountSso postActive callback");
                onEventOtherAppActive(channelId);
            }

            @Override
            public void processFailure() {
                MyLog.w(TAG, "loginByMiAccountSso failure callback");
                onEventLogin(channelId, MiLiveSdkEvent.FAILED);
            }
        });
    }

    @Override
    public void loginByMiAccountOAuth(final int channelId, String packageName, String channelSecret, final String code) throws RemoteException {
        MyLog.w(TAG, "loginByMiAccountOAuth channelId=" + channelId);

        secureOperate(channelId, packageName, channelSecret, new SecureLoginCallback() {
            @Override
            public void postSuccess() {
                MyLog.w(TAG, "loginByMiAccountOAuth success callback");

                AccountCaller.login(channelId, LoginType.LOGIN_XIAOMI, code, null, null, null, null)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<AccountProto.LoginRsp>() {
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
                            public void onNext(AccountProto.LoginRsp rsp) {
                                MyLog.w(TAG, "miLoginByCode login onNext");
                                if (rsp.getRetCode() == MiLiveSdkEvent.SUCCESS) {
                                    UploadService.toUpload(new UploadService.UploadInfo(rsp, channelId));
                                }
                                if (rsp != null) {
                                    onEventLogin(channelId, rsp.getRetCode());
                                } else {
                                    onEventLogin(channelId, MiLiveSdkEvent.FAILED);
                                }
                            }
                        });
            }

            @Override
            public void postActive() {
                MyLog.w(TAG, "loginByMiAccountOAuth postActive callback");
                onEventOtherAppActive(channelId);
            }

            @Override
            public void processFailure() {
                MyLog.d(TAG, "loginByMiAccountOAuth failure callback");
                onEventLogin(channelId, MiLiveSdkEvent.FAILED);
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
                onEventLogoff(channelId, MiLiveSdkEvent.SUCCESS);
            }

            @Override
            public void processFailure() {
                MyLog.w(TAG, "clearAccount failure callback");
            }
        });
    }

    public void openWatch(final Activity activity, final int channelId, final String packageName, String channelSecret,
                          final long playerId, final String liveId, final String videoUrl, final int liveType, final String gameId, final boolean enableShare,
                          final boolean needFinish) {
        MyLog.w(TAG, "openWatch by activity channelId=" + channelId);

        secureOperate(channelId, packageName, channelSecret, new SecureCommonCallBack() {
            @Override
            public void postSuccess() {
                MyLog.w(TAG, "openWatch by activity success callback");
                // 直接跳转
                RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, videoUrl)
                        .setLiveType(liveType)
                        .setGameId(gameId)
                        .setEnableShare(enableShare)
                        .build();
                WatchSdkActivity.openActivity(activity, roomInfo);
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
                           final long playerId, final String liveId, final String videoUrl, final int liveType, final String gameId, final boolean enableShare,
                           final boolean needFinish) {
        MyLog.w(TAG, "openReplay by activity channelId=" + channelId);

        secureOperate(channelId, packageName, channelSecret, new SecureCommonCallBack() {
            @Override
            public void postSuccess() {
                MyLog.w(TAG, "openReplay by activity success callback");
                // 直接跳转
                RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, videoUrl)
                        .setLiveType(liveType)
                        .setGameId(gameId)
                        .setEnableShare(enableShare)
                        .build();
                // ReplaySdkActivity.openActivity(activity, roomInfo);
                VideoDetailSdkActivity.openActivity(activity, roomInfo);
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

    public void openGameLive(final Activity activity, final int channelId, final String packageName, String channelSecret,
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

    private void secureOperate(final int channelId, final String packageName, final String channelSecret, final ISecureCallBack callback) {
        if (mAuthMap.containsKey(channelId) && mAuthMap.get(channelId).equals(packageName)) {
            if (callback != null) {
                callback.process(channelId, packageName);
            }
            return;
        }
        rx.Observable.just(null)
                .map(new Func1<Object, Integer>() {
                    @Override
                    public Integer call(Object o) {
                        SecurityProto.VerifyAssistantRsp rsp = new VerifyRequest(channelId, packageName, channelSecret).syncRsp();
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
                        callback.processFailure();
                    }

                    @Override
                    public void onNext(Integer integer) {
                        MyLog.d(TAG, "onNext integer=" + integer);
                        if (integer != null && integer == 0) {
                            if (callback != null) {
                                callback.process(channelId, packageName);
                            }
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
        if (mAARCallback != null) {
            mAARCallback.notifyWantLogin();
            return;
        }
        MyLog.d(TAG, "onEventWantLogin channelId=" + channelId);
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
        MyLog.d(TAG, "onEventWantLogin aidl success=" + aidlSuccess);
    }

    public void onEventVerifyFailure(int channelId, int code) {
        if (mAARCallback != null) {
            mAARCallback.notifyVerifyFailure(code);
            return;
        }
        MyLog.d(TAG, "onEventVerifyFailure channelId=" + channelId);
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
        MyLog.d(TAG, "onEventVerifyFailure aidl success=" + aidlSuccess);
    }

    public void onEventOtherAppActive(int channelId) {
        if (mAARCallback != null) {
            mAARCallback.notifyOtherAppActive();
            return;
        }
        MyLog.d(TAG, "onEventOtherApp channelId=" + channelId);
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
        MyLog.d(TAG, "onEventOtherApp aidl success=" + aidlSuccess);
    }

    @Override
    public void thirdPartLogin(String packageName, String channelSecret, final ThirdPartLoginData loginData) throws RemoteException {
        if (loginData != null) {
            MyLog.w(TAG, "thirdPartLogin channelId=" + loginData.getChannelId());
        }
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
                                onEventLogin(channelId, MiLiveSdkEvent.FAILED);
                            }

                            @Override
                            public void onNext(AccountProto.ThirdPartSignLoginRsp rsp) {
                                MyLog.w(TAG, "thirdPartLogin onNext,retCode:" + rsp.getRetCode());
                                if (rsp.getRetCode() == MiLiveSdkEvent.SUCCESS) {
                                    UploadService.toUpload(new UploadService.UploadInfo(rsp, loginData));
                                }
                                if (rsp != null) {
                                    onEventLogin(channelId, rsp.getRetCode());
                                } else {
                                    onEventLogin(channelId, MiLiveSdkEvent.FAILED);
                                }

                            }
                        });
            }

            @Override
            public void postActive() {
                MyLog.w(TAG, "loginByMiAccountOAuth postActive callback");
                onEventOtherAppActive(channelId);
            }

            @Override
            public void processFailure() {
                MyLog.d(TAG, "loginByMiAccountOAuth failure callback");
                onEventLogin(channelId, MiLiveSdkEvent.FAILED);
            }
        });
    }

    public void onEventGetRecommendLives(int channelId, int errCode, List<LiveInfo> liveInfos) {
        MyLog.d(TAG, "onEventGetRecommendLives errCode" + errCode);
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
        MyLog.d(TAG, "onEventGetRecommendLives aidl success=" + aidlSuccess);
    }


    public void onEventGetFollowingUserList(int channelId, int errCode, List<UserInfo> userInfos, int total, long timeStamp) {
        MyLog.d(TAG, "onEventGetFollowingUserList channelId=" + channelId);
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
        MyLog.d(TAG, "onEventGetFollowingUserList aidl success=" + aidlSuccess);
    }


    public void onEventGetFollowingLiveList(int channelId, int errCode, List<LiveInfo> liveInfos) {
        MyLog.d(TAG, "onEventGetFollowingLiveList errCode" + errCode);
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
        MyLog.d(TAG, "onEventGetFollowingLiveList aidl success=" + aidlSuccess);
    }


    public void onEventShare(int channelId, ShareInfo shareInfo) {
        MyLog.d(TAG, "onEventShare");
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
        MyLog.d(TAG, "onEventShare aidl success=" + aidlSuccess);
    }

}
