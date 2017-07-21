package com.wali.live.watchsdk.scheme.specific;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.mi.live.data.account.MyUserInfoManager;
import com.wali.live.watchsdk.channel.util.BannerManger;
import com.wali.live.watchsdk.scheme.SchemeConstants;
import com.wali.live.watchsdk.scheme.SchemeUtils;
import com.wali.live.watchsdk.watch.VideoDetailSdkActivity;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;
import com.wali.live.watchsdk.webview.WebViewActivity;

/**
 * Created by lan on 16/10/27.
 *
 * @module scheme
 * @description 非Walilive的Uri的逻辑代码
 */
public class SpecificProcessor {
    private static final String TAG = SchemeConstants.LOG_PREFIX + SpecificProcessor.class.getSimpleName();

    public static boolean process(@NonNull Uri uri, String scheme, @NonNull Activity activity) {
        if (TextUtils.isEmpty(scheme)) {
            scheme = uri.getScheme();
            if (TextUtils.isEmpty(scheme)) {
                MyLog.d(TAG, "process scheme is empty, uri=" + uri);
                return false;
            }
        }
        MyLog.d(TAG, "process scheme=" + scheme);
        switch (scheme) {
            case SpecificConstants.SCHEME_HTTP:
            case SpecificConstants.SCHEME_HTTPS:
                processSchemeHttp(uri, activity);
                break;
            case SpecificConstants.SCHEME_GAMECENTER:
                processSchemeGameCenter(uri, activity);
                break;
            default:
                return false;
        }
        return true;
    }

    public static boolean isLegalPath(Uri uri, String logKey, @NonNull String comparePath) {
        MyLog.d(TAG, logKey + " uri=" + uri);
        if (uri == null) {
            return false;
        }
        String path = uri.getPath();
        return comparePath.equals(path);
    }

    /**
     * 跳转到WebView
     */
    public static void processSchemeHttp(Uri uri, @NonNull Activity activity) {
        Intent intent = new Intent(activity, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, uri.toString());
        intent.putExtra(WebViewActivity.EXTRA_UID, MyUserInfoManager.getInstance().getUuid());
        intent.putExtra(WebViewActivity.EXTRA_AVATAR, MyUserInfoManager.getInstance().getAvatar());
        BannerManger.BannerItem item = activity.getIntent().getParcelableExtra(SchemeConstants.EXTRA_BANNER_INFO);
        if (item != null) {
            intent.putExtra(SchemeConstants.EXTRA_BANNER_INFO, item);
        }
        activity.startActivity(intent);
    }

    public static void processSchemeGameCenter(Uri uri, @NonNull Activity activity) {
        String host = uri.getHost();
        if (SpecificConstants.HOST_OPEN_LIVE.equals(host)) {
            processHostOpenLive(uri, activity);
        }
    }

    private static void processHostOpenLive(Uri uri, Activity activity) {
        String liveId = uri.getQueryParameter(SpecificConstants.PARAM_LIVE_ID);
        long playerId = SchemeUtils.getLong(uri, SpecificConstants.PARAM_PLAYER_ID, 0);
        int isLive = SchemeUtils.getInt(uri, SpecificConstants.PARAM_IS_LIVE, 1);
        String gameId = uri.getQueryParameter(SpecificConstants.PARAM_GAME_ID);

        RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, null)
                .setGameId(gameId)
                .build();
        MyLog.w(TAG, "openLive isLive=" + isLive);
        if (isLive == SpecificConstants.PARAM_TYPE_LIVE) {
            WatchSdkActivity.openActivity(activity, roomInfo);
        } else if (isLive == SpecificConstants.PARAM_TYPE_PLAYBACK) {
            VideoDetailSdkActivity.openActivity(activity, roomInfo);
        }
    }
}
