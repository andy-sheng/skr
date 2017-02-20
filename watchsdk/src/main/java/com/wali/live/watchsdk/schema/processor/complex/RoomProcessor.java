package com.wali.live.watchsdk.schema.processor.complex;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.data.LiveShow;
import com.mi.live.data.manager.UserInfoManager;
import com.wali.live.michannel.ChannelParam;
import com.wali.live.proto.LiveProto;
import com.wali.live.watchsdk.schema.SchemeConstants;
import com.wali.live.watchsdk.schema.SchemeUtils;
import com.wali.live.watchsdk.schema.processor.WaliliveProcessor;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * Created by lan on 16/11/7.
 *
 * @module scheme
 * @description 直播间的跳转逻辑，通过WaliliveProcessor跳转
 */
public class RoomProcessor {
    private static final String TAG = SchemeConstants.LOG_PREFIX + RoomProcessor.class.getSimpleName();

    // 频道打点参数保存
    private static ChannelParam sChannelParam;
    // 是否finishActivity
    private static boolean sFinishActivity;

    // 用于指示GetLiveRoomInfoTask是否正在进行
    private static boolean sGetLiveRoomInfoTaskRunning = false;

    /**
     * Scheme中包含的直播结束后跳转去向
     */
    enum AfterLiveEnd {
        END_LIVE(0),
        REPLAY_OR_END_LIVE(1),
        PERSON_INFO(2);

        private int type;

        AfterLiveEnd(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public static AfterLiveEnd instance(int type, AfterLiveEnd defaultLiveEnd) {
            if (type == AfterLiveEnd.END_LIVE.getType()) {
                return AfterLiveEnd.END_LIVE;
            } else if (type == AfterLiveEnd.REPLAY_OR_END_LIVE.getType()) {
                return AfterLiveEnd.REPLAY_OR_END_LIVE;
            } else if (type == AfterLiveEnd.PERSON_INFO.getType()) {
                return AfterLiveEnd.PERSON_INFO;
            }
            return defaultLiveEnd;
        }
    }

