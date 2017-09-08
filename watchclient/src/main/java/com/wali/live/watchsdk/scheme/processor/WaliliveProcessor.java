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
import com.wali.live.pay.activity.RechargeActivity;
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
public class WaliliveProcessor extends CommonProcessor {
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
            case SchemeConstants.HOST_RECHARGE:
                jumpToRechargeActivity(activity);
                break;
            default:
                return false;
        }
        if (finishActivity) {
            activity.finish();
        }
        return true;
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

    /**
     * 不使用CommonProcessor，因为walilive的room协议更复杂，这里单独处理
     */
    protected static void processHostRoom(Uri uri, Activity activity) {
        if (!isLegalPath(uri, "processHostRoom", SchemeConstants.PATH_JOIN)) {
            return;
        }

        String liveId = uri.getQueryParameter(SchemeConstants.PARAM_LIVE_ID);
        long playerId = SchemeUtils.getLong(uri, SchemeConstants.PARAM_PLAYER_ID, 0);
        String videoUrl = uri.getQueryParameter(SchemeConstants.PARAM_VIDEO_URL);
        int type = SchemeUtils.getInt(uri, SchemeConstants.PARAM_TYPE, 0);
        int liveType = LiveManager.mapLiveTypeFromListToRoom(type);

        if (TextUtils.isEmpty(videoUrl) && TextUtils.isEmpty(liveId)) {
            int liveEndType = SchemeUtils.getInt(uri, SchemeConstants.PARAM_TYPE_LIVE_END, 0);
            if (liveEndType == SchemeConstants.TYPE_PERSON_INFO) { //跳转个人资料页
                //因為沒有个人资料页，所以这里做了拦截
                return;
            }
        }

        RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, videoUrl)
                .setLiveType(liveType)
                .build();
        WatchSdkActivity.openActivity(activity, roomInfo);
    }

    /**
     * 跳转到充值页
     */
    public static void jumpToRechargeActivity(@NonNull Activity activity) {
        RechargeActivity.openActivity(activity, null);
    }
}
