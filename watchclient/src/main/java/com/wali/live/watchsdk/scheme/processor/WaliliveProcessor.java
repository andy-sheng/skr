package com.wali.live.watchsdk.scheme.processor;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.activity.BaseActivity;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.LiveManager;
import com.wali.live.event.EventClass;
import com.wali.live.pay.activity.RechargeActivity;
import com.wali.live.watchsdk.contest.ContestPrepareActivity;
import com.wali.live.watchsdk.contest.ContestWatchActivity;
import com.wali.live.watchsdk.longtext.LongTextActivity;
import com.wali.live.watchsdk.scheme.SchemeConstants;
import com.wali.live.watchsdk.scheme.SchemeUtils;
import com.wali.live.watchsdk.watch.VideoDetailSdkActivity;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by lan on 16/10/26.
 *
 * @module scheme
 * @description Walilive的Uri的逻辑代码
 */
public class WaliliveProcessor extends CommonProcessor {
    private static final String TAG = SchemeConstants.LOG_PREFIX + WaliliveProcessor.class.getSimpleName();

    public static boolean process(@NonNull Uri uri, String host, @NonNull BaseActivity activity, boolean finishActivity) {
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
                if (isLegalPath(uri, "processDirectPay", SchemeConstants.PATH_DIRECT_PAY)) {
                    processRechargeDirectPay(uri, activity);
                } else {
                    jumpToRechargeActivity(activity);
                }
//                jumpToRechargeActivity(activity);
                break;
            case SchemeConstants.HOST_CONTEST:
                jumpToContestPrepare(uri, activity);
                break;
            default:
                String tips = String.format("无法识别的host:%s,下载小米直播体验完整功能", host);
                ToastUtils.showToast(tips);
                return false;
        }
        if (finishActivity) {
            activity.finish();
        }
        return true;
    }

    public static void processHostFeed(Uri uri, @NonNull Activity activity) {
        if (!isLegalPath(uri, "processHostFeed", SchemeConstants.PATH_NEWS_INFO)) {
            return;
        }

        String feedId = uri.getQueryParameter(SchemeConstants.PARAM_FEED_ID);
        long ownerId = SchemeUtils.getLong(uri, SchemeConstants.PARAM_OWENER_ID, 0);
        int feedsType = SchemeUtils.getInt(uri, SchemeConstants.PARAM_FEEDS_TYPE, 0);
        String videoUrl = uri.getQueryParameter(SchemeConstants.PARAM_VIDEO_URL);
        int extType = SchemeUtils.getInt(uri, SchemeConstants.PARAM_EXT_TYPE, 0);

        if (extType == SchemeConstants.EXT_TYPE_LONG_TEXT) {
            LongTextActivity.open(activity, feedId, ownerId);
        } else {
            //这里拿掉区分type的
            VideoDetailSdkActivity.openActivity(activity, RoomInfo.Builder.newInstance(ownerId, feedId, videoUrl).build());
        }
    }

    /**
     * 不使用CommonProcessor，因为walilive的room协议更复杂，这里单独处理
     */
    protected static void processHostRoom(Uri uri, BaseActivity activity) {
        if (!isLegalPath(uri, "processHostRoom", SchemeConstants.PATH_JOIN)) {
            return;
        }

        String liveId = uri.getQueryParameter(SchemeConstants.PARAM_LIVE_ID);
        long playerId = SchemeUtils.getLong(uri, SchemeConstants.PARAM_PLAYER_ID, 0);
        String videoUrl = uri.getQueryParameter(SchemeConstants.PARAM_VIDEO_URL);
        int type = SchemeUtils.getInt(uri, SchemeConstants.PARAM_TYPE, 0);
        int liveType = LiveManager.mapLiveTypeFromListToRoom(type);

        /**
         *进行冲顶大会，直接判断进入
         * <p/>
         *sdk查询房间，WatchActivity里有逻辑，这里和直播不一致
         */
        boolean isContest = uri.getBooleanQueryParameter(SchemeConstants.PARAM_IS_CONTEST, false);
        if (isContest) {
            ContestWatchActivity.open(activity, playerId, liveId, videoUrl);
            return;
        }

        if (TextUtils.isEmpty(videoUrl) && TextUtils.isEmpty(liveId)) {
            int liveEndType = SchemeUtils.getInt(uri, SchemeConstants.PARAM_TYPE_LIVE_END, 0);
            if (liveEndType == SchemeConstants.TYPE_PERSON_INFO) { //跳转个人资料页
                //因為沒有个人资料页，所以这里做了拦截
                String tips = String.format("更多精彩内容，请下载小米直播体验");
                ToastUtils.showToast(tips);
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

    private static void jumpToContestPrepare(Uri uri, BaseActivity activity) {
        if (!isLegalPath(uri, "jumpToContestPrepare", SchemeConstants.PATH_PREPARE)) {
            return;
        }
        long zuid = SchemeUtils.getLong(uri, SchemeConstants.PARAM_ZUID, 0);
        ContestPrepareActivity.open(activity, zuid);
    }

    private static void processRechargeDirectPay(Uri uri, @NonNull Activity activity) {
        long uuid = SchemeUtils.getLong(uri, SchemeConstants.PARAM_UUID, 0);
        int goodId = SchemeUtils.getInt(uri, SchemeConstants.PARAM_GOODS_ID, 0);
        int gemCnt = SchemeUtils.getInt(uri, SchemeConstants.PARAM_GEM_CNT, 0);
        int giveGemCnt = SchemeUtils.getInt(uri, SchemeConstants.PARAM_GIVE_GEM_CNT, 0);
        int goodPrice = SchemeUtils.getInt(uri, SchemeConstants.PARAM_PRICE, 0);
        int times = SchemeUtils.getInt(uri, SchemeConstants.PARAM_TIMES, 0);
        int payType = SchemeUtils.getInt(uri, SchemeConstants.PARAM_PAY_TYPE, 0);
        int channel = SchemeUtils.getInt(uri, SchemeConstants.PARAM_PAY_CHANNEL, -1);
//        RechargeDirectPayActivity.openActivity(activity, uuid, goodId, gemCnt, giveGemCnt, goodPrice, times, payType, channel);
        EventBus.getDefault().post(new com.wali.live.watchsdk.eventbus.EventClass.H5FirstPayEvent(goodId, gemCnt, giveGemCnt, goodPrice, payType, channel));
    }
}
