package com.wali.live.livesdk.live;

import android.app.Activity;
import android.app.Application;
import android.os.IBinder;
import android.os.RemoteException;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.callback.ICommonCallBack;
import com.mi.live.data.location.Location;
import com.wali.live.watchsdk.ipc.service.IMiLiveSdkEventCallback;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkBinder;
import com.wali.live.watchsdk.ipc.service.ThirdPartLoginData;

/**
 * Created by chengsimin on 2016/12/8.
 */
public class MiLiveSdkController implements IMiLiveSdk {

    public static final String TAG = MiLiveSdkController.class.getSimpleName();
    private static final MiLiveSdkController sSdkController = new MiLiveSdkController();

    private int mChannelId = 0;
    private String mChannelSecret;
    private ICallback mCallback;
    private String mPackageName;

    public static IMiLiveSdk getInstance() {
        return sSdkController;
    }

    public void init(Application app, int channelId, String channelSecret, ICallback callback) {
        GlobalData.setApplication(app);
        MyLog.d(TAG, "init channelId=" + channelId);
        mChannelId = channelId;
        mChannelSecret = channelSecret;
        mCallback = callback;
        mPackageName = app.getPackageName();
        try {
            MiLiveSdkBinder.getInstance().setEventCallBack(channelId, new IMiLiveSdkEventCallback() {
                @Override
                public void onEventLogin(int code) throws RemoteException {
                    if (mCallback != null) {
                        mCallback.notifyLogin(code);
                    }
                }

                @Override
                public void onEventLogoff(int code) throws RemoteException {
                    if (mCallback != null) {
                        mCallback.notifyLogoff(code);
                    }
                }

                @Override
                public void onEventWantLogin() throws RemoteException {
                    if (mCallback != null) {
                        mCallback.notifyWantLogin();
                    }
                }

                @Override
                public void onEventVerifyFailure(int code) throws RemoteException {
                    if (mCallback != null) {
                        mCallback.notifyVerifyFailure(code);
                    }
                }

                @Override
                public void onEventOtherAppActive() throws RemoteException {
                    if (mCallback != null) {
                        mCallback.notifyOtherAppActive();
                    }
                }

                @Override
                public IBinder asBinder() {
                    return null;
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void checkHasInit() {
        if (mChannelId == 0) {
            throw new RuntimeException("channelId==0, make sure MiLiveSdkController.init(...) be called.");
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
    public void clearAccount() {
        try {
            MiLiveSdkBinder.getInstance().clearAccount(mChannelId, mPackageName, mChannelSecret);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openWatch(Activity activity, long playerId, String liveId, String videoUrl, int liveType) {
        checkHasInit();
        MiLiveSdkBinder.getInstance().openWatch(activity, mChannelId, mPackageName, mChannelSecret,
                playerId, liveId, videoUrl, liveType, false);
    }

    @Override
    public void openReplay(Activity activity, long playerId, String liveId, String videoUrl, int liveType) {
        checkHasInit();
        MiLiveSdkBinder.getInstance().openReplay(activity, mChannelId, mPackageName, mChannelSecret,
                playerId, liveId, videoUrl, liveType, false);
    }

    @Override
    public void openNormalLive(final Activity activity, final Location location) {
        checkHasInit();
        MiLiveSdkBinder.getInstance().openNormalLive(activity, mChannelId, mPackageName, mChannelSecret,
                new ICommonCallBack() {
                    @Override
                    public void process(Object objects) {
                        LiveSdkActivity.openActivity(activity, location, false);
                    }
                }, false);
    }

    @Override
    public void openGameLive(final Activity activity, final Location location) {
        checkHasInit();
        MiLiveSdkBinder.getInstance().openGameLive(activity, mChannelId, mPackageName, mChannelSecret,
                new ICommonCallBack() {
                    @Override
                    public void process(Object objects) {
                        LiveSdkActivity.openActivity(activity, location, false);
                    }
                }, false);
    }
}
