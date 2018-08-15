package com.wali.live.sdk.manager;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.mi.live.data.location.Location;
import com.wali.live.sdk.manager.global.GlobalData;
import com.wali.live.sdk.manager.http.HttpUtils;
import com.wali.live.sdk.manager.http.SimpleRequest;
import com.wali.live.sdk.manager.log.Logger;
import com.wali.live.sdk.manager.utils.CommonUtils;
import com.wali.live.sdk.manager.utils.SchemeUtils;
import com.wali.live.sdk.manager.version.VersionCheckManager;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkServiceProxy;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Created by chengsimin on 2016/12/8.
 */
public class MiLiveSdkController implements IMiLiveSdk {
    public static final String TAG = MiLiveSdkController.class.getSimpleName();

    private static final String EXTRA_CHANNEL_ID = "extra_channel_id";
    private static final String EXTRA_PACKAGE_NAME = "extra_package_name";
    private static final String EXTRA_CHANNEL_SECRET = "extra_channel_secret";
    private static final String EXTRA_ENABLE_SHARE = "extra_enable_share";
    private static final String EXTRA_ENABLE_FOLLOW = "extra_enable_follow";

    private static final String EXTRA_PLAYER_ID = "extra_player_id";
    private static final String EXTRA_LIVE_ID = "extra_live_id";
    private static final String EXTRA_VIDEO_URL = "extra_video_url";
    private static final String EXTRA_LIVE_TYPE = "extra_live_type";
    private static final String EXTRA_GAME_ID = "extra_game_id";
    private static final String EXTRA_PAGE_CHANNEL_ID = "extra_page_channel_id";

    private static final String EXTRA_LOCATION = "extra_location";

    private static final String EXTRA_WATCH_ROOM = "extra_watch_room";
    private static final String EXTRA_WATCH_ROOM_LIST = "extra_watch_room_list";
    private static final String EXTRA_WATCH_ROOM_POSITION = "extra_watch_room_position";

    private static final String ACTION_LOGIN_OAUTH = "login_oauth";
    private static final String ACTION_LOGIN_SSO = "login_sso";
    private static final String ACTION_THIRD_PART_LOGIN = "third_part_login";
    private static final String ACTION_CLEAR_ACCOUNT = "clear_account";

    private static final String ACTION_EDIT_USER_INFO = "edit_user_info";
    private static final String ACTION_DO_FEED_BACK = "do_feed_back";

    private static final String ACTION_OPEN_WATCH = "open_watch";
    private static final String ACTION_OPEN_WATCH_ROOM = "open_watch_room";
    private static final String ACTION_OPEN_WATCH_ROOM_LIST = "open_watch_room_list";
    private static final String ACTION_OPEN_REPLAY = "open_replay";

    private static final String ACTION_OPEN_NORMAL_LIVE = "open_normal_live";
    private static final String ACTION_OPEN_GAME_LIVE = "open_game_live";

    private static final String ACTION_OPEN_CONTEST_PREPARE = "open_contest_prepare";
    private static final String ACTION_OPEN_CONTEST_WATCH = "open_contest_watch";
    private static final String ACTION_OPEN_WITHDRAW = "open_withdraw";

    private static final String ACTION_GET_CHANNEL_LIVES = "get_channel_lives";
    private static final String ACTION_GET_FOLLOWING_USERS = "get_following_users";
    private static final String ACTION_NOTIFY_SHARE_SUC = "notify_share_suc";
    private static final String ACTION_GET_FOLLOWING_LIVES = "get_following_lives";

    private static final String ACTION_DISABLE_RELATION_CHAIN = "disable_relation_chain";

    private static final String ACTION_OP_GET_BARRAGE = "get_barrage";

    private static final String ACTION_OPEN_CHANNEL_LIST = "open_channel_list";
    ;
    private static final String ACTION_STATISTIC = "statistic";

    /*SharedPreferences File & Key*/
    private static final String PREF_FILE_NAME = "liveassistant_upgrade";
    private static final String PREF_FORCE_CHECK_TIME = "pref_force_check_time";

