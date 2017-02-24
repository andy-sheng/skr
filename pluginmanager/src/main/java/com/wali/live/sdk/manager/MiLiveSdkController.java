package com.wali.live.sdk.manager;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.wali.live.sdk.manager.global.GlobalData;
import com.wali.live.sdk.manager.log.Logger;
import com.wali.live.sdk.manager.version.VersionCheckManager;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkServiceProxy;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chengsimin on 2016/12/8.
 */
public class MiLiveSdkController implements IMiLiveSdk {
    public static final String TAG = MiLiveSdkController.class.getSimpleName();

    private static final String EXTRA_CHANNEL_ID = "extra_channel_id";
    private static final String EXTRA_PACKAGE_NAME = "extra_package_name";
    private static final String EXTRA_CHANNEL_SECRET = "extra_channel_secret";

    private static final String EXTRA_PLAYER_ID = "extra_player_id";
    private static final String EXTRA_LIVE_ID = "extra_live_id";
    private static final String EXTRA_VIDEO_URL = "extra_video_url";
    private static final String EXTRA_LIVE_TYPE = "extra_live_type";

    private static final String ACTION_OPEN_WATCH = "open_watch";
    private static final String ACTION_OPEN_REPLAY = "open_replay";
    private static final String ACTION_LOGIN_OAUTH = "login_oauth";
    private static final String ACTION_LOGIN_SSO = "login_sso";
    private static final String ACTION_CLEAR_ACCOUNT = "clear_account";

    private static final MiLiveSdkController sSdkController = new MiLiveSdkController();

    private Map<String, Integer> mMinVersionMap = new HashMap();
    private int mApkVersion;

    private int mChannelId = 0;
    private String mChannelSecret;

    private ICallback mCallback;

    private MiLiveSdkController() {
        mMinVersionMap.put(ACTION_OPEN_WATCH, 204000);
        mMinVersionMap.put(ACTION_OPEN_REPLAY, 204000);

        mMinVersionMap.put(ACTION_LOGIN_OAUTH, 204000);
        mMinVersionMap.put(ACTION_LOGIN_SSO, 204000);
        mMinVersionMap.put(ACTION_CLEAR_ACCOUNT, 204000);
    }

    public static IMiLiveSdk getInstance() {
        return sSdkController;
    }

    public void init(Application app, int channelId, String channelSecret, ICallback callback) {
        GlobalData.setApplication(app);
        Logger.d(TAG, "init channelId=" + channelId);
        mChannelId = channelId;
        mChannelSecret = channelSecret;
        mCallback = callback;

        MiLiveSdkServiceProxy.getInstance().setCallback(mCallback);
        checkHasInit();

        getApkVersion();
    }

    @Override
    public void setLogEnabled(boolean isEnabled) {
        Logger.setEnabled(isEnabled);
    }

    private void checkHasInit() {
        if (mChannelId == 0) {
            throw new RuntimeException("channelId==0, make sure MiLiveSdkController.init(...) be called.");
        }
        MiLiveSdkServiceProxy.getInstance().tryInit();
    }