    /**
     * 跳转到直播间
     */
    public static void processHostRoom(Uri uri, @NonNull final RxActivity activity, boolean finishActivity) {
        // 参数保存
        sFinishActivity = finishActivity;

        if (!WaliliveProcessor.isLegalPath(uri, "processHostRoom", SchemeConstants.PATH_JOIN)) {
            finish(activity);
            return;
        }

        final long playerId = SchemeUtils.getLong(uri, SchemeConstants.PARAM_PLAYER_ID, 0);
        if (playerId <= 0) {
            MyLog.e(TAG, "error, playerId=" + playerId);
            finish(activity);
            return;
        }

        final String liveId = uri.getQueryParameter(SchemeConstants.PARAM_ROOMID);
        String videoUrl = uri.getQueryParameter(SchemeConstants.PARAM_VIDEOURL);

        boolean needQueryRoomInfo = uri.getBooleanQueryParameter(SchemeConstants.PARAM_NEED_QUERY_ROOM_INFO, false);
        // 如果liveId为空，也需要查询最新的房间
        if (TextUtils.isEmpty(liveId)) {
            needQueryRoomInfo = true;
        }
        // 对于门票直播，服务器提供的scheme没有type和videoUrl，需要我们去请求roominfo获取到type
        // 这样服务器给的videoUrl是升级视频流，可用于老客户端
        // type给新客户端，用以识别门票直播
        int liveType = SchemeUtils.getInt(uri, SchemeConstants.PARAM_TYPE, 0);

        int afterLiveEndType = SchemeUtils.getInt(uri, SchemeConstants.PARAM_AFTER_LIVE_END, 0);
        final AfterLiveEnd afterLiveEnd = AfterLiveEnd.instance(afterLiveEndType, AfterLiveEnd.END_LIVE);

        // 跳转逻辑
        if (!needQueryRoomInfo) {
            if (afterLiveEnd == AfterLiveEnd.END_LIVE && !TextUtils.isEmpty(videoUrl)) {
                // 类型是默认类型，直接进行跳转
                normalJumpToWatchActivity(playerId, liveId,  videoUrl, activity);
            } else {
                if (sGetLiveRoomInfoTaskRunning) {
                    return;
                }
                sGetLiveRoomInfoTaskRunning = true;

                // 查询当前的liveId是否是正在直播的房间
                Observable.just(liveId)
                        .map(new Func1<String, LiveProto.RoomInfoRsp>() {
                            @Override
                            public LiveProto.RoomInfoRsp call(String s) {
                                return LiveManager.roomInfoRsp(playerId, s);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(((RxActivity) activity).<LiveProto.RoomInfoRsp>bindToLifecycle())
                        .subscribe(new Observer<LiveProto.RoomInfoRsp>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(LiveProto.RoomInfoRsp rsp) {
                                sGetLiveRoomInfoTaskRunning = false;
                                if (rsp != null && rsp.hasRetCode() && (rsp.getRetCode() == 0)) {
                                    // 直播房间存在
                                    if (rsp.hasDownStreamUrl() && (!TextUtils.isEmpty(rsp.getDownStreamUrl()))) {
                                        // 有具体的直播地址
                                        normalJumpToWatchActivity(playerId, liveId, rsp.getDownStreamUrl(), activity);
                                    } else {
                                        // 没有直播地址，liveId无效
                                        String playBackUrl = null;
                                        if (rsp.hasPlaybackUrl() && (!TextUtils.isEmpty(rsp.getPlaybackUrl()))) {
                                            playBackUrl = rsp.getPlaybackUrl();
                                        }
                                        checkAfterLiveEndParam(playerId, liveId,  afterLiveEnd, playBackUrl, activity);
                                    }
                                } else {
                                    // 直播房间不存在,检查是否存在回放地址
                                    if (rsp != null) {
                                        String playBackUrl = null;
                                        if (rsp.hasPlaybackUrl() && (!TextUtils.isEmpty(rsp.getPlaybackUrl()))) {
                                            playBackUrl = rsp.getPlaybackUrl();
                                        }
                                        checkAfterLiveEndParam(playerId, liveId, afterLiveEnd, playBackUrl, activity);
                                    }
                                }
                            }
                        });
            }
        } else {
            queryRoomInfo(playerId, liveId, liveType, afterLiveEnd, activity);
        }
    }

    /**
     * 不需要重定向liveId，且之前Scheme中携带的liveId房间正常
     */
    private static void normalJumpToWatchActivity(long playerId, String liveId, String videoUrl, @NonNull RxActivity activity) {
        RoomInfo liveShow = RoomInfo.Builder.newInstance(playerId,liveId,videoUrl).build();
        jumpToWatchActivity(liveShow, activity);
    }

    /**
     * query_room_info为true或liveid为空
     * 查找主播最新的直播
     * 通过playerId查询最新的liveId，判断liveId是否为空
     * - 非空 : 主播正在直播，跳转直播间
     * - 空 : 说明主播不在直播了，对after_live_end参数进行分析
     */
    private static void queryRoomInfo(final long playerId, final String liveId, final int liveType, final AfterLiveEnd afterLiveEnd, @NonNull final RxActivity activity) {
        // 需要查询最新的房间信息
        if (sGetLiveRoomInfoTaskRunning) {
            return;
        }
        sGetLiveRoomInfoTaskRunning = true;

        if (TextUtils.isEmpty(liveId)) {
            // 没有给liveId，容错
            Observable.just(playerId)
                    .map(new Func1<Long, LiveShow>() {
                        @Override
                        public LiveShow call(Long aLong) {
                            return UserInfoManager.getLiveShowByUserId(playerId);
                        }
                    })
                    .map(new Func1<LiveShow, RoomInfo>() {
                        @Override
                        public RoomInfo call(LiveShow liveShow) {
                            return RoomInfo.Builder.newInstance(liveShow.getUid(),liveShow.getLiveId(),liveShow.getUrl()).build();
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(activity.<RoomInfo>bindToLifecycle())
                    .subscribe(new Action1<RoomInfo>() {
                        @Override
                        public void call(RoomInfo liveShow) {
                            sGetLiveRoomInfoTaskRunning = false;
                            if (!TextUtils.isEmpty(liveShow.getLiveId())) {
                                // 查询到liveId非空,直接跳转直播间
                                jumpToWatchActivity(liveShow, activity);
                            } else {
                                // 找不到liveId信息，根据参数跳转结束页面
                                checkAfterLiveEndParam(playerId, String.valueOf(playerId), afterLiveEnd, null, activity);
                            }
                        }
                    });
        } else {
            /**
             * 通过10-20号新合成的接口一次性拿到直播地址或者回放地址
             * 如果当前主播正在直播，则返回最新房间号和最新的直播地址
             * 如果当前主播没在直播，如果有回放地址会返回回放地址
             */
            Observable.just(liveId)
                    .map(new Func1<String, LiveProto.RoomInfoRsp>() {
                        @Override
                        public LiveProto.RoomInfoRsp call(String s) {
                            return LiveManager.roomInfoRspGetLatestLive(playerId, s);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(activity.<LiveProto.RoomInfoRsp>bindToLifecycle())
                    .subscribe(new Action1<LiveProto.RoomInfoRsp>() {
                        @Override
                        public void call(LiveProto.RoomInfoRsp roomInfoRsp) {
                            sGetLiveRoomInfoTaskRunning = false;
                            if (roomInfoRsp != null && roomInfoRsp.hasRetCode() && roomInfoRsp.getRetCode() == 0) {
                                if (roomInfoRsp.hasDownStreamUrl() && (!TextUtils.isEmpty(roomInfoRsp.getDownStreamUrl()))) {
                                    // 正在直播,直接跳转直播间
                                    RoomInfo liveShow = RoomInfo.Builder.newInstance(playerId,roomInfoRsp.getLiveid(),roomInfoRsp.getPlaybackUrl()).build();
                                    if (roomInfoRsp.hasLiveid() && (!TextUtils.isEmpty(roomInfoRsp.getLiveid()))) {
                                        liveShow.setmLiveId(roomInfoRsp.getLiveid());
                                    } else {
                                        liveShow.setmLiveId(liveId);
                                    }
                                    liveShow.setmVideoUrl(roomInfoRsp.getDownStreamUrl());
                                    jumpToWatchActivity(liveShow, activity);
                                } else {
                                    String playBackUrl = null;
                                    if (roomInfoRsp.hasPlaybackUrl() && (!TextUtils.isEmpty(roomInfoRsp.getPlaybackUrl()))) {
                                        playBackUrl = roomInfoRsp.getPlaybackUrl();
                                    }
                                    checkAfterLiveEndParam(playerId, liveId, afterLiveEnd, playBackUrl, activity);
                                }
                            } else {
                                if (roomInfoRsp != null) {
                                    String playBackUrl = null;
                                    if (roomInfoRsp.hasPlaybackUrl() && (!TextUtils.isEmpty(roomInfoRsp.getPlaybackUrl()))) {
                                        // 存在回放地址
                                        playBackUrl = roomInfoRsp.getPlaybackUrl();
                                    }
                                    checkAfterLiveEndParam(playerId, liveId, afterLiveEnd, playBackUrl, activity);
                                } else {
                                    finish(activity);
                                }
                            }
                        }
                    });
        }
    }

    /**
     * 不涉及网络请求了
     * 主播当前没有在直播了，根据after_live_end参数来判断
     * - 0 : (默认) 跳直播结束页。
     * - 1 : 找该Scheme带入的liveid对应的回放，若没有或liveid为空，则跳直播结束页，若有，进回放
     * - 2 : 跳到playid对应的个人主页
     */
    private static void checkAfterLiveEndParam(long playerId, String liveId, AfterLiveEnd afterLiveEnd, String playBackUrl, @NonNull RxActivity activity) {
        RoomInfo liveShow = RoomInfo.Builder.newInstance(playerId,liveId,playBackUrl).build();
        jumpToWatchActivity(liveShow, activity);
    }

    /**
     * 跳到直播页
     */
    private static void jumpToWatchActivity(RoomInfo liveShow, @NonNull RxActivity activity) {
        if (liveShow != null) {
                WatchSdkActivity.openActivity(activity, liveShow);
                finish(activity);
        } else {
            finish(activity);
        }
    }

    private static void finish(@NonNull Activity activity) {
        sChannelParam = null;
        if (sFinishActivity) {
            activity.finish();
        }
    }
}