    private static final long ONE_DAY = 24 * 60 * 60 * 1000;

    private static final MiLiveSdkController sSdkController = new MiLiveSdkController();

    private ExecutorService mExecutor = HttpUtils.ONLINE_FILE_TASK_EXECUTOR;
    private int mRemoteVersion;
    private boolean mForceUpdate;

    private Map<String, Integer> mMinVersionMap = new HashMap();
    private int mApkVersion;

    private int mChannelId = 0;
    private String mChannelSecret;

    private boolean mEnableShare;
    /**
     * 是否隐藏关系链，无法关注，无法私信
     */
    private boolean mEnableRelationChain = true;

    private ICallback mCallback;

    private MiLiveSdkController() {
        mMinVersionMap.put(ACTION_OPEN_WATCH, 204000);
        mMinVersionMap.put(ACTION_OPEN_REPLAY, 204000);

        mMinVersionMap.put(ACTION_LOGIN_OAUTH, 204000);
        mMinVersionMap.put(ACTION_LOGIN_SSO, 204000);
        mMinVersionMap.put(ACTION_CLEAR_ACCOUNT, 204000);

        mMinVersionMap.put(ACTION_OPEN_NORMAL_LIVE, 205001);
        mMinVersionMap.put(ACTION_OPEN_GAME_LIVE, 205001);

        mMinVersionMap.put(ACTION_THIRD_PART_LOGIN, 205005);
        mMinVersionMap.put(ACTION_GET_CHANNEL_LIVES, 205008);
        mMinVersionMap.put(ACTION_GET_FOLLOWING_USERS, 205011);
        mMinVersionMap.put(ACTION_NOTIFY_SHARE_SUC, 205014);
        mMinVersionMap.put(ACTION_GET_FOLLOWING_LIVES, 205017);

        mMinVersionMap.put(ACTION_STATISTIC, 205025);

        mMinVersionMap.put(ACTION_OPEN_WATCH_ROOM, 205037);
        mMinVersionMap.put(ACTION_OPEN_WATCH_ROOM_LIST, 205037);

        mMinVersionMap.put(ACTION_OPEN_CONTEST_PREPARE, 205055);
        mMinVersionMap.put(ACTION_OPEN_CONTEST_WATCH, 205055);

        mMinVersionMap.put(ACTION_OPEN_WITHDRAW, 205057);
        mMinVersionMap.put(ACTION_EDIT_USER_INFO, 205057);

        mMinVersionMap.put(ACTION_DO_FEED_BACK, 205058);
        mMinVersionMap.put(ACTION_DISABLE_RELATION_CHAIN, 205061);
        mMinVersionMap.put(ACTION_OP_GET_BARRAGE, 206003);
        mMinVersionMap.put(ACTION_OPEN_CHANNEL_LIST, 430020);
    }

    public static IMiLiveSdk getInstance() {
        return sSdkController;
    }

    public void init(Application app, int channelId, String channelSecret, boolean isCta, ICallback callback) {
        GlobalData.setApplication(app);
        Logger.d(TAG, "init channelId=" + channelId);
        mChannelId = channelId;
        mChannelSecret = channelSecret;
        mCallback = callback;

        getApkVersion();
        // 如果网络通过，就检查升级
        if (isCta) {
            checkForceUpdate();
        }

        MiLiveSdkServiceProxy.getInstance().setCallback(mCallback);
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
        MiLiveSdkServiceProxy.getInstance().initService();
    }

