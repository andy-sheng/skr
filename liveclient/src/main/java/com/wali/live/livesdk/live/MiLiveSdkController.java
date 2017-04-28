package com.wali.live.livesdk.live;

import android.app.Activity;
import android.app.Application;
import android.os.RemoteException;

import com.base.log.MyLog;
import com.base.utils.callback.ICommonCallBack;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.data.LiveShow;
import com.mi.live.data.location.Location;
import com.mi.live.data.manager.UserInfoManager;
import com.wali.live.proto.Live2Proto;
import com.wali.live.proto.LiveProto;
import com.wali.live.watchsdk.AarCallback;
import com.wali.live.watchsdk.IMiLiveSdk;
import com.wali.live.watchsdk.init.InitManager;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkBinder;
import com.wali.live.watchsdk.ipc.service.ThirdPartLoginData;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by chengsimin on 2016/12/8.
 */
public class MiLiveSdkController implements IMiLiveSdk {
    public static final String TAG = MiLiveSdkController.class.getSimpleName();

    private static final MiLiveSdkController sSdkController = new MiLiveSdkController();

    private int mChannelId = 0;
    private String mChannelSecret;
    private String mPackageName;

    private AarCallback mCallback;

    public static IMiLiveSdk getInstance() {
        return sSdkController;
    }

    public void init(Application app, int channelId, String channelSecret) {
        InitManager.init(app, null);
        MyLog.d(TAG, "init channelId=" + channelId);
        mChannelId = channelId;
        mChannelSecret = channelSecret;
        mPackageName = app.getPackageName();
    }

    public void setCallback(ICallback callback) {
        if (callback == null) {
            return;
        }
        if (mCallback == null) {
            mCallback = new AarCallback();
        }
        mCallback.setCallback(callback);
        if (mCallback != null) {
            MiLiveSdkBinder.getInstance().setCallback(mCallback);
        }
    }

    private void checkHasInit() {
        if (mChannelId == 0 || mCallback == null) {
            throw new RuntimeException("channelId==0 or callback is null");
        }
    }

