package com.wali.live.watchsdk.scheme.processor;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.activity.BaseActivity;
import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.api.request.RoomInfoRequest;
import com.mi.live.data.data.LiveShow;
import com.mi.live.data.manager.UserInfoManager;
import com.wali.live.proto.Live2Proto;
import com.wali.live.proto.LiveProto;
import com.wali.live.proto.LiveProto.HistoryLiveRsp;
import com.wali.live.proto.LiveProto.RoomInfoRsp;
import com.wali.live.watchsdk.scheme.SchemeConstants;
import com.wali.live.watchsdk.scheme.SchemeUtils;
import com.wali.live.watchsdk.watch.VideoDetailSdkActivity;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

//import activity.ContestPrepareActivity;
//import activity.ContestWatchActivity;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 16/10/26.
 *
 * @module scheme
 * @description LiveSdk的Uri的逻辑代码
 */
public class SchemeProcessor extends CommonProcessor {
    private static final String TAG = SchemeConstants.LOG_PREFIX + SchemeProcessor.class.getSimpleName();

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
            case SchemeConstants.HOST_ROOM:
                processHostRoom(uri, activity);
                return true;
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
            case SchemeConstants.HOST_ZHIBO_COM:
                processHostLivesdk(uri, activity);
                break;
            case SchemeConstants.HOST_CONTEST:
                //TODO-冲顶大会入口隐藏了
                ToastUtils.showToast("冲顶大会入口隐藏了");
//                jumpToContestPrepare(uri, activity);
                break;
            default:
                return false;
        }
        if (finishActivity) {
            activity.finish();
        }
        return true;
    }

    /**
     * 使用action模拟正常livesdk的path
     */
    private static void processHostLivesdk(Uri uri, @NonNull BaseActivity activity) {
        String action = uri.getQueryParameter(SchemeConstants.PARAM_ACTION);
        switch (action) {
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
                processHostSubList(uri, activity);
                break;
        }
    }

    private static void processHostRoom(Uri uri, final BaseActivity activity) {
        if (!isLegalPath(uri, "processHostRoom", SchemeConstants.PATH_JOIN)) {
            activity.finish();
            return;
        }

        final String liveId = uri.getQueryParameter(SchemeConstants.PARAM_LIVE_ID);
        final long playerId = SchemeUtils.getLong(uri, SchemeConstants.PARAM_PLAYER_ID, 0);
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
            ToastUtils.showToast("冲顶大会入口隐藏了");
//            ContestWatchActivity.open(activity, playerId, liveId, videoUrl);
            return;
        }

        if (!TextUtils.isEmpty(liveId)) {
            MyLog.w(TAG, "processHostRoom roomInfoRequest");
            Observable.just(0)
                    .map(new Func1<Integer, RoomInfoRsp>() {
                        @Override
                        public RoomInfoRsp call(Integer integer) {
                            return new RoomInfoRequest(playerId, liveId).syncRsp();
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(activity.<RoomInfoRsp>bindUntilEvent())
                    .subscribe(new Action1<RoomInfoRsp>() {
                        @Override
                        public void call(RoomInfoRsp rsp) {
                            MyLog.w(TAG, "roomInfoRequest enter");
                            if (rsp != null) {
                                if (rsp.hasDownStreamUrl() && !TextUtils.isEmpty(rsp.getDownStreamUrl())) {
                                    MyLog.w(TAG, "roomInfoRequest enterWatch success");
                                    jumpToWatchActivity(playerId, liveId, rsp.getType(), rsp.getDownStreamUrl(), activity);
                                    activity.finish();
                                    return;
                                } else if (rsp.hasPlaybackUrl() && !TextUtils.isEmpty(rsp.getPlaybackUrl())) {
                                    MyLog.w(TAG, "roomInfoRequest enterVideoDetail success");
                                    jumpToVideoDetailActivity(playerId, liveId, rsp.getType(), rsp.getPlaybackUrl(), activity);
                                    activity.finish();
                                    return;
                                }
                            }
                            queryHistoryLive(playerId, activity);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            MyLog.e(TAG, "processHostRoom=" + throwable);
                            activity.finish();
                        }
                    });
        } else {
            queryRoomInfo(playerId, activity);
        }
    }

    private static void queryRoomInfo(final long playerId, @NonNull final RxActivity activity) {
        MyLog.w(TAG, "processHostRoom queryRoomInfo");
        Observable.just(0)
                .map(new Func1<Integer, LiveShow>() {
                    @Override
                    public LiveShow call(Integer i) {
                        return UserInfoManager.getLiveShowByUserId(playerId);
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(activity.<LiveShow>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LiveShow>() {
                    @Override
                    public void call(LiveShow liveShow) {
                        MyLog.w(TAG, "queryRoomInfo enter");
                        if (liveShow != null && !TextUtils.isEmpty(liveShow.getLiveId())) {
                            MyLog.w(TAG, "queryRoomInfo success");
                            jumpToWatchActivity(playerId, liveShow.getLiveId(), liveShow.getLiveType(),
                                    liveShow.getUrl(), activity);
                            activity.finish();
                            return;
                        }
                        queryHistoryLive(playerId, activity);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "queryRoomInfo failed=" + throwable);
                        activity.finish();
                    }
                });
    }

    private static void queryHistoryLive(final long playerId, @NonNull final RxActivity activity) {
        MyLog.w(TAG, "processHostRoom queryHistoryLive");
        Observable.just(0)
                .map(new Func1<Integer, HistoryLiveRsp>() {
                    @Override
                    public LiveProto.HistoryLiveRsp call(Integer i) {
                        return LiveManager.historyRsp(playerId);
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(activity.<LiveProto.HistoryLiveRsp>bindUntilEvent())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<HistoryLiveRsp>() {
                    @Override
                    public void call(HistoryLiveRsp rsp) {
                        MyLog.w(TAG, "queryHistoryLive enter");
                        if (rsp != null && rsp.hasRetCode() && rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
                            if (rsp.getHisLiveCount() > 0) {
                                MyLog.w(TAG, "queryHistoryLive success");
                                Live2Proto.HisLive hisLive = rsp.getHisLive(0);
                                jumpToVideoDetailActivity(playerId, hisLive.getLiveId(), hisLive.getType(), hisLive.getUrl(), activity);
                            }
                        }
                        activity.finish();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "queryHistoryLive failed=" + throwable);
                        activity.finish();
                    }
                });
    }

    private static void jumpToWatchActivity(long playerId, String liveId, int liveType, String videoUrl, @NonNull Activity activity) {
        RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, videoUrl)
                .setLiveType(liveType)
                .build();
        WatchSdkActivity.openActivity(activity, roomInfo);
    }

    protected static void jumpToVideoDetailActivity(long playerId, String liveId, int liveType, String videoUrl, @NonNull Activity activity) {
        RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, videoUrl)
                .setLiveType(liveType)
                .build();
        VideoDetailSdkActivity.openActivity(activity, roomInfo);
    }

    private static void jumpToContestPrepare(Uri uri, BaseActivity activity) {
        if (!isLegalPath(uri, "jumpToContestPrepare", SchemeConstants.PATH_PREPARE)) {
            return;
        }
        long zuid = SchemeUtils.getLong(uri, SchemeConstants.PARAM_ZUID, 0);
        //TODO-冲顶大会入口隐藏了
        ToastUtils.showToast("冲顶大会入口隐藏了");
//        ContestPrepareActivity.open(activity, zuid);
    }
}
