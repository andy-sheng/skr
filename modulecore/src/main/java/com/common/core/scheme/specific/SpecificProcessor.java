package com.common.core.scheme.specific;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.core.RouterConstants;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.scheme.SchemeConstants;
import com.common.core.scheme.SchemeUtils;
import com.common.core.scheme.processor.CommonProcessor;
import com.common.log.MyLog;

/**
 * Created by lan on 16/10/27.
 *
 * @module scheme
 * @description 非Walilive/livesdk的Uri的逻辑代码
 */
public class SpecificProcessor extends CommonProcessor {
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

    protected static void processSchemeHttp(Uri uri, @NonNull Activity activity) {
//        Intent intent = new Intent(activity, WebViewActivity.class);
//        intent.putExtra(WebViewActivity.EXTRA_URL, uri.toString());
//        intent.putExtra(WebViewActivity.EXTRA_UID, MyUserInfoManager.getInstance().getUid());
//        intent.putExtra(WebViewActivity.EXTRA_AVATAR, MyUserInfoManager.getInstance().getAvatarTs());
//        BannerManger.BannerItem item = activity.getIntent().getParcelableExtra(SchemeConstants.EXTRA_BANNER_INFO);
//        if (item != null) {
//            intent.putExtra(SchemeConstants.EXTRA_BANNER_INFO, item);
//        }
//        activity.startActivity(intent);

        // todo 打开VideoDetailSdkActivity
        ARouter.getInstance().build(RouterConstants.ACTIVITY_VIDEO)
                .withString("liveId", uri.toString())
                .withLong("videoUrl", MyUserInfoManager.getInstance().getUid())
                .withLong("liveType", MyUserInfoManager.getInstance().getAvatarTs())
                .greenChannel().navigation();


    }

    private static void processSchemeGameCenter(Uri uri, @NonNull Activity activity) {
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

        MyLog.w(TAG, "openLive isLive=" + isLive);
        if (isLive == SpecificConstants.PARAM_TYPE_LIVE) {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_WATCH)
                    .withLong("playerId", playerId)
                    .withString("liveId", liveId)
                    .withString("gameId", gameId)
                    .greenChannel().navigation();
        } else if (isLive == SpecificConstants.PARAM_TYPE_PLAYBACK) {
            // todo 打开VideoDetailSdkActivity
            ARouter.getInstance().build(RouterConstants.ACTIVITY_VIDEO)
                    .withLong("playerId", playerId)
                    .withString("liveId", liveId)
                    .withString("gameId", gameId)
                    .greenChannel().navigation();
        }
    }
}
