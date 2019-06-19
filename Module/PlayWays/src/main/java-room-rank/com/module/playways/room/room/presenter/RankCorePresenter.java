package com.module.playways.room.room.presenter;


import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;

import com.alibaba.fastjson.JSON;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.engine.ScoreConfig;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.player.ExoPlayer;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.utils.ActivityUtils;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.component.busilib.SkrConfig;
import com.component.busilib.constans.GameModeType;
import com.engine.EngineEvent;
import com.engine.Params;
import com.engine.arccloud.RecognizeConfig;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.msg.CustomMsgType;
import com.module.msg.IMsgService;
import com.module.playways.RoomDataUtils;
import com.module.playways.others.LyricAndAccMatchManager;
import com.module.playways.room.msg.event.AccBeginEvent;
import com.module.playways.room.msg.event.AppSwapEvent;
import com.module.playways.room.msg.event.ExitGameEvent;
import com.module.playways.room.msg.event.MachineScoreEvent;
import com.module.playways.room.msg.event.PkBurstLightMsgEvent;
import com.module.playways.room.msg.event.PkLightOffMsgEvent;
import com.module.playways.room.msg.event.RoundAndGameOverEvent;
import com.module.playways.room.msg.event.RoundOverEvent;
import com.module.playways.room.msg.event.SyncStatusEvent;
import com.module.playways.room.msg.filter.PushMsgFilter;
import com.module.playways.room.msg.manager.ChatRoomMsgManager;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;
import com.module.playways.room.prepare.model.OnlineInfoModel;
import com.module.playways.room.prepare.model.PlayerInfoModel;
import com.module.playways.room.room.RankRoomData;
import com.module.playways.room.room.RankRoomServerApi;
import com.module.playways.room.room.SwapStatusType;
import com.module.playways.room.room.comment.model.CommentAiModel;
import com.module.playways.room.room.comment.model.CommentSysModel;
import com.module.playways.room.room.comment.model.CommentTextModel;
import com.module.playways.room.room.event.PkSomeOneOnlineChangeEvent;
import com.module.playways.room.room.event.PretendCommentMsgEvent;
import com.module.playways.room.room.event.RankRoundInfoChangeEvent;
import com.module.playways.room.room.model.BLightInfoModel;
import com.module.playways.room.room.model.MLightInfoModel;
import com.module.playways.room.room.model.RankPlayerInfoModel;
import com.module.playways.room.room.model.RankRoundInfoModel;
import com.module.playways.room.room.model.RecordData;
import com.module.playways.room.room.score.MachineScoreItem;
import com.module.playways.room.room.score.RobotScoreHelper;
import com.module.playways.room.room.view.IGameRuleView;
import com.zq.live.proto.Common.ESex;
import com.zq.live.proto.Common.UserInfo;
import com.zq.live.proto.Room.EMsgPosType;
import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.MachineScore;
import com.zq.live.proto.Room.RoomMsg;
import com.zq.lyrics.event.LrcEvent;
import com.zq.lyrics.utils.SongResUtils;
import com.zq.mediaengine.kit.ZqEngineKit;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.agora.rtc.Constants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.module.playways.room.msg.event.ExitGameEvent.EXIT_GAME_AFTER_PLAY;
import static com.module.playways.room.msg.event.ExitGameEvent.EXIT_GAME_OUT_ROUND;
import static com.module.playways.room.room.comment.model.CommentModel.TEXT_WHITE;
import static com.module.playways.room.room.comment.model.CommentModel.TEXT_YELLOW;

public class RankCorePresenter extends RxLifeCyclePresenter {
    String TAG = "RankCorePresenter";

    static final int MSG_ENSURE_IN_RC_ROOM = 9;// 确保在融云的聊天室，保证融云的长链接

    static final int MSG_ROBOT_SING_BEGIN = 10;

    static final int MSG_ENSURE_SWITCH_BROADCAST_SUCCESS = 21; // 确保用户切换成主播成功，防止引擎不回调的保护

    static final int MSG_START_LAST_TWO_SECONDS_TASK = 30;

    private static long sHeartBeatTaskInterval = 3000;
    private static long sSyncStateTaskInterval = 12000;

    RankRoomData mRoomData;

    RankRoomServerApi mRoomServerApi = ApiManager.getInstance().createService(RankRoomServerApi.class);

    Disposable mHeartBeatTask;

    Disposable mSyncGameStateTask;

    IGameRuleView mIGameRuleView;

    RobotScoreHelper mRobotScoreHelper;

    ExoPlayer mExoPlayer;

