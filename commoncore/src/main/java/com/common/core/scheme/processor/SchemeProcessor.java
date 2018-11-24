package com.common.core.scheme.processor;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.module.RouterConstants;
import com.common.core.scheme.SchemeConstants;
import com.common.core.scheme.SchemeUtils;
import com.common.log.MyLog;
import com.common.utils.U;


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
                U.getToastUtil().showShort("冲顶大会入口隐藏了");
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
            U.getToastUtil().showShort("冲顶大会入口隐藏了");
            return;
        }

        if (!TextUtils.isEmpty(liveId)) {
            MyLog.w(TAG, "processHostRoom roomInfoRequest");
//            Observable.just(0)
//                    .map(new Function<Integer, RoomInfoRsp>() {
//                        @Override
//                        public RoomInfoRsp apply(Integer integer) throws Exception {
//                            return RoomInfoServerApi.getRoomInfo(MyUserInfoManager.getInstance().getMyUserInfo().getUserInfo().getUserId(),
//                                    playerId, liveId, null, false, false);
//                        }
//                    })
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .compose(activity.<RoomInfoRsp>bindUntilEvent(ActivityEvent.DESTROY))
//                    .subscribe(new Observer<RoomInfoRsp>() {
//                        @Override
//                        public void onSubscribe(Disposable d) {
//
//                        }
//
//                        @Override
//                        public void onNext(RoomInfoRsp roomInfoRsp) {
//                            MyLog.w(TAG, "roomInfoRequest enter");
//                            if (roomInfoRsp != null && roomInfoRsp.getRetCode() == 0) {
//                                if (roomInfoRsp.hasDownStreamUrl() && !TextUtils.isEmpty(roomInfoRsp.getDownStreamUrl())) {
//                                    MyLog.w(TAG, "roomInfoRequest enterWatch success");
//                                    jumpToWatchActivity(playerId, liveId, roomInfoRsp.getType(), roomInfoRsp.getDownStreamUrl());
//                                    activity.finish();
//                                    return;
//                                } else if (roomInfoRsp.hasPlaybackUrl() && !TextUtils.isEmpty(roomInfoRsp.getPlaybackUrl())) {
//                                    MyLog.w(TAG, "roomInfoRequest enterVideoDetail success");
//                                    jumpToWatchActivity(playerId, liveId, roomInfoRsp.getType(), roomInfoRsp.getPlaybackUrl());
//                                    activity.finish();
//                                    return;
//                                }
//
//                            }
//                            queryHistoryLive(playerId, activity);
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//
//                        }
//
//                        @Override
//                        public void onComplete() {
//
//                        }
//                    });
        } else {
            queryRoomInfo(playerId, activity);
        }
    }

    private static void queryRoomInfo(final long playerId, @NonNull final BaseActivity activity) {
        MyLog.w(TAG, "processHostRoom queryRoomInfo");
//        Observable.just(0)
//                .map(new Function<Integer, HisRoomRsp>() {
//                    @Override
//                    public HisRoomRsp apply(Integer integer) throws Exception {
//                        return RoomInfoServerApi.getLiveShowByUserId(playerId);
//                    }
//                })
//                .subscribeOn(Schedulers.io())
//                .compose(activity.<HisRoomRsp>bindUntilEvent(ActivityEvent.DESTROY))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<HisRoomRsp>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(HisRoomRsp hisRoomRsp) {
//                        MyLog.w(TAG, "queryRoomInfo enter");
//                        if (hisRoomRsp != null && hisRoomRsp.getRetCode() == 0) {
//                            MyLog.w(TAG, "queryRoomInfo success");
//                            jumpToWatchActivity(playerId, hisRoomRsp.getLiveId(), hisRoomRsp.getType(), hisRoomRsp.getViewUrl());
//                            activity.finish();
//                            return;
//                        } else {
//                            MyLog.w(TAG, " queryRoomInfo err !!!");
//                        }
//
//                        queryHistoryLive(playerId, activity);
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        MyLog.e(TAG, "queryRoomInfo failed ");
//                        MyLog.e(e);
//                        activity.finish();
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
    }

    private static void queryHistoryLive(final long playerId, @NonNull final BaseActivity activity) {
        MyLog.w(TAG, "processHostRoom queryHistoryLive");
//        Observable.just(0)
//                .map(new Function<Integer, HistoryLiveRsp>() {
//                    @Override
//                    public HistoryLiveRsp apply(Integer integer) throws Exception {
//                        return RoomInfoServerApi.getHistoryShowList(MyUserInfoManager.getInstance().getUid(), playerId);
//                    }
//                })
//                .subscribeOn(Schedulers.io())
//                .compose(activity.<HistoryLiveRsp>bindUntilEvent(ActivityEvent.DESTROY))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<HistoryLiveRsp>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(HistoryLiveRsp historyLiveRsp) {
//                        MyLog.w(TAG, "queryHistoryLive enter");
//                        if (historyLiveRsp != null && historyLiveRsp.getRetCode() == 0) {
//                            List<HisLive> list = historyLiveRsp.getHisLiveList();
//                            if (list != null && list.size() > 0) {
//                                MyLog.w(TAG, "queryHistoryLive success");
//                                HisLive hisLive = list.get(0);
//                                jumpToVideoDetailActivity(playerId, hisLive.getLiveId(), hisLive.getType(), hisLive.getUrl());
//                            }
//                        } else {
//                            MyLog.w(TAG, " queryHistoryLive err !!!");
//                        }
//                        activity.finish();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        MyLog.e(TAG, "queryHistoryLive failed ");
//                        MyLog.e(e);
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
    }

    private static void jumpToWatchActivity(long playerId, String liveId, int liveType, String videoUrl) {
        // todo 打开WatchSdkAcitivity
        ARouter.getInstance().build(RouterConstants.ACTIVITY_WATCH)
                .withLong("playerId", playerId)
                .withString("liveId", liveId)
                .withString("videoUrl", videoUrl)
                .withInt("liveType", liveType)
                .greenChannel().navigation();
    }

    protected static void jumpToVideoDetailActivity(long playerId, String liveId, int liveType, String videoUrl) {
        // todo 打开 VideoDetailSdkActivity
        ARouter.getInstance().build(RouterConstants.ACTIVITY_VIDEO)
                .withLong("playerId", playerId)
                .withString("liveId", liveId)
                .withString("videoUrl", videoUrl)
                .withInt("liveType", liveType)
                .greenChannel().navigation();
    }

    private static void jumpToContestPrepare(Uri uri, BaseActivity activity) {
        if (!isLegalPath(uri, "jumpToContestPrepare", SchemeConstants.PATH_PREPARE)) {
            return;
        }
        long zuid = SchemeUtils.getLong(uri, SchemeConstants.PARAM_ZUID, 0);
        //TODO-冲顶大会入口隐藏了
        U.getToastUtil().showShort("冲顶大会入口隐藏了");
    }
}