    private void checkForceUpdate() {
        long last = getForceCheckTime();
        long delta = System.currentTimeMillis() - last;
        if (delta > 0 && delta < ONE_DAY) {
            Logger.d(TAG, "force update has check today, last=" + last + ", delta=" + delta);
            return;
        }
        if (VersionCheckManager.getInstance().isUpgrading()) {
            Logger.d(TAG, "update is upgrading");
            return;
        }
        Logger.d(TAG, "force update is executing");
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                checkUpdateFromServer();
            }
        });
    }

    private void checkUpdateFromServer() {
        // 默认不需要强制升级
        mForceUpdate = false;

        SimpleRequest.StringContent result = VersionCheckManager.getInstance().getStringContent();
        if (result == null) {
            Logger.d(TAG, "updateResult is null");
            return;
        }
        String jsonString = result.getBody();
        if (TextUtils.isEmpty(jsonString)) {
            Logger.d(TAG, "updateResult body is empty");
            return;
        }
        try {
            JSONObject resultObj = new JSONObject(jsonString);
            if (!resultObj.has("result") || !"ok".equalsIgnoreCase(resultObj.getString("result"))) {
                Logger.d(TAG, "updateResult content is illegal");
                return;
            }
            Logger.w(TAG, "updateResult=" + resultObj.toString());
            JSONObject dataObj = resultObj.getJSONObject("data");
            boolean shouldUpdate = dataObj.getBoolean("newUpdate");
            if (!shouldUpdate) {
                // 只有获取到数据，认为不需要强制升级，才写入checkTime
                saveForceCheckTime();
                return;
            }
            mRemoteVersion = dataObj.getInt("toVersion");
            JSONObject custom = dataObj.optJSONObject("custom");
            if (custom != null) {
                mForceUpdate = custom.optBoolean("forced", false);
                // 只有获取到数据，认为不需要强制升级，才写入checkTime
                if (!mForceUpdate) {
                    saveForceCheckTime();
                }
            }
        } catch (JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
    }

    private void saveForceCheckTime() {
        SharedPreferences pref = GlobalData.app().getApplicationContext()
                .getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = pref.edit();
        ed.putLong(PREF_FORCE_CHECK_TIME, System.currentTimeMillis());
        ed.apply();
    }

    private long getForceCheckTime() {
        SharedPreferences pref = GlobalData.app().getApplicationContext()
                .getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        return pref.getLong(PREF_FORCE_CHECK_TIME, 0);
    }

    private void getApkVersion() {
        try {
            PackageInfo packageInfo = GlobalData.app().getPackageManager().getPackageInfo(
                    VersionCheckManager.PACKAGE_NAME, PackageManager.GET_META_DATA);
            mApkVersion = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, e.getMessage());
        }
        Logger.d(TAG, "getApkVersion versionCode=" + mApkVersion);
        // 如果版本为0，置空服务，防止下次apk重装出问题
        if (mApkVersion == 0) {
            MiLiveSdkServiceProxy.getInstance().clearService();
        }
    }

    private boolean checkVersion(String action, IAssistantCallback callback) {
        if (!mMinVersionMap.containsKey(action)) {
            Logger.d(TAG, "version action not exist=" + action);
            return false;
        }
        int version = mMinVersionMap.get(action);
        // 版本校验失败再重新获取下版本
        if (version > mApkVersion) {
            Logger.d(TAG, "checkVersion smaller");
            getApkVersion();
        }
        // 如果版本为0，通知未安装
        if (mApkVersion == 0) {
            Logger.d(TAG, "checkVersion zero");
            callback.notifyNotInstall();
            return false;
        }
        // 如果接口要求版本大于当前版本，通知版本过低
        if (version > mApkVersion) {
            Logger.d(TAG, "checkVersion smaller again");
            callback.notifyVersionLow();
            return false;
        }
        // 强制升级，如果当前版本小于远端强制升级版本，通知版本过低，否则重新检测下是否需要强制升级
        if (mForceUpdate) {
            Logger.d(TAG, "checkVersion forceUpdate");
            if (mApkVersion < mRemoteVersion) {
                Logger.d(TAG, "checkVersion smaller tripple");
                callback.notifyVersionLow();
                return false;
            }
            Logger.d(TAG, "checkVersion forceUpdate again");
            checkForceUpdate();
        }
        return true;
    }

    @Override
    public void setChannelId(int channelId) {
        mChannelId = channelId;
    }

    @Override
    public void startBarragePull(String roomId, int[] msgType, IGetBarrageCallback callback) {
        Logger.d(TAG, "beginBarragePull roomId:" + roomId + " msgtype:" + msgType);
        if (!checkVersion(ACTION_OP_GET_BARRAGE, callback)) {
            return;
        }
        checkHasInit();
        MiLiveSdkServiceProxy.getInstance().startBarragePull(roomId, msgType, callback);
    }

    @Override
    public void stopBarragePull(String roomId, IAssistantCallback callback) {
        Logger.d(TAG, "stopBarragePull" + " roomId=" + roomId);
        if (!checkVersion(ACTION_OP_GET_BARRAGE, callback)) {
            return;
        }
        checkHasInit();
        MiLiveSdkServiceProxy.getInstance().stopBarragePull(roomId);
    }

    @Override
    public void getLiveUid(IAssistantCallback callback) {
//        if (!checkVersion(ACTION_OP_GET_BARRAGE, callback)) {
//            return;
//        }
        checkHasInit();
        MiLiveSdkServiceProxy.getInstance().getLiveUid();
    }

    @Override
    public boolean openChannelList(Activity activity, IAssistantCallback callback) {
        Logger.d(TAG, "openChannelList");
        if (!checkVersion(ACTION_OPEN_CHANNEL_LIST, callback)) {
            return false;
        }
        checkHasInit();

        Bundle bundle = getBasicBundle();
        jumpToSdk(activity, bundle, ACTION_OPEN_CHANNEL_LIST, callback);
        return false;
    }

    @Override
    public void enableShare(boolean enable) {
        mEnableShare = enable;
    }

    @Override
    public boolean enableRelationChain(boolean enable, IAssistantCallback callback) {
        if (!enable) {
            if (!checkVersion(ACTION_DISABLE_RELATION_CHAIN, callback)) {
                return false;
            }
        }
        mEnableRelationChain = enable;
        return true;
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
    public void loginByMiAccountOAuth(String authCode, IAssistantCallback callback) {
        if (!checkVersion(ACTION_LOGIN_OAUTH, callback)) {
            return;
        }
        checkHasInit();
        MiLiveSdkServiceProxy.getInstance().loginByMiAccountOAuth(authCode);
    }

    @Override
    public void loginByMiAccountSso(long miid, String serviceToken, IAssistantCallback callback) {
        if (!checkVersion(ACTION_LOGIN_SSO, callback)) {
            return;
        }
        checkHasInit();
        MiLiveSdkServiceProxy.getInstance().loginByMiAccountSso(miid, serviceToken);
    }

    @Override
    public void thirdPartLogin(int channelId, String xuid, int sex, String nickname, String headUrl, String sign, IAssistantCallback callback) {
        if (!checkVersion(ACTION_THIRD_PART_LOGIN, callback)) {
            return;
        }
        checkHasInit();
        MiLiveSdkServiceProxy.getInstance().thirdPartLogin(channelId, xuid, sex, nickname, headUrl, sign);
    }

    @Override
    public void editUserInfo(int channelId, String xuid, int sex, String nickname, String headUrl, IAssistantCallback callback) {
        if (!checkVersion(ACTION_EDIT_USER_INFO, callback)) {
            return;
        }
        checkHasInit();
        MiLiveSdkServiceProxy.getInstance().editUserInfo(channelId, xuid, sex, nickname, headUrl);
    }

    @Override
    public void doFeedBack(IAssistantCallback callback) {
        if (!checkVersion(ACTION_DO_FEED_BACK, callback)) {
            return;
        }
        checkHasInit();
        MiLiveSdkServiceProxy.getInstance().doFeedBack();
    }

    @Override
    public void clearAccount(IAssistantCallback callback) {
        if (!checkVersion(ACTION_CLEAR_ACCOUNT, callback)) {
            return;
        }
        checkHasInit();
        MiLiveSdkServiceProxy.getInstance().clearAccount();
    }

    /**
     * migamecenter://room/join?liveid=4431273_1527670977&playerid=4431273
     * &videourl=http%3A%2F%2Fv2.zb.mi.com%2Flive%2F4431273_1527670977.flv%3Fplayui%3D0&type=8&recommend=r-0-0-4431273-4431273_1527670977-2001-8240-1527685008-1-1-1-0-1-56
     *
     * @param schema
     * @return
     */
    @Override
    public boolean tryJumpBySchema(Activity activity, String schema, IAssistantCallback callback) {
        return tryJumpBySchema(activity, schema, 0, callback);
    }

    @Override
    public boolean tryJumpBySchema(Activity activity, String schema, long pageChannelId, IAssistantCallback callback) {
        //尝试使用schema跳转
        Uri uri = Uri.parse(schema);
        String host = uri.getHost();
        String path = uri.getPath();
        if ("room".equals(host)) {
            if ("/join".equals(path)) {
                String liveId = SchemeUtils.getString(uri, "liveid");
                long playerId = SchemeUtils.getLong(uri, "playerid", 0);
                String videoUrl = SchemeUtils.getString(uri, "videourl");
                int liveType = SchemeUtils.getInt(uri, "type", 0);
                int gameId = SchemeUtils.getInt(uri, "gameid", 0);
                if (!TextUtils.isEmpty(liveId) && !TextUtils.isEmpty(videoUrl)) {
                    if (gameId > 0) {
                        RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, videoUrl)
                                .setGameId(String.valueOf(gameId))
                                .build();
                        openWatchRoom(activity, roomInfo, callback);
                    } else {
                        if (pageChannelId > 0) {
                            openWatch(activity, playerId, liveId, videoUrl, liveType, pageChannelId, callback);
                        } else {
                            openWatch(activity, playerId, liveId, videoUrl, liveType, callback);
                        }
                    }
                    return true;
                }
            }
        } else if ("playback".equals(host)) {
            if ("/join".equals(path)) {
                String liveId = SchemeUtils.getString(uri, "liveid");
                long playerId = SchemeUtils.getLong(uri, "playerid", 0);
                String videoUrl = SchemeUtils.getString(uri, "videourl");
                int liveType = SchemeUtils.getInt(uri, "type", 0);
                if (!TextUtils.isEmpty(liveId) && !TextUtils.isEmpty(videoUrl)) {
                    openReplay(activity, playerId, liveId, videoUrl, liveType, callback);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void openWatch(Activity activity, long playerId, String liveId, String videoUrl, int liveType, IAssistantCallback callback) {
        if (!checkVersion(ACTION_OPEN_WATCH, callback)) {
            return;
        }
        checkHasInit();

        Bundle bundle = getBasicBundle();
        bundle.putLong(EXTRA_PLAYER_ID, playerId);
        bundle.putString(EXTRA_LIVE_ID, liveId);
        bundle.putString(EXTRA_VIDEO_URL, videoUrl);
        bundle.putInt(EXTRA_LIVE_TYPE, liveType);
        jumpToSdk(activity, bundle, ACTION_OPEN_WATCH, callback);
    }


    @Override
    public void openWatch(Activity activity, long playerId, String liveId, String videoUrl, int liveType, long pageChannelId, IAssistantCallback callback) {
        if (!checkVersion(ACTION_OPEN_WATCH, callback)) {
            return;
        }
        checkHasInit();

        Bundle bundle = getBasicBundle();
        bundle.putLong(EXTRA_PLAYER_ID, playerId);
        bundle.putString(EXTRA_LIVE_ID, liveId);
        bundle.putString(EXTRA_VIDEO_URL, videoUrl);
        bundle.putInt(EXTRA_LIVE_TYPE, liveType);
        bundle.putLong(EXTRA_PAGE_CHANNEL_ID, pageChannelId);
        jumpToSdk(activity, bundle, ACTION_OPEN_WATCH, callback);
    }

    @Override
    public void openReplay(Activity activity, long playerId, String liveId, String videoUrl, int liveType, IAssistantCallback callback) {
        if (!checkVersion(ACTION_OPEN_REPLAY, callback)) {
            return;
        }
        checkHasInit();

        Bundle bundle = getBasicBundle();
        bundle.putLong(EXTRA_PLAYER_ID, playerId);
        bundle.putString(EXTRA_LIVE_ID, liveId);
        bundle.putString(EXTRA_VIDEO_URL, videoUrl);
        bundle.putInt(EXTRA_LIVE_TYPE, liveType);
        jumpToSdk(activity, bundle, ACTION_OPEN_REPLAY, callback);
    }

    @Override
    public void openWatch(Activity activity, long playerId, String liveId, String videoUrl, int liveType, String gameId, IAssistantCallback callback) {
        if (!checkVersion(ACTION_OPEN_WATCH, callback)) {
            return;
        }
        checkHasInit();

        Bundle bundle = getBasicBundle();
        bundle.putLong(EXTRA_PLAYER_ID, playerId);
        bundle.putString(EXTRA_LIVE_ID, liveId);
        bundle.putString(EXTRA_VIDEO_URL, videoUrl);
        bundle.putInt(EXTRA_LIVE_TYPE, liveType);
        bundle.putString(EXTRA_GAME_ID, gameId);
        jumpToSdk(activity, bundle, ACTION_OPEN_WATCH, callback);
    }

    @Override
    public void openWatchRoom(Activity activity, RoomInfo roomInfo, IAssistantCallback callback) {
        if (!checkVersion(ACTION_OPEN_WATCH_ROOM, callback)) {
            return;
        }
        checkHasInit();

        Bundle bundle = getBasicBundle();
        bundle.putParcelable(EXTRA_WATCH_ROOM, roomInfo);
        jumpToSdk(activity, bundle, ACTION_OPEN_WATCH_ROOM, callback);
    }

    @Override
    public void openWatchRoomList(Activity activity, ArrayList<RoomInfo> list, int position, IAssistantCallback callback) {
        if (!checkVersion(ACTION_OPEN_WATCH_ROOM_LIST, callback)) {
            return;
        }
        checkHasInit();

        Bundle bundle = getBasicBundle();
        bundle.putParcelableArrayList(EXTRA_WATCH_ROOM_LIST, list);
        bundle.putInt(EXTRA_WATCH_ROOM_POSITION, position);
        jumpToSdk(activity, bundle, ACTION_OPEN_WATCH_ROOM_LIST, callback);
    }

    @Override
    public void openReplay(Activity activity, long playerId, String liveId, String videoUrl, int liveType, String gameId, IAssistantCallback callback) {
        if (!checkVersion(ACTION_OPEN_REPLAY, callback)) {
            return;
        }
        checkHasInit();

        Bundle bundle = getBasicBundle();
        bundle.putLong(EXTRA_PLAYER_ID, playerId);
        bundle.putString(EXTRA_LIVE_ID, liveId);
        bundle.putString(EXTRA_VIDEO_URL, videoUrl);
        bundle.putInt(EXTRA_LIVE_TYPE, liveType);
        bundle.putString(EXTRA_GAME_ID, gameId);
        jumpToSdk(activity, bundle, ACTION_OPEN_REPLAY, callback);
    }

    @Override
    public void openNormalLive(Activity activity, Location location, IAssistantCallback callback) {
        if (!checkVersion(ACTION_OPEN_NORMAL_LIVE, callback)) {
            return;
        }
        checkHasInit();

        Bundle bundle = getBasicBundle();
        if (location != null) {
            bundle.putParcelable(EXTRA_LOCATION, location);
        }
        jumpToSdk(activity, bundle, ACTION_OPEN_NORMAL_LIVE, callback);
    }

    @Override
    public void openGameLive(Activity activity, Location location, IAssistantCallback callback) {
        if (!checkVersion(ACTION_OPEN_GAME_LIVE, callback)) {
            return;
        }
        checkHasInit();

        Bundle bundle = getBasicBundle();
        if (location != null) {
            bundle.putParcelable(EXTRA_LOCATION, location);
        }
        jumpToSdk(activity, bundle, ACTION_OPEN_GAME_LIVE, callback);
    }

    @Override
    public void openContestPrepare(Activity activity, IAssistantCallback callback) {
        if (!checkVersion(ACTION_OPEN_CONTEST_PREPARE, callback)) {
            return;
        }
        checkHasInit();

        Bundle bundle = getBasicBundle();
        jumpToSdk(activity, bundle, ACTION_OPEN_CONTEST_PREPARE, callback);
    }

    @Override
    public void openContestWatch(Activity activity, long playerId, String liveId, String videoUrl, IAssistantCallback callback) {
        if (!checkVersion(ACTION_OPEN_CONTEST_WATCH, callback)) {
            return;
        }
        checkHasInit();

        Bundle bundle = getBasicBundle();
        bundle.putLong(EXTRA_PLAYER_ID, playerId);
        bundle.putString(EXTRA_LIVE_ID, liveId);
        bundle.putString(EXTRA_VIDEO_URL, videoUrl);
        jumpToSdk(activity, bundle, ACTION_OPEN_CONTEST_WATCH, callback);
    }

    @Override
    public void openContestWithdrawals(Activity activity, IAssistantCallback callback) {
        if (!checkVersion(ACTION_OPEN_WITHDRAW, callback)) {
            return;
        }
        checkHasInit();

        Bundle bundle = getBasicBundle();
        jumpToSdk(activity, bundle, ACTION_OPEN_WITHDRAW, callback);
    }

    @Override
    public void getChannelLives(IChannelAssistantCallback callback) {
        if (!checkVersion(ACTION_GET_CHANNEL_LIVES, callback)) {
            return;
        }
        MiLiveSdkServiceProxy.getInstance().getChannelLives(callback);
    }

    @Override
    public void getFollowingUserList(boolean isBothWay, long timeStamp, IFollowingUsersCallback callback) {
        if (!checkVersion(ACTION_GET_FOLLOWING_USERS, callback)) {
            return;
        }
        MiLiveSdkServiceProxy.getInstance().getFollowingUsers(isBothWay, timeStamp, callback);
    }

    @Override
    public void notifyShare(boolean success, int type, IAssistantCallback callback) {
        if (!checkVersion(ACTION_NOTIFY_SHARE_SUC, callback)) {
            return;
        }
        MiLiveSdkServiceProxy.getInstance().notifyShare(success, type);
    }

    @Override
    public void getFollowingLiveList(IFollowingLivesCallback callback) {
        if (!checkVersion(ACTION_GET_FOLLOWING_LIVES, callback)) {
            return;
        }
        MiLiveSdkServiceProxy.getInstance().getFollowingLives(callback);
    }

    @Override
    public void statistic(String key, long time, IAssistantCallback callback) {
        if (!checkVersion(ACTION_STATISTIC, callback)) {
            return;
        }
        MiLiveSdkServiceProxy.getInstance().statistic(key, time);
    }

    @Override
    public boolean hasInstallLiveSdk(Application application) {
        if (application == null) {
            application = GlobalData.app();
        }
        if (application == null) {
            Logger.e(TAG, "hasInstallLiveSdk application is null");
            return false;
        }
        PackageInfo pInfo = null;
        try {
            pInfo = application.getPackageManager().getPackageInfo(
                    VersionCheckManager.PACKAGE_NAME, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, e.getMessage());
        }
        return pInfo != null;
    }

    private void jumpToSdk(@NonNull Activity activity, @NonNull Bundle bundle, @NonNull String action, IAssistantCallback callback) {
        if (CommonUtils.isFastDoubleClick()) {
            Logger.d(TAG, "jumpToSdk fast double click, action=" + action);
            return;
        }
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

        // check service alive
        MiLiveSdkServiceProxy.getInstance().checkService();
    }

    private Bundle getBasicBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_CHANNEL_ID, mChannelId);
        bundle.putString(EXTRA_PACKAGE_NAME, GlobalData.app().getPackageName());
        bundle.putString(EXTRA_CHANNEL_SECRET, mChannelSecret);
        if (mEnableShare) {
            bundle.putBoolean(EXTRA_ENABLE_SHARE, mEnableShare);
        }
        if (!mEnableRelationChain) {
            bundle.putBoolean(EXTRA_ENABLE_FOLLOW, mEnableRelationChain);
        }
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
