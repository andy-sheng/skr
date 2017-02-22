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

    private static final MiLiveSdkController sSdkController = new MiLiveSdkController();

    private int mChannelId = 0;
    private String mChannelSecret;

    private ICallback mCallback;

    private MiLiveSdkController() {
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

        checkHasInit();
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
        MiLiveSdkServiceProxy.getInstance().setCallback(mCallback);
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
    public void openWatch(Activity activity, long playerId, String liveId, String videoUrl) {
        checkHasInit();

        Bundle bundle = getBasicBundle();
        bundle.putLong(EXTRA_PLAYER_ID, playerId);
        bundle.putString(EXTRA_LIVE_ID, liveId);
        bundle.putString(EXTRA_VIDEO_URL, videoUrl);
        jumpToSdk(activity, bundle, "open_watch");
    }

    @Override
    public void openReplay(Activity activity, long playerId, String liveId, String videoUrl) {
        checkHasInit();

        Bundle bundle = getBasicBundle();
        bundle.putLong(EXTRA_PLAYER_ID, playerId);
        bundle.putString(EXTRA_LIVE_ID, liveId);
        bundle.putString(EXTRA_VIDEO_URL, videoUrl);
        jumpToSdk(activity, bundle, "open_replay");
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
    public void loginByMiAccountOAuth(String authCode) {
        checkHasInit();
        MiLiveSdkServiceProxy.getInstance().loginByMiAccountOAuth(authCode);
    }

    @Override
    public void loginByMiAccountSso(long miid, String serviceToken) {
        checkHasInit();
        MiLiveSdkServiceProxy.getInstance().loginByMiAccountSso(miid, serviceToken);
    }

    @Override
    public void clearAccount() {
        checkHasInit();
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

    /**
     * 只为测试使用，正常不需要
     */
    public void openRandomLive(@NonNull Activity activity) {
        checkHasInit();
        jumpToSdk(activity, getBasicBundle(), "test_random_live");
    }

    private void jumpToSdk(@NonNull Activity activity, @NonNull Bundle bundle, @NonNull String action) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName(VersionCheckManager.PACKAGE_NAME, VersionCheckManager.JUMP_CLASS_NAME);
        intent.putExtras(bundle);
        intent.setAction(action);

        if (!startActivity(activity, intent)) {
            if (mCallback != null) {
                mCallback.notifyNotInstall();
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

    // 跳转
    private boolean startActivity(Activity activity, Intent intent) {
        Logger.d(TAG, "start activity uri=" + intent.getDataString());
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
