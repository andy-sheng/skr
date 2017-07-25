package com.wali.live.watchsdk.scheme.processor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.mi.live.data.api.LiveManager;
import com.wali.live.event.EventClass;
import com.wali.live.watchsdk.channel.ChannelSdkActivity;
import com.wali.live.watchsdk.channel.sublist.activity.SubChannelActivity;
import com.wali.live.watchsdk.scheme.SchemeConstants;
import com.wali.live.watchsdk.scheme.SchemeUtils;
import com.wali.live.watchsdk.watch.VideoDetailSdkActivity;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;
import com.wali.live.watchsdk.webview.HalfWebViewActivity;
import com.wali.live.watchsdk.webview.WebViewActivity;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by lan on 16/10/26.
 *
 * @module scheme
 * @description Walilive的Uri的逻辑代码
 */
public class WaliliveProcessor {
    private static final String TAG = SchemeConstants.LOG_PREFIX + WaliliveProcessor.class.getSimpleName();

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
            case SchemeConstants.HOST_OPEN_URL:
                processHostOpenUrl(uri, activity);
                break;
            case SchemeConstants.HOST_FEED:
                processHostFeed(uri, activity);
                break;
            case SchemeConstants.HOST_ROOM:
                processHostRoom(uri, activity);
                break;
            case SchemeConstants.HOST_PLAYBACK:
                processHostPlayback(uri, activity);
                break;
            case SchemeConstants.HOST_CHANNEL:
                processHostChannel(uri, activity);
                break;
            case SchemeConstants.HOST_RECOMMEND:
                if (isLegalPath(uri, "processHostSubList", SchemeConstants.PATH_SUB_LIST)) {
                    processHostSubList(uri, activity);
                } else {
                    //小视频二级页暂时不考虑
                    return false;
                }
                break;
            case SchemeConstants.HOST_UNLOGIN_H5:
                EventBus.getDefault().post(new EventClass.H5UnloginEvent());
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

    public static void processHostOpenUrl(Uri uri, @NonNull Activity activity) {
        if (!isLegalPath(uri, "processHostOpenUrl", SchemeConstants.PATH_NEW_WINDOW)) {
            return;
        }

        String url = Uri.decode(uri.getQueryParameter(SchemeConstants.PARAM_WEBVIEW_RUL));
        boolean isHalf = uri.getBooleanQueryParameter(SchemeConstants.PARAM_WEBVIEW_ISHALF, false);

        Intent intent = new Intent(activity, isHalf ? HalfWebViewActivity.class : WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTRA_URL, url);
        intent.putExtra(WebViewActivity.EXTRA_DISPLAY_TYPE, isHalf);
        activity.startActivity(intent);
    }

    public static void processHostFeed(Uri uri, @NonNull Activity activity) {
        if (!isLegalPath(uri, "processHostFeed", SchemeConstants.PATH_NEWS_INFO)) {
            return;
        }

        String feedId = uri.getQueryParameter(SchemeConstants.PARAM_FEED_ID);
        long ownerId = SchemeUtils.getLong(uri, SchemeConstants.PARAM_OWENER_ID, 0);
        int feedsType = SchemeUtils.getInt(uri, SchemeConstants.PARAM_FEEDS_TYPE, 0);

        //这里拿掉区分type的
        VideoDetailSdkActivity.openActivity(activity, RoomInfo.Builder.newInstance(ownerId, feedId, "").build());
    }

    private static void processHostRoom(Uri uri, Activity activity) {
        if (!isLegalPath(uri, "processHostRoom", SchemeConstants.PATH_JOIN)) {
            return;
        }

        String liveId = uri.getQueryParameter(SchemeConstants.PARAM_LIVE_ID);
        long playerId = SchemeUtils.getLong(uri, SchemeConstants.PARAM_PLAYER_ID, 0);
        String videoUrl = uri.getQueryParameter(SchemeConstants.PARAM_VIDEO_URL);
        int type = SchemeUtils.getInt(uri, SchemeConstants.PARAM_TYPE, 0);
        int liveType = LiveManager.mapLiveTypeFromListToRoom(type);
        RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, videoUrl)
                .setLiveType(liveType)
                .build();
        WatchSdkActivity.openActivity(activity, roomInfo);
    }

    private static void processHostPlayback(Uri uri, Activity activity) {
        if (!isLegalPath(uri, "processHostPlayback", SchemeConstants.PATH_JOIN)) {
            return;
        }

        long playerId = SchemeUtils.getLong(uri, SchemeConstants.PARAM_PLAYER_ID, 0);
        String liveId = uri.getQueryParameter(SchemeConstants.PARAM_LIVE_ID);
        String videoUrl = Uri.decode(uri.getQueryParameter(SchemeConstants.PARAM_VIDEO_URL));
        int liveType = SchemeUtils.getInt(uri, SchemeConstants.PARAM_TYPE, 0);

        RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, videoUrl)
                .setLiveType(liveType)
                .build();
        VideoDetailSdkActivity.openActivity(activity, roomInfo);
    }

    /**
     * 跳转到频道页面
     */
    public static void processHostChannel(Uri uri, Activity activity) {
        long channelId = SchemeUtils.getLong(uri, SchemeConstants.PARAM_CHANNEL_ID, 0);
        MyLog.w(TAG, "channel id=" + channelId);
        if (channelId != 0) {
            ChannelSdkActivity.openActivity(activity, channelId);
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

        SubChannelActivity.openActivity(activity, id, title, channelId, key, keyId, animation, source, select);
    }
}
