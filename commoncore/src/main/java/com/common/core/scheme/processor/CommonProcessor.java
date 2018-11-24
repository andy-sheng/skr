package com.common.core.scheme.processor;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.alibaba.android.arouter.launcher.ARouter;
import com.module.RouterConstants;
import com.common.core.scheme.SchemeConstants;
import com.common.core.scheme.SchemeUtils;
import com.common.log.MyLog;

/**
 * Created by lan on 16/10/26.
 *
 * @module scheme
 * @description LiveSdk的Uri的逻辑代码
 */
public class CommonProcessor {
    private static final String TAG = SchemeConstants.LOG_PREFIX + CommonProcessor.class.getSimpleName();

    protected static boolean isLegalPath(Uri uri, String logKey, @NonNull String comparePath) {
        MyLog.d(TAG, logKey + " uri=" + uri);
        if (uri == null) {
            return false;
        }
        String path = uri.getPath();
        return comparePath.equals(path);
    }

    protected static void processHostPlayback(Uri uri, Activity activity) {
        if (!isLegalPath(uri, "processHostPlayback", SchemeConstants.PATH_JOIN)) {
            return;
        }

        long playerId = SchemeUtils.getLong(uri, SchemeConstants.PARAM_PLAYER_ID, 0);
        String liveId = uri.getQueryParameter(SchemeConstants.PARAM_LIVE_ID);
        String videoUrl = Uri.decode(uri.getQueryParameter(SchemeConstants.PARAM_VIDEO_URL));
        int liveType = SchemeUtils.getInt(uri, SchemeConstants.PARAM_TYPE, 0);

        // todo 打开VideoDetailSdkActivity
        ARouter.getInstance().build(RouterConstants.ACTIVITY_VIDEO)
                .withLong("playerId", playerId)
                .withString("liveId", liveId)
                .withString("videoUrl", videoUrl)
                .withInt("liveType", liveType)
                .greenChannel().navigation();
    }

    /**
     * 跳转到频道页面
     */
    protected static void processHostChannel(Uri uri, Activity activity) {
        long channelId = SchemeUtils.getLong(uri, SchemeConstants.PARAM_CHANNEL_ID, 0);
        MyLog.w(TAG, "channel id=" + channelId);
        if (channelId != 0) {
            String title = Uri.decode(uri.getQueryParameter(SchemeConstants.PARAM_LIST_TITLE));
            // todo 打开ChannelSdkActivity
            ARouter.getInstance().build(RouterConstants.ACTIVITY_CHANNEL_LIST_SDK)
                    .withLong("channelId", channelId)
                    .withString("title", title)
                    .greenChannel().navigation();
        }
    }

    /**
     * 跳转到频道二级页面
     */
    public static void processHostSubList(Uri uri, @NonNull Activity activity) {
        int id = Integer.valueOf(uri.getQueryParameter(SchemeConstants.PARAM_LIST_ID));
        String title = Uri.decode(uri.getQueryParameter(SchemeConstants.PARAM_LIST_TITLE));
        int channelId = SchemeUtils.getInt(uri, SchemeConstants.PARAM_LIST_CHANNEL_ID, 0);

        String key = uri.getQueryParameter(SchemeConstants.PARAM_LIST_KEY);

        int keyId = SchemeUtils.getInt(uri, SchemeConstants.PARAM_LIST_KEY_ID, 0);
        int animation = SchemeUtils.getInt(uri, SchemeConstants.PARAM_LIST_ANIMATION, 0);
        int source = SchemeUtils.getInt(uri, SchemeConstants.PARAM_LIST_SOURCE, 0);
        int select = SchemeUtils.getInt(uri, SchemeConstants.PARAM_SELECT, 0);

        // todo 打开SubChannelActivity
        ARouter.getInstance().build(RouterConstants.ACTIVITY_SUB_CHANNEL)
                .withInt("id", id)
                .withString("title", title)
                .withInt("channelId", channelId)
                .withString("key", key)
                .withInt("keyId", keyId)
                .withInt("animation", animation)
                .withInt("source", source)
                .withInt("select", select)
                .greenChannel().navigation();
    }

    public static void processHostOpenUrl(Uri uri, @NonNull Activity activity) {
        if (!isLegalPath(uri, "processHostOpenUrl", SchemeConstants.PATH_NEW_WINDOW)) {
            return;
        }

        String url = Uri.decode(uri.getQueryParameter(SchemeConstants.PARAM_WEBVIEW_RUL));
        boolean isHalf = uri.getBooleanQueryParameter(SchemeConstants.PARAM_WEBVIEW_ISHALF, false);

        // todo 打开 HalfWebViewActivity 或 WebViewActivity
        ARouter.getInstance().build(isHalf ? RouterConstants.ACTIVITY_HALFWEB : RouterConstants.ACTIVITY_WEB)
                .withObject("url", url)
                .withBoolean("isHalf", isHalf)
                .greenChannel().navigation();
    }
}