    private void getApkVersion() {
        try {
            PackageInfo packageInfo = GlobalData.app().getPackageManager().getPackageInfo(
                    VersionCheckManager.PACKAGE_NAME, PackageManager.GET_META_DATA);
            int versionCode = packageInfo.versionCode;
            Logger.d(TAG, "versionCode=" + versionCode);
            mApkVersion = versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, e.getMessage());
        }
    }

    private boolean checkVersion(String action, IAssistantCallback callback) {
        int version = mMinVersionMap.get(action);
        if (version > mApkVersion) {
            getApkVersion();
        }
        if (mApkVersion == 0) {
            callback.notifyNotInstall();
            return false;
        }
        if (version > mApkVersion) {
            callback.notifyVersionLow();
            return false;
        }
        return true;
    }

    @Override
    public void setChannelId(int channelId) {
        mChannelId = channelId;
    }

    @Override
    public int getChannelId() {
        return mChannelId;
    }

    @Override
    public String getChannelSecret() {
        return mChannelSecret;
    }

    @Override
    public void openWatch(Activity activity, long playerId, String liveId, String videoUrl, int liveType, IAssistantCallback callback) {
        checkHasInit();
        if (!checkVersion(ACTION_OPEN_WATCH, callback)) {
            return;
        }

        Bundle bundle = getBasicBundle();
        bundle.putLong(EXTRA_PLAYER_ID, playerId);
        bundle.putString(EXTRA_LIVE_ID, liveId);
        bundle.putString(EXTRA_VIDEO_URL, videoUrl);
        bundle.putInt(EXTRA_LIVE_TYPE, liveType);
        jumpToSdk(activity, bundle, ACTION_OPEN_WATCH, callback);
    }

    @Override
    public void openReplay(Activity activity, long playerId, String liveId, String videoUrl, int liveType, IAssistantCallback callback) {
        checkHasInit();
        if (!checkVersion(ACTION_OPEN_REPLAY, callback)) {
            return;
        }

        Bundle bundle = getBasicBundle();
        bundle.putLong(EXTRA_PLAYER_ID, playerId);
        bundle.putString(EXTRA_LIVE_ID, liveId);
        bundle.putString(EXTRA_VIDEO_URL, videoUrl);
        bundle.putInt(EXTRA_LIVE_TYPE, liveType);
        jumpToSdk(activity, bundle, ACTION_OPEN_REPLAY, callback);
    }

//    @Override
//    public void openGameLive() {
//        if (hasInstallLiveSdk()) {
//            MiLiveSdkServiceProxy.getInstance().openGameLive();
//        } else {
//            ToastUtils.showToast(R.string.cannot_find_livesdk);
//        }
//    }

    @Override
    public void loginByMiAccountOAuth(String authCode, IAssistantCallback callback) {
        checkHasInit();
        if (!checkVersion(ACTION_LOGIN_OAUTH, callback)) {
            return;
        }
        MiLiveSdkServiceProxy.getInstance().loginByMiAccountOAuth(authCode);
    }

    @Override
    public void loginByMiAccountSso(long miid, String serviceToken, IAssistantCallback callback) {
        checkHasInit();
        if (!checkVersion(ACTION_LOGIN_SSO, callback)) {
            return;
        }
        MiLiveSdkServiceProxy.getInstance().loginByMiAccountSso(miid, serviceToken);
    }

    @Override
    public void clearAccount(IAssistantCallback callback) {
        checkHasInit();
        if (!checkVersion(ACTION_CLEAR_ACCOUNT, callback)) {
            return;
        }
        MiLiveSdkServiceProxy.getInstance().clearAccount();
    }

    @Override
    public boolean hasInstallLiveSdk() {
        PackageInfo pInfo = null;
        try {
            pInfo = GlobalData.app().getPackageManager().getPackageInfo(
                    VersionCheckManager.PACKAGE_NAME, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, e.getMessage());
        }
        return pInfo != null;
    }

    private void jumpToSdk(@NonNull Activity activity, @NonNull Bundle bundle, @NonNull String action, IAssistantCallback callback) {
        Logger.d(TAG, "jumpToSdk action=" + action);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName(VersionCheckManager.PACKAGE_NAME, VersionCheckManager.JUMP_CLASS_NAME);
        intent.putExtras(bundle);
        intent.setAction(action);
        if (!startActivity(activity, intent)) {
            if (callback != null) {
                callback.notifyNotInstall();
                getApkVersion();
            }
        }
    }

    private Bundle getBasicBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_CHANNEL_ID, mChannelId);
        bundle.putString(EXTRA_PACKAGE_NAME, GlobalData.app().getPackageName());
        bundle.putString(EXTRA_CHANNEL_SECRET, mChannelSecret);
        return bundle;
    }

    private boolean startActivity(Activity activity, Intent intent) {
        Logger.d(TAG, "start activity action=" + intent.getAction());
        if (intent.resolveActivity(GlobalData.app().getPackageManager()) != null) {
            try {
                activity.startActivity(intent);
                return true;
            } catch (Exception e) {
            }
        }
        return false;
    }
}