    @Override
    public void loginByMiAccountOAuth(String authCode) {
        checkHasInit();
        try {
            MiLiveSdkBinder.getInstance().loginByMiAccountOAuth(mChannelId, mPackageName, mChannelSecret, authCode);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loginByMiAccountSso(long miid, String serviceToken) {
        checkHasInit();
        try {
            MiLiveSdkBinder.getInstance().loginByMiAccountSso(mChannelId, mPackageName, mChannelSecret, miid, serviceToken);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void thirdPartLogin(int channelId, String xuid, int sex, String nickname, String headUrl, String sign) {
        checkHasInit();
        ThirdPartLoginData thirdPartLoginData = new ThirdPartLoginData();
        thirdPartLoginData.setChannelId(channelId);
        thirdPartLoginData.setXuid(xuid);
        thirdPartLoginData.setSex(sex);
        thirdPartLoginData.setNickname(nickname);
        thirdPartLoginData.setHeadUrl(headUrl);
        thirdPartLoginData.setSign(sign);
        try {
            MiLiveSdkBinder.getInstance().thirdPartLogin(mPackageName, mChannelSecret, thirdPartLoginData);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getChannelLives(IChannelCallback callback) {
        checkHasInit();
        mCallback.setChannelCallback(callback);
        try {
            MiLiveSdkBinder.getInstance().getChannelLives(mChannelId, mPackageName, mChannelSecret);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getFollowingList(boolean isBothWay, long timeStamp, IFollowingListCallback callback) {
        checkHasInit();
        mCallback.setFollowingListCallback(callback);
        try {
            MiLiveSdkBinder.getInstance().getFollowingList(mChannelId, mPackageName, mChannelSecret, isBothWay, timeStamp);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notifyShareSuc(int type) {
        checkHasInit();
        try {
            MiLiveSdkBinder.getInstance().notifyShareSuc(mChannelId, mPackageName, mChannelSecret, type);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearAccount() {
        checkHasInit();
        try {
            MiLiveSdkBinder.getInstance().clearAccount(mChannelId, mPackageName, mChannelSecret);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openWatch(Activity activity, long playerId, String liveId, String videoUrl, int liveType, int shareType) {
        checkHasInit();
        MiLiveSdkBinder.getInstance().openWatch(activity, mChannelId, mPackageName, mChannelSecret,
                playerId, liveId, videoUrl, liveType, null, shareType, false);
    }

    @Override
    public void openReplay(Activity activity, long playerId, String liveId, String videoUrl, int liveType, int shareType) {
        checkHasInit();
        MiLiveSdkBinder.getInstance().openReplay(activity, mChannelId, mPackageName, mChannelSecret,
                playerId, liveId, videoUrl, liveType, null, shareType, false);
    }

    @Override
    public void openWatch(Activity activity, long playerId, String liveId, String videoUrl, int liveType, String gameId, int shareType) {
        checkHasInit();
        MiLiveSdkBinder.getInstance().openWatch(activity, mChannelId, mPackageName, mChannelSecret,
                playerId, liveId, videoUrl, liveType, gameId, shareType, false);
    }

    @Override
    public void openReplay(Activity activity, long playerId, String liveId, String videoUrl, int liveType, String gameId, int shareType) {
        checkHasInit();
        MiLiveSdkBinder.getInstance().openReplay(activity, mChannelId, mPackageName, mChannelSecret,
                playerId, liveId, videoUrl, liveType, gameId, shareType, false);
    }

    @Override
    public void openNormalLive(final Activity activity, final Location location, final int shareType) {
        checkHasInit();
        MiLiveSdkBinder.getInstance().openNormalLive(activity, mChannelId, mPackageName, mChannelSecret,
                new ICommonCallBack() {
                    @Override
                    public void process(Object objects) {
                        LiveSdkActivity.openActivity(activity, location, shareType, false);
                    }
                }, false);
    }

    @Override
    public void openGameLive(final Activity activity, final Location location, final int shareType) {
        checkHasInit();
        MiLiveSdkBinder.getInstance().openGameLive(activity, mChannelId, mPackageName, mChannelSecret,
                new ICommonCallBack() {
                    @Override
                    public void process(Object objects) {
                        LiveSdkActivity.openActivity(activity, location, shareType, true);
                    }
                }, false);
    }

    @Override
    public void enterReplay(final Activity activity, final String playerId) {
        Observable
                .just(0)
                .map(new Func1<Integer, Object>() {
                    @Override
                    public Object call(Integer integer) {
                        /**
                         * 内部接口，这里方便demo测试使用，外层应用请不要随意调用
                         */
                        LiveProto.HistoryLiveRsp rsp = LiveManager.historyRsp(Long.parseLong(playerId));
                        if (rsp == null) {
                            return null;
                        }
                        Live2Proto.HisLive hisLive = rsp.getHisLive(0);
                        MiLiveSdkController.getInstance().openReplay(
                                activity, Long.parseLong(playerId), hisLive.getLiveId(), hisLive.getUrl(), 0, null, 0);
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    @Override
    public void enterWatch(final Activity activity, final String playerId) {
        Observable
                .just(0)
                .map(new Func1<Integer, Object>() {
                    @Override
                    public Object call(Integer integer) {
                        /**
                         * 内部接口，这里方便demo测试使用，外层应用请不要随意调用
                         */
                        LiveShow liveShow = UserInfoManager.getLiveShowByUserId(Long.parseLong(playerId));
                        if (liveShow == null) {
                            return null;
                        }
                        MiLiveSdkController.getInstance().openWatch(
                                activity, liveShow.getUid(), liveShow.getLiveId(), liveShow.getUrl(), 0, null, 0);
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }
}
