package com.wali.live.watchsdk.scheme.processor;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.wali.live.watchsdk.channel.ChannelSdkActivity;
import com.wali.live.watchsdk.scheme.SchemeConstants;
import com.wali.live.watchsdk.scheme.SchemeUtils;
import com.wali.live.watchsdk.watch.VideoDetailSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

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

        RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, videoUrl)
                .setLiveType(liveType)
                .build();
        VideoDetailSdkActivity.openActivity(activity, roomInfo);
    }

    /**
     * 跳转到频道页面
     */
    protected static void processHostChannel(Uri uri, Activity activity) {
        long channelId = SchemeUtils.getLong(uri, SchemeConstants.PARAM_CHANNEL_ID, 0);
        MyLog.w(TAG, "channel id=" + channelId);
        if (channelId != 0) {
            ChannelSdkActivity.openActivity(activity, channelId);
        }
    }
}