    PushMsgFilter mPushMsgFilter = new PushMsgFilter<RoomMsg>() {
        @Override
        public boolean doFilter(RoomMsg msg) {
            if (msg.getRoomID() == mRoomData.getGameId()) {
                return true;
            }
            return false;
        }
    };

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ENSURE_IN_RC_ROOM:
                    MyLog.d(TAG, "handleMessage 长时间没收到push，重新进入融云房间容错");
                    ModuleServiceManager.getInstance().getMsgService().leaveChatRoom(mRoomData.getGameId() + "");
                    joinRcRoom(0);
                    break;
                case MSG_ROBOT_SING_BEGIN:
                    robotSingBegin((RankPlayerInfoModel) msg.obj);
                    break;
                case MSG_ENSURE_SWITCH_BROADCAST_SUCCESS:
                    onChangeBroadcastSuccess();
                    break;
//                case MSG_ROBOT_SING_END:
//                    break;
                case MSG_START_LAST_TWO_SECONDS_TASK:
                    BaseRoundInfoModel roundInfoModel = (BaseRoundInfoModel) msg.obj;
                    if (roundInfoModel != null && roundInfoModel == mRoomData.getRealRoundInfo()) {
                        mIGameRuleView.hideMainStage();
                    }
                    break;
            }

        }
    };

    public RankCorePresenter(@NotNull IGameRuleView iGameRuleView, @NotNull RankRoomData roomData) {
        mIGameRuleView = iGameRuleView;
        mRoomData = roomData;
        TAG += hashCode();

        MyLog.w(TAG, "player info is " + mRoomData.toString());

        if (mRoomData.getGameId() > 0) {
            Params params = Params.getFromPref();
            params.setScene(Params.Scene.rank);
            ZqEngineKit.getInstance().init("rankingroom", params);
            boolean isAnchor = false;
//            if(RoomDataUtils.isMyRound(mRoomData.getRealRoundInfo())){
//                isAnchor = true;
//            }
            ZqEngineKit.getInstance().joinRoom(String.valueOf(mRoomData.getGameId()), (int) UserAccountManager.getInstance().getUuidAsLong(), isAnchor, mRoomData.getAgoraToken());
            // 不发送本地音频
//            ZqEngineKit.getInstance().muteLocalAudioStream(true);
            // 系统提示
            pretenSystemMsg();
            // 伪装AI裁判
            pretenAiJudgeMsg();
            // 伪装评论消息
            for (int i = 0; i < mRoomData.getRoundInfoModelList().size(); i++) {
                RankRoundInfoModel roundInfoModel = mRoomData.getRoundInfoModelList().get(i);
                PlayerInfoModel playerInfoModel = RoomDataUtils.getPlayerInfoById(mRoomData, roundInfoModel.getUserID());
                pretenEnterRoom(playerInfoModel, i);
            }
//            IMsgService msgService = ModuleServiceManager.getInstance().getMsgService();
//            if (msgService != null) {
//                msgService.syncHistoryFromChatRoom(String.valueOf(mRoomData.getGameId()), 10, true, null);
//            }
            ChatRoomMsgManager.getInstance().addFilter(mPushMsgFilter);

            if (ScoreConfig.isAcrEnable()) {
                ZqEngineKit.getInstance().startRecognize(RecognizeConfig.newBuilder()
                        .setMode(RecognizeConfig.MODE_MANUAL)
                        .setSongName(mRoomData.getSongModel().getItemName())
                        .setArtist(mRoomData.getSongModel().getOwner())
                        .build()
                );
            }
        }
    }

    private void joinRcRoom(int deep) {
        if (deep > 4) {
            MyLog.d(TAG, "加入融云房间，重试5次仍然失败，放弃");
            return;
        }
        if (mRoomData.getGameId() > 0) {
            ModuleServiceManager.getInstance().getMsgService().joinChatRoom(String.valueOf(mRoomData.getGameId()), 10, new ICallback() {
                @Override
                public void onSucess(Object obj) {
                    MyLog.d(TAG, "加入融云房间成功");
                }

                @Override
                public void onFailed(Object obj, int errcode, String message) {
                    MyLog.d(TAG, "加入融云房间失败");
                    joinRcRoom(deep + 1);
                }
            });
        }
    }

    private void ensureInRcRoom() {
        mUiHandler.removeMessages(MSG_ENSURE_IN_RC_ROOM);
        mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_IN_RC_ROOM, 30 * 1000);
    }

    private void pretenSystemMsg() {
        CommentSysModel commentSysModel = new CommentSysModel(mRoomData.getGameType(), "欢迎进入撕歌排位赛，对局马上开始，比赛过程发现坏蛋请用力举报哦～");
        EventBus.getDefault().post(new PretendCommentMsgEvent(commentSysModel));
    }

    private void pretenAiJudgeMsg() {
        PlayerInfoModel ai = mRoomData.getAiJudgeInfo();
        if (ai != null) {
            CommentAiModel commentAiModel = new CommentAiModel(ai, "已就位,参与本局演唱评价，比赛正式开始");
            EventBus.getDefault().post(new PretendCommentMsgEvent(commentAiModel));
        }
    }

    private void pretenEnterRoom(PlayerInfoModel playerInfoModel, int index) {
        CommentTextModel commentTextModel = new CommentTextModel();
        commentTextModel.setUserId(playerInfoModel.getUserInfo().getUserId());
        commentTextModel.setAvatar(playerInfoModel.getUserInfo().getAvatar());
        commentTextModel.setUserName(playerInfoModel.getUserInfo().getNicknameRemark());
        commentTextModel.setAvatarColor(Color.WHITE);
        SpannableStringBuilder ssb = new SpanUtils()
                .append(playerInfoModel.getUserInfo().getNicknameRemark() + " ").setForegroundColor(TEXT_YELLOW)
                .append(String.format("第%s个唱", index + 1)).setForegroundColor(TEXT_WHITE)
                .create();
        commentTextModel.setStringBuilder(ssb);
        EventBus.getDefault().post(new PretendCommentMsgEvent(commentTextModel));
    }

    @Override
    public void start() {
        super.start();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mRoomData.checkRoundInEachMode();
        startSyncGameStateTask(sSyncStateTaskInterval);
        ensureInRcRoom();
    }

    @Override
    public void destroy() {
        super.destroy();
        cancelHeartBeatTask("destroy");
        cancelSyncGameStateTask();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        mUiHandler.removeCallbacksAndMessages(null);
        if (mExoPlayer != null) {
            mExoPlayer.release();
            mExoPlayer = null;
        }
        ChatRoomMsgManager.getInstance().removeFilter(mPushMsgFilter);
        if (!mRoomData.hasGoVoiceRoom()) {
            exitGame();
            ModuleServiceManager.getInstance().getMsgService().leaveChatRoom(String.valueOf(mRoomData.getGameId()));
            ZqEngineKit.getInstance().destroy("rankingroom");
        } else {
            MyLog.w(TAG, "跳转到语音房，暂时不退出融云聊天室");
        }
    }

    /**
     * 上报轮次结束信息
     */
    public void sendRoundOverInfo(String reason) {
        MyLog.w(TAG, "上报我的演唱结束 reason:" + reason);
        estimateOverTsThisRound();

        // TODO: 2018/12/27 机器评分先写死，都给90分
        long timeMs = System.currentTimeMillis();
        int sysScore = 0;
        if (mRobotScoreHelper != null) {
            sysScore = mRobotScoreHelper.getAverageScore();
        }
        String sign = U.getMD5Utils().MD5_32("skrer|" +
                String.valueOf(mRoomData.getGameId()) + "|" +
                String.valueOf(sysScore) + "|" +
                String.valueOf(timeMs));

        RankRoundInfoModel infoModel = RoomDataUtils.getRoundInfoByUserId(mRoomData, (int) MyUserInfoManager.getInstance().getUid());
        if (infoModel == null) {
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());
        map.put("sysScore", sysScore);
        map.put("timeMs", timeMs);
        map.put("sign", sign);
        // 提前获取roundSeq，如果在result里在获取，可能是下下一个了，如果提前收到轮次变化的push
        map.put("roundSeq", infoModel.getRoundSeq());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.sendRoundOver(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    MyLog.w(TAG, "演唱结束上报成功 traceid is " + result.getTraceId());
                    // TODO: 2019/1/22  可能带回来游戏结束的信息，后期优化
                    // 尝试自己切换到下一个轮次
//                    if (finalRoundSeq >= 0) {
//                        RoundInfoModel roundInfoModel = RoomDataUtils.findRoundInfoBySeq(mRoomData.getRoundInfoModelList(), finalRoundSeq + 1);
//                        mRoomData.setExpectRoundInfo(roundInfoModel);
//                        mRoomData.checkRound();
//                    }
                } else {
                    MyLog.w(TAG, "演唱结束上报失败 traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.w(TAG, "sendRoundOverInfo" + " error " + e);
            }
        }, this);
    }

    /**
     * 上报我的轮次得分信息
     */
    public void sendRoundScoreInfo(int myRoundSeq) {
        MyLog.w(TAG, "上报我的轮次得分");
        estimateOverTsThisRound();
        long timeMs = System.currentTimeMillis();
        int sysScore = 0;
        if (mRobotScoreHelper != null) {
            sysScore = mRobotScoreHelper.getAverageScore();
        }
        String sign = U.getMD5Utils().MD5_32("skrer|" +
                String.valueOf(mRoomData.getGameId()) + "|" +
                String.valueOf(sysScore) + "|" +
                String.valueOf(timeMs));

        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());
        map.put("sysScore", sysScore);
        map.put("timeMs", timeMs);
        map.put("sign", sign);
        // 提前获取roundSeq，如果在result里在获取，可能是下下一个了，如果提前收到轮次变化的push
        map.put("roundSeq", myRoundSeq);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.sendRoundScore(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    MyLog.w(TAG, "演唱结束上报总分成功 traceid is " + result.getTraceId());
                    // TODO: 2019/1/22  可能带回来游戏结束的信息，后期优化
                    // 尝试自己切换到下一个轮次
//                    if (finalRoundSeq >= 0) {
//                        RoundInfoModel roundInfoModel = RoomDataUtils.findRoundInfoBySeq(mRoomData.getRoundInfoModelList(), finalRoundSeq + 1);
//                        mRoomData.setExpectRoundInfo(roundInfoModel);
//                        mRoomData.checkRound();
//                    }
                } else {
                    MyLog.w(TAG, "演唱结束上报总分失败 traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.w(TAG, "sendRoundScoreInfo error " + e);
            }
        }, this);
    }

    /**
     * 退出游戏
     */
    public void exitGame() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.exitGame(body), null);
    }

    /**
     * 游戏切后台或切回来
     *
     * @param out 切出去
     * @param in  切回来
     */
    public void swapGame(boolean out, boolean in) {
        MyLog.w(TAG, "swapGame" + " out=" + out + " in=" + in);
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());
        if (out) {
            map.put("status", SwapStatusType.SS_SWAP_OUT);
        } else if (in) {
            map.put("status", SwapStatusType.SS_SWAP_IN);
        }


        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.swap(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
//                    U.getToastUtil().showShort("切换请求发送成功");
                } else {
                    MyLog.e(TAG, "swapGame result errno is " + result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, "swapGame error " + e);
            }
        }, this);
    }

    /**
     * 心跳相关
     */
    public void startHeartBeatTask() {
        cancelHeartBeatTask("startHeartBeatTask");
        mHeartBeatTask = Observable
                .interval(0, sHeartBeatTaskInterval, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        sendHeartBeat();
                    }
                });
    }

    public void cancelHeartBeatTask(String from) {
        MyLog.w(TAG, "取消心跳，是从 " + from);
        if (mHeartBeatTask != null && !mHeartBeatTask.isDisposed()) {
            mHeartBeatTask.dispose();
        }
    }

    // 上报心跳，只有当前演唱者上报 2s一次
    public void sendHeartBeat() {
        MyLog.w(TAG, "sendHeartBeat");
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());
        map.put("userID", MyUserInfoManager.getInstance().getUid());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.sendHeartBeat(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    // TODO: 2018/12/13  当前postman返回的为空 待补充
                    MyLog.w(TAG, "心跳ok, traceid is " + result.getTraceId());
                } else {
                    MyLog.w(TAG, "心跳失败 traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.w(TAG, "心跳错误" + e);
            }
        }, this);
    }

    /**
     * 轮询同步状态task
     */
    public void startSyncGameStateTask(long delayTime) {
        cancelSyncGameStateTask();

        if (mRoomData.isIsGameFinish()) {
            MyLog.w(TAG, "游戏结束了，还特么Sync");
            return;
        }

        mSyncGameStateTask = Observable
                .interval(delayTime, sSyncStateTaskInterval, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        MyLog.w(TAG, "12秒钟的 syncGameTask 去更新状态了");
                        syncGameStatus(mRoomData.getGameId());
                    }
                }, throwable -> MyLog.w(TAG, throwable + ""));
    }

    public void cancelSyncGameStateTask() {
        if (mSyncGameStateTask != null && !mSyncGameStateTask.isDisposed()) {
            mSyncGameStateTask.dispose();
        }
    }

    // 同步游戏详情状态(检测不到长连接调用)
    public void syncGameStatus(int gameID) {
        ApiMethods.subscribe(mRoomServerApi.syncGameStatus(gameID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {

                    long syncStatusTimes = result.getData().getLongValue("syncStatusTimeMs");  //状态同步时的毫秒时间戳
                    long gameOverTimeMs = result.getData().getLongValue("gameOverTimeMs");  //游戏结束时间
                    List<OnlineInfoModel> onlineInfos = JSON.parseArray(result.getData().getString("onlineInfo"), OnlineInfoModel.class); //在线状态

                    RankRoundInfoModel currentInfo = JSON.parseObject(result.getData().getString("currentRound"), RankRoundInfoModel.class); //当前轮次信息
                    RankRoundInfoModel nextInfo = JSON.parseObject(result.getData().getString("nextRound"), RankRoundInfoModel.class); //下个轮次信息

                    String msg = "";
                    if (currentInfo != null) {
                        msg = "syncGameStatus成功了, currentRound 是 " + currentInfo.getUserID();
                    } else {
                        msg = "syncGameStatus成功了, currentRound 是 null";
                    }

                    if (nextInfo != null) {
                        msg = msg + ", nextRound 是 " + nextInfo.getUserID();
                    } else {
                        msg = msg + ", nextRound 是 null";
                    }

                    msg = msg + ",traceid is " + result.getTraceId();
                    MyLog.w(TAG, msg);

                    if (currentInfo == null && nextInfo == null) {
                        cancelSyncGameStateTask();
                        recvGameOverFromServer("syncGameStatus", gameOverTimeMs);
                        return;
                    }
                    updatePlayerState(gameOverTimeMs, syncStatusTimes, onlineInfos, currentInfo, nextInfo);
                } else {
                    MyLog.w(TAG, "syncGameStatus失败 traceid is " + result.getTraceId());
                    estimateOverTsThisRound();
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.w(TAG, "syncGameStatus失败了，errno是" + e);
            }
        }, this);
    }

    /**
     * pk灭灯等
     */
    public void pklightOff() {
        MyLog.d(TAG, "pklightOff");
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());
        map.put("roundSeq", mRoomData.getRealRoundSeq());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.pklightOff(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    // TODO: 2019/2/21 灭灯成功
                    MyLog.w(TAG, "灭灯ok, traceid is " + result.getTraceId());
                } else {
                    MyLog.w(TAG, "灭灯失败, traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.w(TAG, "灭灯失败, errno is " + e);
            }
        }, this);
    }

    public void pkburst() {
        MyLog.d(TAG, "pkburst");
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());
        map.put("roundSeq", mRoomData.getRealRoundSeq());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.pkburst(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    // TODO: 2019/2/21 灭灯成功
                    MyLog.w(TAG, "爆灯ok, traceid is " + result.getTraceId());
                } else {
                    MyLog.w(TAG, "爆灯失败, traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.w(TAG, "爆灯失败, errno is " + e);
            }
        }, this);
    }

    /**
     * 根据时间戳更新选手状态,目前就只有两个入口，SyncStatusEvent push了sycn，不写更多入口
     */
    private synchronized void updatePlayerState(long gameOverTimeMs, long syncStatusTimes, List<OnlineInfoModel> onlineInfos, RankRoundInfoModel currentInfo, RankRoundInfoModel nextInfo) {
        MyLog.w(TAG, "updatePlayerState" + " gameOverTimeMs=" + gameOverTimeMs + " syncStatusTimes=" + syncStatusTimes + " onlineInfos=" + onlineInfos + " currentInfo=" + currentInfo + " nextInfo=" + nextInfo);
        if (syncStatusTimes > mRoomData.getLastSyncTs()) {
            mRoomData.setLastSyncTs(syncStatusTimes);
            if (onlineInfos != null) {
                for (OnlineInfoModel onlineInfoModel : onlineInfos) {
                    mRoomData.setOnline(onlineInfoModel.getUserID(), onlineInfoModel.isIsOnline());
                }
            }
            mIGameRuleView.updateUserState(onlineInfos);
        }
        if (gameOverTimeMs != 0) {
            if (gameOverTimeMs > mRoomData.getGameStartTs()) {
                MyLog.w(TAG, "gameOverTimeMs ！= 0 游戏应该结束了");
                // 游戏结束了
                recvGameOverFromServer("sync", gameOverTimeMs);
            } else {
                MyLog.w(TAG, "服务器结束时间不合法 startTs:" + mRoomData.getGameStartTs() + " overTs:" + gameOverTimeMs);
            }
        } else {
            // 没结束 current 不应该为null
            if (currentInfo != null) {
                // 服务下发的轮次已经大于当前轮次了，说明本地信息已经不对了，更新
                if (RoomDataUtils.roundSeqLarger(currentInfo, mRoomData.getRealRoundInfo())) {
                    MyLog.w(TAG, "updatePlayerState" + " sync发现本地轮次信息滞后，更新");
                    // 轮次确实比当前的高，可以切换
                    mRoomData.setExpectRoundInfo(RoomDataUtils.getRoundInfoFromRoundInfoListInRankMode(mRoomData, currentInfo));
                    mRoomData.checkRoundInEachMode();
                } else if (RoomDataUtils.roundInfoEqual(currentInfo, mRoomData.getRealRoundInfo())) {
                    // TODO: 2019/2/21 更新本次round的数据
                    if (mRoomData.getRealRoundInfo() != null) {
                        mRoomData.getRealRoundInfo().tryUpdateRoundInfoModel(currentInfo, true);
                    }
                }
            } else {
                MyLog.w(TAG, "服务器结束时间不合法 currentInfo=null");
            }
        }
    }

    /**
     * 轮次信息有更新
     * 核心事件
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(RankRoundInfoChangeEvent event) {
        MyLog.w(TAG, "开始切换唱将 myturn=" + event.myturn);
        estimateOverTsThisRound();
        //以防万一
        tryStopRobotPlay();
        mUiHandler.removeMessages(MSG_START_LAST_TWO_SECONDS_TASK);
        mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS);
        if (event.myturn) {
            // 轮到我唱了
            // 开始发心跳
            if (U.getActivityUtils().isAppForeground()) {
                startHeartBeatTask();
            }

            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    // 开始倒计时 3 2 1
                    mIGameRuleView.startSelfCountdown(event.getLastRoundInfoModel(), new Runnable() {
                        @Override
                        public void run() {
                            //再次确认
                            if (mRoomData.getRealRoundInfo() != null && mRoomData.getRealRoundInfo().getUserID() == MyUserInfoManager.getInstance().getUid()) {
                                ZqEngineKit.getInstance().setClientRole(true);
                                // 等待角色变换成功回调
                                Message msg = mUiHandler.obtainMessage(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS);
                                mUiHandler.sendMessageDelayed(msg, 2000);
                            }
                        }
                    });
                    if (mRoomData.getRealRoundInfo() != null) {
                        MyLog.w(TAG, "演唱的时间是：" + U.getDateTimeUtils().formatTimeStringForDate(mRoomData.getGameStartTs() + mRoomData.getRealRoundInfo().getSingBeginMs(), "HH:mm:ss:SSS")
                                + "--" + U.getDateTimeUtils().formatTimeStringForDate(mRoomData.getGameStartTs() + mRoomData.getRealRoundInfo().getSingEndMs(), "HH:mm:ss:SSS"));
                    }
                }
            });
        } else {
            MyLog.w(TAG, "不是我的轮次，停止发心跳，停止混音，闭麦");

            cancelHeartBeatTask("切换唱将");

            if (RoomDataUtils.isMyRound(event.getLastRoundInfoModel())) {
                if (SkrConfig.getInstance().isNeedUploadAudioForAI(GameModeType.GAME_MODE_CLASSIC_RANK)) {
                    //属于需要上传音频文件的状态
                    // 上一轮是我的轮次，暂停录音
                    ZqEngineKit.getInstance().stopAudioRecording();
                    BaseRoundInfoModel myRoundInfoModel = event.getLastRoundInfoModel();
                    if (mRobotScoreHelper != null && mRobotScoreHelper.isScoreEnough()) {
                        myRoundInfoModel.setSysScore(mRobotScoreHelper.getAverageScore());
                        uploadRes1ForAi(myRoundInfoModel);
                    }
                }
                // 上报分数
                sendRoundScoreInfo(event.getLastRoundInfoModel().getRoundSeq());
            }

            ZqEngineKit.getInstance().stopAudioMixing();
            ZqEngineKit.getInstance().setClientRole(false);
//            ZqEngineKit.getInstance().muteLocalAudioStream(true);
            // 收到其他的人onMute消息 开始播放其他人的歌的歌词，应该提前下载好
            if (mRoomData.getRealRoundInfo() != null) {
                // 其他人演唱
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int uid = RoomDataUtils.getUidOfRoundInfo(mRoomData.getRealRoundInfo());
                        PlayerInfoModel playerInfoModel = RoomDataUtils.getPlayerInfoById(mRoomData, uid);
                        String avatar = "";
                        if (playerInfoModel != null) {
                            avatar = playerInfoModel.getUserInfo().getAvatar();
                        }
                        mIGameRuleView.startRivalCountdown(event.getLastRoundInfoModel(), uid, avatar);
                        checkMachineUser(uid);
                        if (mRoomData.getRealRoundInfo() != null) {
                            MyLog.w(TAG, uid + "开始唱了，歌词走起,演唱的时间是：" + U.getDateTimeUtils().formatTimeStringForDate(mRoomData.getGameStartTs() + mRoomData.getRealRoundInfo().getSingBeginMs(), "HH:mm:ss:SSS")
                                    + "--" + U.getDateTimeUtils().formatTimeStringForDate(mRoomData.getGameStartTs() + (mRoomData.getRealRoundInfo() == null ? 0 : mRoomData.getRealRoundInfo().getSingEndMs()), "HH:mm:ss:SSS"));
                        } else {
                            MyLog.w(TAG, "mRoomData.getRealRoundInfo() 为空啊！！！！");
                        }
                        //mIGameRuleView.playLyric(RoomDataUtils.getPlayerSongInfoUserId(mRoomData.getPlayerInfoList(), uid), false);
                    }
                });
            } else if (mRoomData.getRealRoundInfo() == null) {
                if (mRoomData.getGameOverTs() > mRoomData.getGameStartTs()) {
                    // 取消轮询
                    cancelSyncGameStateTask();
                    // 游戏结束了,处理相应的ui逻辑
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            gameIsFinish();
                            mIGameRuleView.gameFinish();
                        }
                    });
                } else {
                    MyLog.w(TAG, "结束时间比开始时间小，不应该吧 startTs:" + mRoomData.getGameStartTs() + " overTs:" + mRoomData.getGameOverTs());
                }
            }
        }
    }

    private void robotSingBegin(RankPlayerInfoModel playerInfo) {
        String skrerUrl = playerInfo.getResourceInfoList().get(0).getAudioURL();
        String midiUrl = playerInfo.getResourceInfoList().get(0).getMidiURL();
        if (mRobotScoreHelper == null) {
            mRobotScoreHelper = new RobotScoreHelper();
        }
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) {
                mRobotScoreHelper.loadDataFromUrl(midiUrl, 0);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();

        if (mExoPlayer == null) {
            mExoPlayer = new ExoPlayer();
        }
        mExoPlayer.startPlay(skrerUrl);
        if (!ZqEngineKit.getInstance().getParams().isAllRemoteAudioStreamsMute()) {
            mExoPlayer.setVolume(1);
        } else {
            mExoPlayer.setVolume(0);
        }
        //直接播放歌词
        othersBeginSinging();
    }

    private void tryStopRobotPlay() {
        if (mExoPlayer != null) {
            mExoPlayer.reset();
        }
    }

    public void sendBurst(int seq) {
        BaseRoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
        if (roundInfoModel != null && roundInfoModel.getRoundSeq() == seq) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("gameID", mRoomData.getGameId());
            map.put("roundSeq", roundInfoModel.getRoundSeq());
            RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
            ApiMethods.subscribe(mRoomServerApi.pkburst(body), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        MyLog.w(TAG, "爆灯成功 traceid is " + result.getTraceId() + "，seq是" + roundInfoModel.getRoundSeq());
                        mRoomData.consumeBurstLightTimes(roundInfoModel);
                    } else {
                        MyLog.w(TAG, "爆灯上报失败 traceid is " + result.getTraceId() + "，seq是" + roundInfoModel.getRoundSeq());
                    }
                }

                @Override
                public void onError(Throwable e) {
                    MyLog.w(TAG, "burst" + " error " + e);
                }

                @Override
                public void onNetworkError(ErrorType errorType) {
                    MyLog.w(TAG, "burst" + " errorType " + errorType);
                }
            }, this);
        } else {
            MyLog.d(TAG, "爆灯，但不是本轮次，" + "seq=" + seq);
        }
    }

    public void sendLightOff(int seq) {
        BaseRoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
        if (roundInfoModel != null && roundInfoModel.getRoundSeq() == seq) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("gameID", mRoomData.getGameId());
            map.put("roundSeq", roundInfoModel.getRoundSeq());
            RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
            ApiMethods.subscribe(mRoomServerApi.pklightOff(body), new ApiObserver<ApiResult>() {

                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        MyLog.w(TAG, "灭灯成功 traceid is " + result.getTraceId() + "，seq是" + roundInfoModel.getRoundSeq());
                        mRoomData.consumeLightOffTimes(roundInfoModel);
                    } else {
                        MyLog.w(TAG, "灭灯上报失败 traceid is " + result.getTraceId() + "，seq是" + roundInfoModel.getRoundSeq());
                    }
                }

                @Override
                public void onError(Throwable e) {
                    MyLog.w(TAG, "lightOff" + " error " + e);
                }

                @Override
                public void onNetworkError(ErrorType errorType) {
                    MyLog.w(TAG, "lightOff" + " errorType " + errorType);
                }
            }, this);
        } else {
            MyLog.d(TAG, "灭灯，但不是本轮次，" + "seq=" + seq);
        }
    }

    private void checkMachineUser(long uid) {
        PlayerInfoModel playerInfo = RoomDataUtils.getPlayerInfoById(mRoomData, uid);
        if (playerInfo == null) {
            MyLog.w(TAG, "切换别人的时候PlayerInfo为空");
            return;
        }

        /**
         * 机器人
         */
        if (playerInfo.isSkrer()) {
            MyLog.d(TAG, "checkMachineUser" + " uid=" + uid + " is machine");
            //因为机器人没有逃跑，所以不需要加保护。这里的4000不写死，第一个人应该是7000
            BaseRoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
            long delayTime = 4000l;
            //第一个人如果是机器人，需要deley6秒
            if (roundInfoModel.getRoundSeq() == 1) {
                delayTime = 6000l;
            }
            //移除之前的要发生的机器人演唱
            mUiHandler.removeMessages(MSG_ROBOT_SING_BEGIN);
            Message message = mUiHandler.obtainMessage(MSG_ROBOT_SING_BEGIN);
            message.obj = playerInfo;
            mUiHandler.sendMessageDelayed(message, delayTime);
        }
    }

    /**
     * 游戏真的结束了,游戏的结束可能是Sync接口和Sync push，还有收到RoundAndGameOver push
     */
    private void gameIsFinish() {
        mRoomData.setIsGameFinish(true);
        cancelHeartBeatTask("gameIsFinish");
        cancelSyncGameStateTask();
        // 游戏结束，直接关闭引擎，节省计费
        ZqEngineKit.getInstance().destroy("rankingroom");
        EventBus.getDefault().isRegistered(this);
    }

    private void recvGameOverFromServer(String from, long gameOverTs) {
        MyLog.w(TAG, "游戏结束 gameOverTs=" + gameOverTs + " from:" + from);
        if (gameOverTs > mRoomData.getGameStartTs() && gameOverTs > mRoomData.getGameOverTs()) {
            mRoomData.setGameOverTs(gameOverTs);
            mRoomData.setExpectRoundInfo(null);
            mRoomData.checkRoundInEachMode();
        }
    }

    public void muteAllRemoteAudioStreams(boolean mute, boolean fromUser) {
        if (fromUser) {
            mRoomData.setMute(mute);
        }
        ZqEngineKit.getInstance().muteAllRemoteAudioStreams(mute);
        // 如果是机器人的话
        if (mute) {
            // 如果是静音
            if (mExoPlayer != null) {
                mExoPlayer.setVolume(0);
            }
        } else {
            // 如果打开静音
            if (mExoPlayer != null) {
                mExoPlayer.setVolume(1f);
            }
        }
    }

    /**
     * 上传音频文件用作机器人
     *
     * @param roundInfoModel
     */
    private void uploadRes1ForAi(BaseRoundInfoModel roundInfoModel) {
        if (mRobotScoreHelper != null && mRobotScoreHelper.vilid()) {
            UploadParams.newBuilder(RoomDataUtils.getSaveAudioForAiFilePath())
                    .setFileType(UploadParams.FileType.audioAi)
                    .startUploadAsync(new UploadCallback() {
                        @Override
                        public void onProgress(long currentSize, long totalSize) {

                        }

                        @Override
                        public void onSuccess(String url) {
                            uploadRes2ForAi(roundInfoModel, url);
                        }

                        @Override
                        public void onFailure(String msg) {

                        }
                    });
        }
    }

    /**
     * 上传打分文件用作机器人
     *
     * @param roundInfoModel
     * @param audioUrl
     */
    private void uploadRes2ForAi(BaseRoundInfoModel roundInfoModel, String audioUrl) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                String path = RoomDataUtils.getSaveMatchingSocreForAiFilePath();
                if (mRobotScoreHelper != null) {
                    mRobotScoreHelper.save(path);
                    UploadParams.newBuilder(path)
                            .setFileType(UploadParams.FileType.midiAi)
                            .startUploadAsync(new UploadCallback() {
                                @Override
                                public void onProgress(long currentSize, long totalSize) {

                                }

                                @Override
                                public void onSuccess(String url) {
                                    sendUploadRequest(roundInfoModel, audioUrl, url);
                                }

                                @Override
                                public void onFailure(String msg) {

                                }
                            });
                }
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }

    /**
     * 上传机器人资源相关文件到服务器
     *
     * @param roundInfoModel
     * @param audioUrl
     * @param midiUrl
     */
    private void sendUploadRequest(BaseRoundInfoModel roundInfoModel, String audioUrl, String midiUrl) {
        long timeMs = System.currentTimeMillis();
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());
        map.put("itemID", roundInfoModel.getPlaybookID());
        map.put("sysScore", roundInfoModel.getSysScore());
        map.put("audioURL", audioUrl);
        map.put("midiURL", midiUrl);
        map.put("timeMs", timeMs);
        StringBuilder sb = new StringBuilder();
        sb.append("skrer")
                .append("|").append(mRoomData.getGameId())
                .append("|").append(roundInfoModel.getPlaybookID())
                .append("|").append(roundInfoModel.getSysScore())
                .append("|").append(audioUrl)
                .append("|").append(midiUrl)
                .append("|").append(timeMs);
        String sign = U.getMD5Utils().MD5_32(sb.toString());
        map.put("sign", sign);
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.putGameResource(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    MyLog.e(TAG, "sendAiUploadRequest success");
                } else {
                    MyLog.e(TAG, "sendAiUploadRequest failed， errno is " + result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, "sendUploadRequest error " + e);
            }
        }, this);
    }

    private int estimateOverTsThisRound() {
        int pt = RoomDataUtils.estimateTs2End(mRoomData, mRoomData.getRealRoundInfo());
        MyLog.w(TAG, "估算出距离本轮结束还有" + pt + "ms");
        return pt;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(AccBeginEvent event) {
        onUserSpeakFromEngine("AccBeginEvent", event.userId);
    }

    //服务器push，某人爆灯了
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(PkBurstLightMsgEvent event) {
        if (RoomDataUtils.isCurrentRunningRound(event.getpKBLightMsg().getRoundSeq(), mRoomData)) {
            MyLog.w(TAG, "有人爆灯了：userID " + event.getpKBLightMsg().getUserID() + ", seq " + event.getpKBLightMsg().getRoundSeq());
            RankRoundInfoModel roundInfoModel = (RankRoundInfoModel) mRoomData.getRealRoundInfo();

            BLightInfoModel bLightInfoModel = new BLightInfoModel();
            bLightInfoModel.setUserID(event.getpKBLightMsg().getUserID());
            bLightInfoModel.setTimeMs((int) event.getInfo().getTimeMs());
            bLightInfoModel.setSeq(event.getpKBLightMsg().getRoundSeq());

            roundInfoModel.addBrustLightUid(true, bLightInfoModel);
        } else {
            BaseRoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
            if (roundInfoModel != null && event.getpKBLightMsg().getRoundSeq() > roundInfoModel.getRoundSeq()) {
                // TODO: 2019/2/20  如果此次爆灯的round比现在的高，需要切换到下一个round或者sync
            }
            MyLog.w(TAG, "有人爆灯了,但是不是这个轮次：userID " + event.getpKBLightMsg().getUserID() + ", seq " + event.getpKBLightMsg().getRoundSeq() + "，当前轮次是 " + mRoomData.getRealRoundSeq());
        }
    }

    //服务器push，某人灭灯了
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(PkLightOffMsgEvent event) {
        if (RoomDataUtils.isCurrentRunningRound(event.getPKMLightMsg().getRoundSeq(), mRoomData)) {
            MyLog.w(TAG, "有人灭灯了：userID " + event.getPKMLightMsg().getUserID() + ", seq " + event.getPKMLightMsg().getRoundSeq());
            RankRoundInfoModel roundInfoModel = (RankRoundInfoModel) mRoomData.getRealRoundInfo();

            MLightInfoModel mLightInfoModel = new MLightInfoModel();
            mLightInfoModel.setUserID(event.getPKMLightMsg().getUserID());
            mLightInfoModel.setTimeMs((int) event.getInfo().getTimeMs());
            mLightInfoModel.setSeq(event.getPKMLightMsg().getRoundSeq());

            roundInfoModel.addPkLightOffUid(true, mLightInfoModel);
        } else {
            BaseRoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
            if (roundInfoModel != null && event.getPKMLightMsg().getRoundSeq() > roundInfoModel.getRoundSeq()) {
                // TODO: 2019/2/20  如果此次灭灯的round比现在的高，需要切换到下一个round或者sync
            }
            MyLog.w(TAG, "有人灭灯了,但是不是这个轮次：userID " + event.getPKMLightMsg().getUserID() + ", seq " + event.getPKMLightMsg().getRoundSeq() + "，当前轮次是 " + mRoomData.getRealRoundSeq());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(EngineEvent event) {
        if (event.getType() == EngineEvent.TYPE_USER_JOIN) {
            int userId = event.getUserStatus().getUserId();
            onUserSpeakFromEngine("TYPE_USER_JOIN", userId);
        } else if (event.getType() == EngineEvent.TYPE_USER_ROLE_CHANGE) {
            EngineEvent.RoleChangeInfo roleChangeInfo = event.getObj();
            if (roleChangeInfo.getNewRole() == 1) {
                onChangeBroadcastSuccess();
            }
        } else if (event.getType() == EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION) {
//            if (RoomDataUtils.isMyRound(mRoomData.getRealRoundInfo())) {
//                if (event.getObj() != null) {
//                    List<EngineEvent.UserVolumeInfo> list = (List<EngineEvent.UserVolumeInfo>) event.getObj();
//                    for (EngineEvent.UserVolumeInfo info : list) {
//
//                        if (info.getUid() == UserAccountManager.getInstance().getUuidAsLong()
//                                || info.getUid() == 0) {
//
//                            break;
//                        }
//                    }
//                }
//            }
        } else if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_FINISH) {
            //伴奏播放结束了也发结束轮次的通知了
            sendRoundOverInfo("TYPE_MUSIC_PLAY_FINISH");
        } else if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_TIME_FLY_LISTENER) {
            EngineEvent.MixMusicTimeInfo timeInfo = (EngineEvent.MixMusicTimeInfo) event.getObj();
            //这个是唱的时间，先在按长度算时间
            if (timeInfo.getCurrent() >= mRoomData.getSongModel().getEndMs() - mRoomData.getSongModel().getBeginMs()) {
                //可以发结束轮次的通知了
                sendRoundOverInfo("TYPE_MUSIC_PLAY_TIME_FLY_LISTENER");
            }
        } else if (event.getType() == EngineEvent.TYPE_USER_MUTE_AUDIO) {
            int muteUserId = event.getUserStatus().getUserId();
            if (!event.getUserStatus().isAudioMute()) {
                MyLog.w(TAG, "EngineEvent muteUserId=" + muteUserId + "解麦了");
                onUserSpeakFromEngine("TYPE_USER_MUTE_AUDIO", muteUserId);
            } else {
                /**
                 * 有人闭麦了，可以考虑加个逻辑，如果闭麦的人是当前演唱的人
                 * 说明此人演唱结束，可以考虑进入下一轮
                 */
                MyLog.w(TAG, "引擎监测到有人有人闭麦了，id是" + muteUserId);
            }
        }
    }

    /**
     * 成功切换为主播
     */
    private void onChangeBroadcastSuccess() {
        mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS);
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                RankRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
                if (infoModel == null || infoModel.getUserID() != MyUserInfoManager.getInstance().getUid()) {
                    MyLog.d(TAG, "onChangeBroadcastSuccess,但已经不是你的轮次了，cancel");
                    return;
                }
                // 开始开始混伴奏，开始解除引擎mute
                File accFile = SongResUtils.getAccFileByUrl(mRoomData.getSongModel().getAcc());
                File midiFile = SongResUtils.getMIDIFileByUrl(mRoomData.getSongModel().getMidi());

                if (accFile != null && accFile.exists()) {
//                                    ZqEngineKit.getInstance().muteLocalAudioStream(false);
                    ZqEngineKit.getInstance().startAudioMixing((int) MyUserInfoManager.getInstance().getUid(), accFile.getAbsolutePath()
                            , midiFile == null ? "" : midiFile.getAbsolutePath(), mRoomData.getSongModel().getBeginMs(), false, false, 1);
                    /**
                     * 现在歌儿都是截断过的，getSingBeginMs和getSingEndMs是歌词的时间，伴奏从0位置开始播放
                     */
                    ZqEngineKit.getInstance().setAudioMixingPosition(0);
                    // 还应开始播放歌词
                    mIGameRuleView.playLyric(mRoomData.getSongModel());
                    mIGameRuleView.showLeftTime(infoModel.getSingEndMs() - infoModel.getSingBeginMs());
                    MyLog.w(TAG, "本人开始唱了，歌词和伴奏响起");
                    mRoomData.setSingBeginTs(System.currentTimeMillis());
                    //开始录制声音
                    if (SkrConfig.getInstance().isNeedUploadAudioForAI(GameModeType.GAME_MODE_CLASSIC_RANK)) {
                        // 需要上传音频伪装成机器人
                        ZqEngineKit.getInstance().startAudioRecording(RoomDataUtils.getSaveAudioForAiFilePath(), Constants.AUDIO_RECORDING_QUALITY_HIGH);
                    }
                    mRobotScoreHelper = new RobotScoreHelper();
                    //尝试再用融云通知对端
                    sendUserSpeakEventToOthers();
                } else {
                    MyLog.e(TAG, "onChangeBroadcastSuccess acc 文件不存在，什么情况？");
                }
            }
        });
    }

    private void sendUserSpeakEventToOthers() {
        IMsgService msgService = ModuleServiceManager.getInstance().getMsgService();
        if (msgService != null) {
            long ts = System.currentTimeMillis();
            UserInfo senderInfo = new UserInfo.Builder()
                    .setUserID((int) MyUserInfoManager.getInstance().getUid())
                    .setNickName(MyUserInfoManager.getInstance().getNickName())
                    .setAvatar(MyUserInfoManager.getInstance().getAvatar())
                    .setSex(ESex.fromValue(MyUserInfoManager.getInstance().getSex()))
                    .setDescription("")
                    .setIsSystem(false)
                    .build();

            RoomMsg roomMsg = new RoomMsg.Builder()
                    .setTimeMs(ts)
                    .setMsgType(ERoomMsgType.RM_ROUND_ACC_BEGIN)
                    .setRoomID(mRoomData.getGameId())
                    .setNo(ts)
                    .setPosType(EMsgPosType.EPT_UNKNOWN)
                    .setSender(senderInfo)
                    .build();

            String contnet = U.getBase64Utils().encode(roomMsg.toByteArray());
            msgService.sendChatRoomMessage(String.valueOf(mRoomData.getGameId()), CustomMsgType.MSG_TYPE_ROOM, contnet, null);
        }
    }

    private void onUserSpeakFromEngine(String from, int muteUserId) {
        MyLog.w(TAG, "onUserSpeakFromEngine muteUserId=" + muteUserId + "解麦了,from:" + from);
        RankRoundInfoModel infoModel = RoomDataUtils.getRoundInfoByUserId(mRoomData, muteUserId);
        if (infoModel != null && infoModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
            MyLog.d(TAG, "onUserSpeakFromEngine" + " 解麦的是本人，忽略");
            return;
        }
        /**
         * 用户开始解开mute了，说明某个用户自己认为轮到自己唱了
         * 这里考虑下要不要加个判断，如果当前轮次是这个用户，才播放他的歌词
         * 就是是自己状态对，还是别人状态对的问题，这里先认为自己状态对.
         * 状态依赖服务器
         */
        if (infoModel != null && infoModel.getUserID() != MyUserInfoManager.getInstance().getUid()) {
            if (RoomDataUtils.roundInfoEqual(infoModel, mRoomData.getRealRoundInfo())) {
                //正好相等，没问题,放歌词
                MyLog.w(TAG, "是当前轮次，没问题,放歌词");
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyLog.d(TAG, "引擎监测到有人开始唱了，正好是当前的人，播放歌词 这个人的id是" + muteUserId);
                        othersBeginSinging();
                    }
                });
            } else if (RoomDataUtils.roundSeqLarger(infoModel, mRoomData.getExpectRoundInfo())) {
                // 假设演唱的轮次在当前轮次后面，说明本地滞后了
                MyLog.w(TAG, "演唱的轮次在当前轮次后面，说明本地滞后了,矫正并放歌词");
                // 直接设置最新轮次，什么专场动画都不要了，都异常了，还要这些干嘛
                mRoomData.setExpectRoundInfo(infoModel);
                mRoomData.checkRoundInEachMode();
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyLog.w(TAG, "引擎监测到有人开始唱了，演唱的轮次在当前轮次后面，说明本地滞后了,矫正并放歌词  这个人的id是" + muteUserId);
                        othersBeginSinging();
                    }
                });
            }
        } else {
            MyLog.w(TAG, "引擎监测到有人开始唱了， 找不到该人的轮次信息？？？为什么？？？");
        }
    }

    /**
     * 其他用户真正开始演唱了
     */
    private void othersBeginSinging() {
        BaseRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel != null && !infoModel.isHasSing()) {
            infoModel.setHasSing(true);
            mIGameRuleView.showLeftTime(infoModel.getSingEndMs() - infoModel.getSingBeginMs());
            //mIGameRuleView.playLyric(RoomDataUtils.getPlayerSongInfoUserId(mRoomData.getPlayerInfoList(), infoModel.getUserID()), true);
            mIGameRuleView.onOtherStartSing(RoomDataUtils.getPlayerSongInfoUserId(mRoomData.getPlayerInfoList(), infoModel.getUserID()));
            startLastTwoSecondTask();
        }
    }

    private void startLastTwoSecondTask() {
        final BaseRoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
        if (roundInfoModel != null) {
            mUiHandler.removeMessages(MSG_START_LAST_TWO_SECONDS_TASK);
            Message message = mUiHandler.obtainMessage(MSG_START_LAST_TWO_SECONDS_TASK);
            message.obj = roundInfoModel;
            mUiHandler.sendMessageDelayed(message, roundInfoModel.getSingEndMs() - roundInfoModel.getSingBeginMs() - 2000);
        } else {
            MyLog.e(TAG, "startLastTwoSecondTask roundInfoModel 为 null, 为什么？？？？");
        }
    }

    /**
     * 提前告诉对手总分，因为有可能对手在第一句没唱完之前就爆灯了
     *
     * @param lineNum
     */
    public void sendTotalScoreToOthers(int lineNum) {
        MachineScoreItem machineScoreItem = new MachineScoreItem();
        machineScoreItem.setScore(999);// 与ios约定，如果传递是分数是999就代表只是想告诉这首歌的总分
        long ts = ZqEngineKit.getInstance().getAudioMixingCurrentPosition();
        machineScoreItem.setTs(ts);
        machineScoreItem.setNo(lineNum);
        sendScoreToOthers(machineScoreItem);
    }

    /**
     * 将自己的分数传给其他人
     *
     * @param machineScoreItem
     */
    private void sendScoreToOthers(MachineScoreItem machineScoreItem) {
        // 后续加个优化，如果房间里两人都是机器人就不加了
        IMsgService msgService = ModuleServiceManager.getInstance().getMsgService();
        if (msgService != null) {
            long ts = System.currentTimeMillis();
            UserInfo senderInfo = new UserInfo.Builder()
                    .setUserID((int) MyUserInfoManager.getInstance().getUid())
                    .setNickName(MyUserInfoManager.getInstance().getNickName())
                    .setAvatar(MyUserInfoManager.getInstance().getAvatar())
                    .setSex(ESex.fromValue(MyUserInfoManager.getInstance().getSex()))
                    .setDescription("")
                    .setIsSystem(false)
                    .build();

            RoomMsg roomMsg = new RoomMsg.Builder()
                    .setTimeMs(ts)
                    .setMsgType(ERoomMsgType.RM_ROUND_MACHINE_SCORE)
                    .setRoomID(mRoomData.getGameId())
                    .setNo(ts)
                    .setPosType(EMsgPosType.EPT_UNKNOWN)
                    .setSender(senderInfo)
                    .setMachineScore(new MachineScore.Builder()
                            .setUserID((int) MyUserInfoManager.getInstance().getUid())
                            .setNo(machineScoreItem.getNo())
                            .setScore(machineScoreItem.getScore())
                            .setItemID(mRoomData.getSongModel().getItemID())
                            .setLineNum(mRoomData.getSongLineNum())
                            .build()
                    )
                    .build();
            String contnet = U.getBase64Utils().encode(roomMsg.toByteArray());
            msgService.sendChatRoomMessage(String.valueOf(mRoomData.getGameId()), CustomMsgType.MSG_TYPE_ROOM, contnet, null);
        }
    }

    // 游戏轮次结束的通知消息（在某人向服务器短连接成功后推送)
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventMainThread(RoundOverEvent event) {
        ensureInRcRoom();
        MyLog.w(TAG, "收到服务器的某一个人轮次结束的push，id是 " + event.currenRound.getUserID()
                + ", exitUserID 是 " + event.exitUserID + " timets 是" + event.info.getTimeMs());
        if (mRoomData.getLastSyncTs() > event.info.getTimeMs()) {
            MyLog.w(TAG, "但是是旧数据");
            return;
        }

        if (event.exitUserID != 0) {
            mRoomData.setOnline(event.exitUserID, false);
        }
        if (RoomDataUtils.isCurrentRunningRound(event.currenRound.getRoundSeq(), mRoomData)) {
            // 如果是当前轮次
            mRoomData.getRealRoundInfo().tryUpdateRoundInfoModel(event.currenRound, true);
        }
        // 游戏轮次结束
        if (RoomDataUtils.roundSeqLarger(event.nextRound, mRoomData.getRealRoundInfo())) {
            // 轮次确实比当前的高，可以切换
            MyLog.w(TAG, "轮次确实比当前的高，可以切换");
            mRoomData.setExpectRoundInfo(RoomDataUtils.getRoundInfoFromRoundInfoListInRankMode(mRoomData, event.nextRound));
            mRoomData.checkRoundInEachMode();
        }
    }

    //轮次和游戏结束通知，除了已经结束状态，别的任何状态都要变成
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventMainThread(RoundAndGameOverEvent event) {
        MyLog.w(TAG, "收到服务器的游戏结束的push timets 是 " + event.info.getTimeMs());

        MyLog.d(TAG, "onEventMainThread" + " roundAndGameOverEvent mVoteInfoModels =" + event.mVoteInfoModels);
        MyLog.d(TAG, "onEventMainThread" + " roundAndGameOverEvent mScoreResultModel =" + event.mScoreResultModel);
        MyLog.d(TAG, "onEventMainThread" + " roundAndGameOverEvent mUserGameResultModels =" + event.mUserGameResultModels);
        if (event.mExitUserID != 0) {
            mRoomData.setOnline(event.mExitUserID, false);
        }
        if (RoomDataUtils.isCurrentRunningRound(event.mRankRoundInfoModel.getRoundSeq(), mRoomData)) {
            mRoomData.getRealRoundInfo().tryUpdateRoundInfoModel(event.mRankRoundInfoModel, true);
        }

        RecordData recordData = new RecordData(event.mVoteInfoModels,
                event.mScoreResultModel,
                event.mUserGameResultModels);
        mRoomData.setRecordData(recordData);

        recvGameOverFromServer("push", event.roundOverTimeMs);
        cancelSyncGameStateTask();
    }

    // 应用进程切到后台通知
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AppSwapEvent appSwapEvent) {
        MyLog.w(TAG, "onEventMainThread syncStatusEvent");
    }

    // 长连接 状态同步信令， 以11秒为单位检测
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SyncStatusEvent syncStatusEvent) {
        ensureInRcRoom();
        String msg = "收到服务器更新状态的push了, currentRound 是 ";
        msg = msg + (syncStatusEvent.currentInfo == null ? "null" : syncStatusEvent.currentInfo.getUserID() + "");
        msg = msg + ", nextInfo 是 " + (syncStatusEvent.nextInfo == null ? "null" : syncStatusEvent.nextInfo.getUserID() + "");
        msg = msg + ", timeMs" + syncStatusEvent.info.getTimeMs();
        MyLog.w(TAG, msg);
//        startSyncGameStateTask(sSyncStateTaskInterval);

        updatePlayerState(syncStatusEvent.gameOverTimeMs, syncStatusEvent.syncStatusTimes, syncStatusEvent.onlineInfos, syncStatusEvent.currentInfo, syncStatusEvent.nextInfo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ExitGameEvent event) {
        MyLog.w(TAG, "收到一个人退出的push了，type是" + event.type + ",timeMs是" + event.info.getTimeMs());
        if (event.type == EXIT_GAME_AFTER_PLAY) {   //我在唱歌，有一个人退出
//            U.getToastUtil().showShort("游戏结束后，某一个人退出了");
        } else if (event.type == EXIT_GAME_OUT_ROUND) {   //我是观众，有一个人退出
//            U.getToastUtil().showShort("游戏中，某一个人退出了");
        }
        mRoomData.setOnline(event.exitUserID, false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(PkSomeOneOnlineChangeEvent event) {
        UserInfoModel userInfo = mRoomData.getUserInfo(event.model.getUserID());
        CommentSysModel commentSysModel = new CommentSysModel(userInfo.getNicknameRemark(), "偷偷溜走了");
        EventBus.getDefault().post(new PretendCommentMsgEvent(commentSysModel));

        if (event.model.getUserID() == MyUserInfoManager.getInstance().getUid()) {
            MyLog.d(TAG, "本人溜走了，需要关掉当前房间,id是" + event.model.getUserID());
            U.getToastUtil().showShort("您长时间不在对局状态，已自动退出对局啦");
            mIGameRuleView.finishActivity();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ActivityUtils.ForeOrBackgroundChange event) {
        MyLog.w(TAG, event.foreground ? "切换到前台" : "切换到后台");
        swapGame(!event.foreground, event.foreground);
        if (mRoomData.getRealRoundInfo() != null
                && mRoomData.getRealRoundInfo().getUserID() == MyUserInfoManager.getInstance().getUid()) {
            if (event.foreground) {
                startHeartBeatTask();
            } else {
                cancelHeartBeatTask("前后台切换");
            }
        }

        if (event.foreground) {
            muteAllRemoteAudioStreams(mRoomData.isMute(), false);
        } else {
            muteAllRemoteAudioStreams(true, false);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(LrcEvent.LineLineEndEvent event) {
        MyLog.d(TAG, "onEvent LineEndEvent lineno=" + event.lineNum);
        /**
         * 机器人的轮次
         */
        if (RoomDataUtils.isRobotRound(mRoomData.getRealRoundInfo(), mRoomData.getPlayerInfoList())) {
            // 尝试算机器人的演唱得分
            if (mRobotScoreHelper != null) {
                int score = mRobotScoreHelper.tryGetScoreByLine(event.lineNum);
                if (score >= 0) {
//                        U.getToastUtil().showShort("score:" + score);
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mIGameRuleView.updateScrollBarProgress(score, mRobotScoreHelper.tryGetTotalScoreByLine(event.lineNum), mRobotScoreHelper.tryGetScoreLineNum());
                        }
                    });
                }
            }
        } else {
            // 尝试拿其他人的演唱打分
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LyricAndAccMatchManager.ScoreResultEvent event) {
        int line = event.line;
        int acrScore = event.acrScore;
        int melpScore = event.melpScore;
        String from = event.from;
        if (melpScore > acrScore) {
            processScore(from, melpScore, line);
        } else {
            processScore(from, acrScore, line);
        }
    }

    void processScore(String from, int score, int line) {
        MyLog.d(TAG, "processScore" + " from=" + from + " score=" + score + " line=" + line);
        if (score < 0) {
            return;
        }
        MyLog.d(TAG, "onEvent" + " 得分=" + score);
        MachineScoreItem machineScoreItem = new MachineScoreItem();
        machineScoreItem.setScore(score);
        long ts = ZqEngineKit.getInstance().getAudioMixingCurrentPosition();
        machineScoreItem.setTs(ts);
        machineScoreItem.setNo(line);
        mRoomData.setCurSongTotalScore(mRoomData.getCurSongTotalScore() + score);
        // 打分信息传输给其他人
        sendScoreToOthers(machineScoreItem);
        if (mRobotScoreHelper != null) {
            mRobotScoreHelper.add(machineScoreItem);
        }
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mIGameRuleView.updateScrollBarProgress(score, mRoomData.getCurSongTotalScore(), mRoomData.getSongLineNum());
            }
        });
        //打分传给服务器
        sendScoreToServer(score, line);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MachineScoreEvent event) {
        ensureInRcRoom();
        //收到其他人的机器打分消息，比较复杂，暂时简单点，轮次正确就直接展示
        if (RoomDataUtils.isThisUserRound(mRoomData.getRealRoundInfo(), event.userId)) {
            mIGameRuleView.updateScrollBarProgress(event.score, event.totalScore, event.lineNum);
        } else {
            MyLog.d(TAG, "收到个分数，但不是当前演唱者的，event.userId=" + event.userId);
            if (mRoomData.getRealRoundInfo() != null) {
                MyLog.d(TAG, "收到个分数 但不是当前演唱者的 演唱者:" + mRoomData.getRealRoundInfo().getUserID());
            }
        }
    }

    /**
     * 单句打分上报
     *
     * @param score
     * @param line
     */
    public void sendScoreToServer(int score, int line) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());
        RankRoundInfoModel infoModel = RoomDataUtils.getRoundInfoByUserId(mRoomData, (int) MyUserInfoManager.getInstance().getUid());
        if (infoModel == null) {
            MyLog.d(TAG, "sendScoreToServer 但不是自己的轮次");
            return;
        }
        int itemID = infoModel.getPlaybookID();
        map.put("itemID", itemID);
        int mainLevel = 0;
        PlayerInfoModel playerInfoModel = RoomDataUtils.getPlayerInfoById(mRoomData, MyUserInfoManager.getInstance().getUid());
        if (playerInfoModel != null) {
            mainLevel = playerInfoModel.getUserInfo().getMainLevel();
        }
        map.put("mainLevel", mainLevel);
        map.put("no", line);
        int roundSeq = infoModel.getRoundSeq();
        map.put("roundSeq", roundSeq);
        map.put("score", score);
        long nowTs = System.currentTimeMillis();
        int singSecond = (int) ((nowTs - mRoomData.getSingBeginTs()) / 1000);
        map.put("singSecond", singSecond);
        map.put("timeMs", nowTs);
        map.put("userID", MyUserInfoManager.getInstance().getUid());
        StringBuilder sb = new StringBuilder();
        sb.append("skrer")
                .append("|").append(MyUserInfoManager.getInstance().getUid())
                .append("|").append(itemID)
                .append("|").append(score)
                .append("|").append(line)
                .append("|").append(mRoomData.getGameId())
                .append("|").append(mainLevel)
                .append("|").append(singSecond)
                .append("|").append(roundSeq)
                .append("|").append(nowTs);
        map.put("sign", U.getMD5Utils().MD5_32(sb.toString()));
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.sendPkPerSegmentResult(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    // TODO: 2018/12/13  当前postman返回的为空 待补充
                    MyLog.w(TAG, "单句打分上报成功");
                } else {
                    MyLog.w(TAG, "单句打分上报失败" + result.getErrno());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(e);
            }
        }, this);
    }
}
