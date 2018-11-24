package com.common.core.scheme.processor;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.module.RouterConstants;
import com.common.core.scheme.SchemeConstants;
import com.common.core.scheme.SchemeUtils;
import com.common.core.scheme.event.H5EventClass;
import com.common.log.MyLog;
import com.common.utils.U;

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
                EventBus.getDefault().post(new H5EventClass.H5UnloginEvent());
                break;
            case SchemeConstants.HOST_RECHARGE:
                if (isLegalPath(uri, "processDirectPay", SchemeConstants.PATH_DIRECT_PAY)) {
                    processRechargeDirectPay(uri, activity);
                } else {
                    jumpToRechargeActivity(activity);
                }
                break;
            case SchemeConstants.HOST_CONTEST:
                //TODO-冲顶大会入口隐藏了
                U.getToastUtil().showShort("冲顶大会入口隐藏了");
                break;
            default:
                String tips = String.format("无法识别的host:%s,下载小米直播体验完整功能", host);
                U.getToastUtil().showShort(tips);
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
            // todo 打开LongTextActivity
            ARouter.getInstance().build(RouterConstants.ACTIVITY_LONGTEXT)
                    .withLong("ownerId", ownerId)
                    .withString("feedId", feedId)
                    .greenChannel().navigation();
        } else {
            //这里拿掉区分type的
            // todo 打开VideoDetailSdkActivity
            ARouter.getInstance().build(RouterConstants.ACTIVITY_VIDEO)
                    .withLong("ownerId", ownerId)
                    .withString("feedId", feedId)
                    .withString("videoUrl", videoUrl)
                    .greenChannel().navigation();
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
        int liveType = SchemeUtils.getInt(uri, SchemeConstants.PARAM_TYPE, 0);

        /**
         *进行冲顶大会，直接判断进入
         * <p/>
         *sdk查询房间，WatchActivity里有逻辑，这里和直播不一致
         */
        boolean isContest = uri.getBooleanQueryParameter(SchemeConstants.PARAM_IS_CONTEST, false);
        if (isContest) {
            //TODO-冲顶大会入口隐藏了
            U.getToastUtil().showShort("冲顶大会入口隐藏了");
            return;
        }

        if (TextUtils.isEmpty(videoUrl) && TextUtils.isEmpty(liveId)) {
            int liveEndType = SchemeUtils.getInt(uri, SchemeConstants.PARAM_TYPE_LIVE_END, 0);
            if (liveEndType == SchemeConstants.TYPE_PERSON_INFO) { //跳转个人资料页
                //因為沒有个人资料页，所以这里做了拦截
                String tips = String.format("更多精彩内容，请下载小米直播体验");
                U.getToastUtil().showShort(tips);
                return;
            }
        }

        // todo 打开WatchSdkAcitivity
        ARouter.getInstance().build(RouterConstants.ACTIVITY_WATCH)
                .withLong("playerId", playerId)
                .withString("liveId", liveId)
                .withString("videoUrl", videoUrl)
                .withInt("liveType", liveType)
                .greenChannel().navigation();
    }

    /**
     * 跳转到充值页
     */
    public static void jumpToRechargeActivity(@NonNull Activity activity) {
        // todo 打开RechargeActivity
        ARouter.getInstance().build(RouterConstants.ACTIVITY_RECHARGE)
                .greenChannel().navigation();
    }

    private static void jumpToContestPrepare(Uri uri, BaseActivity activity) {
        if (!isLegalPath(uri, "jumpToContestPrepare", SchemeConstants.PATH_PREPARE)) {
            return;
        }
        long zuid = SchemeUtils.getLong(uri, SchemeConstants.PARAM_ZUID, 0);
        U.getToastUtil().showShort("冲顶大会入口隐藏了");
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
        EventBus.getDefault().post(new H5EventClass.H5FirstPayEvent(goodId, gemCnt, giveGemCnt, goodPrice, payType, channel));
    }
}
