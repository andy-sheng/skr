package com.common.core.scheme.processor;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.core.RouterConstants;
import com.common.core.scheme.SchemeConstants;
import com.common.core.scheme.SchemeUtils;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.log.MyLog;
import com.common.utils.U;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.wali.live.proto.User.HisRoomRsp;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


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
                U.getToastUtil().showToast("冲顶大会入口隐藏了");
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
            U.getToastUtil().showToast("冲顶大会入口隐藏了");
            return;
        }

        if (!TextUtils.isEmpty(liveId)) {
            MyLog.w(TAG, "processHostRoom roomInfoRequest");
//            Observable.just(0)
//                    .map(new Func1<Integer, RoomInfoRsp>() {
//                        @Override
//                        public RoomInfoRsp call(Integer integer) {
//                            return new RoomInfoRequest(playerId, liveId).syncRsp();
//                        }
//                    })
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .compose(activity.<RoomInfoRsp>bindUntilEvent())
//                    .subscribe(new Action1<RoomInfoRsp>() {
//                        @Override
//                        public void call(RoomInfoRsp rsp) {
//                            MyLog.w(TAG, "roomInfoRequest enter");
//                            if (rsp != null) {
//                                if (rsp.hasDownStreamUrl() && !TextUtils.isEmpty(rsp.getDownStreamUrl())) {
//                                    MyLog.w(TAG, "roomInfoRequest enterWatch success");
//                                    jumpToWatchActivity(playerId, liveId, rsp.getType(), rsp.getDownStreamUrl(), activity);
//                                    activity.finish();
//                                    return;
//                                } else if (rsp.hasPlaybackUrl() && !TextUtils.isEmpty(rsp.getPlaybackUrl())) {
//                                    MyLog.w(TAG, "roomInfoRequest enterVideoDetail success");
//                                    jumpToVideoDetailActivity(playerId, liveId, rsp.getType(), rsp.getPlaybackUrl(), activity);
//                                    activity.finish();
//                                    return;
//                                }
//                            }
//                            queryHistoryLive(playerId, activity);
//                        }
//                    }, new Action1<Throwable>() {
//                        @Override
//                        public void call(Throwable throwable) {
//                            MyLog.e(TAG, "processHostRoom=" + throwable);
//                            activity.finish();
//                        }
//                    });
        } else {
            queryRoomInfo(playerId, activity);
        }
    }

    private static void queryRoomInfo(final long playerId, @NonNull final BaseActivity activity) {
        MyLog.w(TAG, "processHostRoom queryRoomInfo");
        Observable.just(0)
                .map(new Function<Integer, HisRoomRsp>() {
                    @Override
                    public HisRoomRsp apply(Integer integer) throws Exception {
                        return UserInfoServerApi.getLiveShowByUserId(playerId);
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(activity.<HisRoomRsp>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<HisRoomRsp>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(HisRoomRsp hisRoomRsp) {
                        MyLog.w(TAG, "queryRoomInfo enter");
                        if (hisRoomRsp != null && hisRoomRsp.getRetCode() == 0) {
                            MyLog.w(TAG, "queryRoomInfo success");
                            jumpToWatchActivity(playerId, hisRoomRsp.getLiveId(), hisRoomRsp.getType(), hisRoomRsp.getViewUrl());
                            activity.finish();
                            return;
                        }

                        queryHistoryLive(playerId, activity);
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "queryRoomInfo failed ");
                        MyLog.e(e);
                        activity.finish();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private static void queryHistoryLive(final long playerId, @NonNull final BaseActivity activity) {
        MyLog.w(TAG, "processHostRoom queryHistoryLive");
//        Observable.just(0)
//                .map(new Func1<Integer, HistoryLiveRsp>() {
//                    @Override
//                    public LiveProto.HistoryLiveRsp call(Integer i) {
//                        return LiveManager.historyRsp(playerId);
//                    }
//                })
//                .subscribeOn(Schedulers.io())
//                .compose(activity.<LiveProto.HistoryLiveRsp>bindUntilEvent())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action1<HistoryLiveRsp>() {
//                    @Override
//                    public void call(HistoryLiveRsp rsp) {
//                        MyLog.w(TAG, "queryHistoryLive enter");
//                        if (rsp != null && rsp.hasRetCode() && rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
//                            if (rsp.getHisLiveCount() > 0) {
//                                MyLog.w(TAG, "queryHistoryLive success");
//                                Live2Proto.HisLive hisLive = rsp.getHisLive(0);
//                                jumpToVideoDetailActivity(playerId, hisLive.getLiveId(), hisLive.getType(), hisLive.getUrl(), activity);
//                            }
//                        }
//                        activity.finish();
//                    }
//                }, new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        MyLog.e(TAG, "queryHistoryLive failed=" + throwable);
//                        activity.finish();
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
        U.getToastUtil().showToast("冲顶大会入口隐藏了");
    }
}
