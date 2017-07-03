package com.wali.live.watchsdk.scheme.gamecenter;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.wali.live.watchsdk.scheme.SchemeConstants;
import com.wali.live.watchsdk.scheme.SchemeUtils;
import com.wali.live.watchsdk.scheme.processor.SchemeProcessor;
import com.wali.live.watchsdk.watch.VideoDetailSdkActivity;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

/**
 * Created by lan on 2017/7/3.
 */
public class GamecenterProcessor {
    private static final String TAG = SchemeConstants.LOG_PREFIX + SchemeProcessor.class.getSimpleName();

    public static boolean process(@NonNull Uri uri, String host, @NonNull RxActivity activity, boolean finishActivity) {
        if (TextUtils.isEmpty(host)) {
            host = uri.getHost();
            if (TextUtils.isEmpty(host)) {
                MyLog.d(TAG, "process host is empty, uri=" + uri);
                return false;
            }
        }
        MyLog.d(TAG, "process host=" + host);
        switch (host) {
            case GamecenterConstants.HOST_OPEN_LIVE:
                processHostOpenLive(uri, activity);
                break;
            default:
                return false;
        }
        if (finishActivity) {
            activity.finish();
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

    private static void processHostOpenLive(Uri uri, Activity activity) {
        String liveId = uri.getQueryParameter(GamecenterConstants.PARAM_LIVE_ID);
        long playerId = SchemeUtils.getLong(uri, GamecenterConstants.PARAM_PLAYER_ID, 0);
        int isLive = SchemeUtils.getInt(uri, GamecenterConstants.PARAM_IS_LIVE, 1);
        String gameId = uri.getQueryParameter(GamecenterConstants.PARAM_GAME_ID);

        RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, null)
                .setGameId(gameId)
                .build();
        MyLog.w(TAG, "openLive isLive=" + isLive);
        if (isLive == GamecenterConstants.PARAM_TYPE_LIVE) {
            WatchSdkActivity.openActivity(activity, roomInfo);
        } else if (isLive == GamecenterConstants.PARAM_TYPE_PLAYBACK) {
            VideoDetailSdkActivity.openActivity(activity, roomInfo);
        }
    }
}
