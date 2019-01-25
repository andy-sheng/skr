package com.module.playways.rank.room.presenter;

import android.os.Handler;
import android.os.Message;
import android.support.v4.util.Pair;

import com.alibaba.fastjson.JSON;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.player.exoplayer.ExoPlayer;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.upload.UploadCallback;
import com.common.upload.UploadParams;
import com.common.utils.ActivityUtils;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.component.busilib.SkrConfig;
import com.engine.EngineEvent;
import com.engine.EngineManager;
import com.engine.Params;
import com.engine.arccloud.ArcRecognizeListener;
import com.engine.arccloud.RecognizeConfig;
import com.engine.arccloud.SongInfo;
import com.module.ModuleServiceManager;
import com.module.msg.CustomMsgType;
import com.module.msg.IMsgService;
import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.msg.event.AccBeginEvent;
import com.module.playways.rank.msg.event.AppSwapEvent;
import com.module.playways.rank.msg.event.CommentMsgEvent;
import com.module.playways.rank.msg.event.ExitGameEvent;
import com.module.playways.rank.msg.event.MachineScoreEvent;
import com.module.playways.rank.msg.event.RoundAndGameOverEvent;
import com.module.playways.rank.msg.event.RoundOverEvent;
import com.module.playways.rank.msg.event.SyncStatusEvent;
import com.module.playways.rank.msg.filter.PushMsgFilter;
import com.module.playways.rank.msg.manager.ChatRoomMsgManager;
import com.module.playways.rank.prepare.model.OnlineInfoModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.prepare.model.RoundInfoModel;
import com.module.playways.rank.room.RoomServerApi;
import com.module.playways.rank.room.SwapStatusType;
import com.module.playways.rank.room.event.RoundInfoChangeEvent;
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.RoomData;
import com.module.playways.RoomDataUtils;
import com.module.playways.rank.room.model.VoteInfoModel;
import com.module.playways.rank.room.model.WinResultModel;
import com.module.playways.rank.room.model.score.ScoreResultModel;
import com.module.playways.rank.room.score.MachineScoreItem;
import com.module.playways.rank.room.score.RobotScoreHelper;
import com.module.playways.rank.room.scoremodel.UserScoreModel;
import com.module.playways.rank.room.view.IGameRuleView;
import com.zq.live.proto.Common.ESex;
import com.zq.live.proto.Common.UserInfo;
import com.zq.live.proto.Room.EMsgPosType;
import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.MachineScore;
import com.zq.live.proto.Room.RoomMsg;
import com.zq.live.proto.Room.UserScoreResult;
import com.zq.lyrics.event.LrcEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.agora.rtc.Constants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.module.playways.rank.msg.event.ExitGameEvent.EXIT_GAME_AFTER_PLAY;
import static com.module.playways.rank.msg.event.ExitGameEvent.EXIT_GAME_OUT_ROUND;

public class RankCorePresenter extends RxLifeCyclePresenter {
    String TAG = "RankCorePresenter";

    static final int MSG_ROBOT_SING_BEGIN = 10;
    //    static final int MSG_ROBOT_SING_END = 11;
    static final int MSG_GET_VOTE = 20;

    static final int MSG_START_LAST_TWO_SECONDS_TASK = 30;

    static final int MSG_LYRIC_END_EVENT = 31;

    private static long sHeartBeatTaskInterval = 3000;
    private static long sSyncStateTaskInterval = 12000;

    RoomData mRoomData;

    RoomServerApi mRoomServerApi = ApiManager.getInstance().createService(RoomServerApi.class);

    Disposable mHeartBeatTask;

    Disposable mSyncGameStateTask;

    PublishSubject<RecordData> mGameFinishActionSubject = PublishSubject.create();

    /**
     * 服务器确定已结束，收到RoundAndGameOver事件
     */
    volatile boolean isConfirmRoundAndGameOver = false;

    IGameRuleView mIGameRuleView;

    RobotScoreHelper mRobotScoreHelper;

    ExoPlayer mExoPlayer;

    int mLastLineNum;

    PushMsgFilter mPushMsgFilter = new PushMsgFilter() {
        @Override
        public boolean doFilter(RoomMsg msg) {
            if (msg.roomID == mRoomData.getGameId()) {
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
                case MSG_ROBOT_SING_BEGIN:
                    robotSingBegin((PlayerInfoModel) msg.obj);
                    break;
//                case MSG_ROBOT_SING_END:
//                    break;
                case MSG_GET_VOTE:
                    MyLog.d(TAG, "handleMessage MSG_GET_VOTE");
                    Pair<Integer, Integer> pair = (Pair<Integer, Integer>) msg.obj;
                    getVoteResult(pair.first, pair.second);
                    break;
                case MSG_START_LAST_TWO_SECONDS_TASK:
                    RoundInfoModel roundInfoModel = (RoundInfoModel) msg.obj;
                    if (roundInfoModel != null && roundInfoModel == mRoomData.getRealRoundInfo()) {
                        mIGameRuleView.hideMainStage();
                    }
                    break;
            }

        }
    };

    public RankCorePresenter(@NotNull IGameRuleView iGameRuleView, @NotNull RoomData roomData) {
        mIGameRuleView = iGameRuleView;
        mRoomData = roomData;
        TAG += hashCode();

        MyLog.w(TAG, "player info is " + mRoomData.toString());

        mGameFinishActionSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RecordData>() {
                    @Override
                    public void accept(RecordData recordData) throws Exception {
                        if (recordData.mVoteInfoModels != null && recordData.mVoteInfoModels.size() > 0) {
                            MyLog.w(TAG, "accept 1");
                            //不需要跳转评论页,直接跳转战绩页
                            mIGameRuleView.showRecordView(new RecordData(recordData.mVoteInfoModels, recordData.mScoreResultModel, recordData.mWinResultModels));
                        } else {
                            MyLog.w(TAG, "accept 1");
                            mIGameRuleView.showVoteView();
                        }
                    }
                });
        if (mRoomData.getGameId() > 0) {
            Params params = Params.getFromPref();
            EngineManager.getInstance().init("rankingroom", params);
            boolean isAnchor = false;
//            if(RoomDataUtils.isMyRound(mRoomData.getRealRoundInfo())){
//                isAnchor = true;
//            }
            EngineManager.getInstance().joinRoom(String.valueOf(mRoomData.getGameId()), (int) UserAccountManager.getInstance().getUuidAsLong(), isAnchor);
            // 不发送本地音频
//            EngineManager.getInstance().muteLocalAudioStream(true);
            // 伪装评论消息
            for (int i = 0; i < mRoomData.getRoundInfoModelList().size(); i++) {
                RoundInfoModel roundInfoModel = mRoomData.getRoundInfoModelList().get(i);
                PlayerInfoModel playerInfoModel = RoomDataUtils.getPlayerInfoById(mRoomData, roundInfoModel.getUserID());
                BasePushInfo basePushInfo = new BasePushInfo();
                basePushInfo.setRoomID(mRoomData.getGameId());
                basePushInfo.setSender(new UserInfo.Builder()
                        .setUserID(playerInfoModel.getUserInfo().getUserId())
                        .setAvatar(playerInfoModel.getUserInfo().getAvatar())
                        .setNickName(playerInfoModel.getUserInfo().getNickname())
                        .setSex(ESex.fromValue(playerInfoModel.getUserInfo().getSex()))
                        .build());
                String text = String.format("%s第%s个唱", playerInfoModel.getUserInfo().getNickname(), i + 1);
                CommentMsgEvent msgEvent = new CommentMsgEvent(basePushInfo, CommentMsgEvent.MSG_TYPE_SEND, text);
                EventBus.getDefault().post(msgEvent);
            }
            BasePushInfo basePushInfo = new BasePushInfo();
            basePushInfo.setRoomID(mRoomData.getGameId());
            basePushInfo.setSender(new UserInfo.Builder()
                    .setUserID(1)
                    .setAvatar("http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/common/system_default.png")
                    .setNickName("系统消息")
                    .setSex(ESex.fromValue(0))
                    .build());
            String text = "撕哥一声吼：请文明参赛，发现坏蛋请用力举报！";
            CommentMsgEvent msgEvent = new CommentMsgEvent(basePushInfo, CommentMsgEvent.MSG_TYPE_SEND, text);
            EventBus.getDefault().post(msgEvent);
//            IMsgService msgService = ModuleServiceManager.getInstance().getMsgService();
//            if (msgService != null) {
//                msgService.syncHistoryFromChatRoom(String.valueOf(mRoomData.getGameId()), 10, true, null);
//            }
            ChatRoomMsgManager.getInstance().addFilter(mPushMsgFilter);

            EngineManager.getInstance().startRecognize(RecognizeConfig.newBuilder()
                    .setMode(RecognizeConfig.MODE_MANUAL)
                    .setSongName(mRoomData.getSongModel().getItemName())
                    .setArtist(mRoomData.getSongModel().getOwner())
                    .setMResultListener(new ArcRecognizeListener() {
                        @Override
                        public void onResult(String result, List<SongInfo> list, SongInfo targetSongInfo) {
                            // 使用最新的打分方案做优化
                            int score = 0;
                            if (targetSongInfo != null) {
                                score = (int) (targetSongInfo.getScore() * 100);
                            }
                            processScore(score, mLastLineNum);
                        }
                    }).build()
            );
        }
    }

    @Override
    public void start() {
        super.start();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mRoomData.checkRoundInRankMode();
        startSyncGameStateTask(sSyncStateTaskInterval);
    }

    @Override
    public void destroy() {
        super.destroy();
        exitGame();
        cancelHeartBeatTask("destroy");
        cancelSyncGameStateTask();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        EngineManager.getInstance().destroy("rankingroom");
        mUiHandler.removeCallbacksAndMessages(null);
        if (mExoPlayer != null) {
            mExoPlayer.release();
            mExoPlayer = null;
        }

        if (mGameFinishActionSubject != null) {
            mGameFinishActionSubject.onComplete();
        }
        ChatRoomMsgManager.getInstance().removeFilter(mPushMsgFilter);
    }

    /**
     * 上报轮次结束信息
     */
    public void sendRoundOverInfo() {
        MyLog.w(TAG, "上报我的演唱结束");
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


        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());
        map.put("sysScore", sysScore);
        map.put("timeMs", timeMs);
        map.put("sign", sign);

        // 提前获取roundSeq，如果在result里在获取，可能是下下一个了，如果提前收到轮次变化的push
        int roundSeq = -1;
        if (mRoomData.getRealRoundInfo() != null && mRoomData.getRealRoundInfo().getUserID() == UserAccountManager.getInstance().getUuidAsLong()) {
            roundSeq = mRoomData.getRealRoundInfo().getRoundSeq();
        }
        int finalRoundSeq = roundSeq;

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
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
     * 退出游戏
     */
    public void exitGame() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
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


        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.swap(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    U.getToastUtil().showShort("切换请求发送成功");
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
                .observeOn(Schedulers.io())
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

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
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
//        mSyncGameStateTask = HandlerTaskTimer.newBuilder()
//                .delay(delayTime)
//                .interval(sSyncStateTaskInterval)
//                .take(-1)
//                .start(new HandlerTaskTimer.ObserverW() {
//                    @Override
//                    public void onNext(Integer integer) {
//                        MyLog.w(TAG, "12秒钟的 syncGameTask 去更新状态了");
//                        syncGameStatus(mRoomData.getGameId());
//                    }
//                });
    }

    public void cancelSyncGameStateTask() {
        if (mSyncGameStateTask != null && !mSyncGameStateTask.isDisposed()) {
            mSyncGameStateTask.dispose();
        }
    }

    /**
     * 获取投票结果
     *
     * @param gameID
     */
    public void getVoteResult(int gameID, int deep) {
        MyLog.w(TAG, "getVoteResult" + " gameID=" + gameID);
        if (deep > 3) {
            MyLog.d(TAG, "拉了 5次都没数据，过分了。getVoteResult" + " gameID=" + gameID + " deep=" + deep);
            return;
        }
        ApiMethods.subscribe(mRoomServerApi.getVoteResult(gameID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<VoteInfoModel> voteInfoModelList = JSON.parseArray(result.getData().getString("voteInfo"), VoteInfoModel.class);
                    List<ScoreResultModel> scoreResultModels = JSON.parseArray(result.getData().getString("userScoreResult"), ScoreResultModel.class);

                    if (scoreResultModels != null && scoreResultModels.size() > 0) {

                        List<WinResultModel> winResultModels = new ArrayList<>();     // 保存3个人胜负平和投票、逃跑结果
                        ScoreResultModel myScoreResultModel = new ScoreResultModel();
                        for (ScoreResultModel scoreResultModel : scoreResultModels) {
                            WinResultModel model = new WinResultModel();
                            model.setUseID(scoreResultModel.getUserID());
                            model.setType(scoreResultModel.getWinType());
                            winResultModels.add(model);

                            if (scoreResultModel.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                                myScoreResultModel = scoreResultModel;
                            }
                        }
                        U.getToastUtil().showShort("获取投票结果成功");

                        MyLog.d(TAG, "getVoteResult" + " result=" + voteInfoModelList.toString());
                        MyLog.d(TAG, "getVoteResult" + " result=" + myScoreResultModel.toString());
                        MyLog.d(TAG, "getVoteResult" + " result=" + winResultModels.toString());

                        mGameFinishActionSubject.onNext(new RecordData(voteInfoModelList, myScoreResultModel, winResultModels));
                        mGameFinishActionSubject.onComplete();
                    } else {
                        mUiHandler.removeMessages(MSG_GET_VOTE);
                        Message message = mUiHandler.obtainMessage(MSG_GET_VOTE);
                        message.obj = new Pair<>(gameID, deep + 1);
                        mUiHandler.sendMessageDelayed(message, 1000 * deep);
                    }
                } else {
                    MyLog.e(TAG, "getVoteResult result failed, msg is " + result.getErrmsg());
                    mUiHandler.removeMessages(MSG_GET_VOTE);
                    Message message = mUiHandler.obtainMessage(MSG_GET_VOTE);
                    message.obj = new Pair<>(gameID, deep + 1);
                    mUiHandler.sendMessageDelayed(message, 1000 * deep);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, "getVoteResult error " + e);
                mUiHandler.removeMessages(MSG_GET_VOTE);
                Message message = mUiHandler.obtainMessage(MSG_GET_VOTE);
                message.obj = new Pair<Integer, Integer>(gameID, deep + 1);
                mUiHandler.sendMessageDelayed(message, 1000 * deep);
            }
        }, this);
    }

    // 同步游戏详情状态(检测不到长连接调用)
    public void syncGameStatus(int gameID) {
        ApiMethods.subscribe(mRoomServerApi.syncGameStatus(gameID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {

                    long syncStatusTimes = result.getData().getLong("syncStatusTimeMs");  //状态同步时的毫秒时间戳
                    long gameOverTimeMs = result.getData().getLong("gameOverTimeMs");  //游戏结束时间
                    List<OnlineInfoModel> onlineInfos = JSON.parseArray(result.getData().getString("onlineInfo"), OnlineInfoModel.class); //在线状态
                    RoundInfoModel currentInfo = JSON.parseObject(result.getData().getString("currentRound"), RoundInfoModel.class); //当前轮次信息
                    RoundInfoModel nextInfo = JSON.parseObject(result.getData().getString("nextRound"), RoundInfoModel.class); //下个轮次信息

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
     * 根据时间戳更新选手状态,目前就只有两个入口，SyncStatusEvent push了sycn，不写更多入口
     */
    private synchronized void updatePlayerState(long gameOverTimeMs, long syncStatusTimes, List<OnlineInfoModel> onlineInfos, RoundInfoModel currentInfo, RoundInfoModel nextInfo) {
        MyLog.w(TAG, "updatePlayerState" + " gameOverTimeMs=" + gameOverTimeMs + " syncStatusTimes=" + syncStatusTimes + " onlineInfos=" + onlineInfos + " currentInfo=" + currentInfo + " nextInfo=" + nextInfo);
        if (syncStatusTimes > mRoomData.getLastSyncTs()) {
            mRoomData.setLastSyncTs(syncStatusTimes);
            mRoomData.setOnlineInfoList(onlineInfos);
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
                    mRoomData.setExpectRoundInfo(currentInfo);
                    mRoomData.checkRoundInRankMode();
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
    public void onEvent(RoundInfoChangeEvent event) {
        MyLog.w(TAG, "开始切换唱将 myturn=" + event.myturn);
        estimateOverTsThisRound();
        //以防万一
        tryStopRobotPlay();
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
                    mIGameRuleView.startSelfCountdown(new Runnable() {
                        @Override
                        public void run() {
                            //再次确认
                            if (mRoomData.getRealRoundInfo() != null && mRoomData.getRealRoundInfo().getUserID() == MyUserInfoManager.getInstance().getUid()) {
                                EngineManager.getInstance().setClientRole(true);
                                // 等待角色变换成功回调
                            }
                        }
                    });
                    MyLog.w(TAG, "演唱的时间是：" + U.getDateTimeUtils().formatTimeStringForDate(mRoomData.getGameStartTs() + mRoomData.getRealRoundInfo().getSingBeginMs(), "HH:mm:ss:SSS")
                            + "--" + U.getDateTimeUtils().formatTimeStringForDate(mRoomData.getGameStartTs() + mRoomData.getRealRoundInfo().getSingEndMs(), "HH:mm:ss:SSS"));
                }
            });
        } else {
            MyLog.w(TAG, "不是我的轮次，停止发心跳，停止混音，闭麦");

            cancelHeartBeatTask("切换唱将");

            if (RoomDataUtils.isMyRound(event.getLastRoundInfoModel())) {
                if (SkrConfig.getInstance().isNeedUploadAudioForAI()) {
                    //属于需要上传音频文件的状态
                    // 上一轮是我的轮次，暂停录音
                    EngineManager.getInstance().stopAudioRecording();
                    RoundInfoModel myRoundInfoModel = event.getLastRoundInfoModel();
                    if (mRobotScoreHelper != null && mRobotScoreHelper.isScoreEnough()) {
                        myRoundInfoModel.setSysScore(mRobotScoreHelper.getAverageScore());
                        uploadRes1ForAi(myRoundInfoModel);
                    }
                }
            }

            EngineManager.getInstance().stopAudioMixing();
            EngineManager.getInstance().setClientRole(false);
//            EngineManager.getInstance().muteLocalAudioStream(true);
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
                        mIGameRuleView.startRivalCountdown(uid, avatar);
                        checkMachineUser(uid);
                        if (mRoomData.getRealRoundInfo() != null) {
                            MyLog.w(TAG, uid + "开始唱了，歌词走起,演唱的时间是：" + U.getDateTimeUtils().formatTimeStringForDate(mRoomData.getGameStartTs() + mRoomData.getRealRoundInfo().getSingBeginMs(), "HH:mm:ss:SSS")
                                    + "--" + U.getDateTimeUtils().formatTimeStringForDate(mRoomData.getGameStartTs() + mRoomData.getRealRoundInfo().getSingEndMs(), "HH:mm:ss:SSS"));
                        } else {
                            MyLog.w(TAG, "mRoomData.getRealRoundInfo() 为空啊！！！！");
                        }
                        mIGameRuleView.playLyric(RoomDataUtils.getPlayerSongInfoUserId(mRoomData.getPlayerInfoList(), uid), false);
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
                            mIGameRuleView.gameFinish();
                            gameIsFinish();
                        }
                    });
                } else {
                    MyLog.w(TAG, "结束时间比开始时间小，不应该吧 startTs:" + mRoomData.getGameStartTs() + " overTs:" + mRoomData.getGameOverTs());
                }
            }
        }
    }

    private void robotSingBegin(PlayerInfoModel playerInfo) {
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
        if (!EngineManager.getInstance().getParams().isAllRemoteAudioStreamsMute()) {
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
            RoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
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
        EngineManager.getInstance().destroy("rankingroom");

        if (!isConfirmRoundAndGameOver) {
            mUiHandler.removeMessages(MSG_GET_VOTE);
            Message message = mUiHandler.obtainMessage(MSG_GET_VOTE);
            message.obj = new Pair<>(mRoomData.getGameId(), 1);
            mUiHandler.sendMessageDelayed(message, 3000);
        }
    }

    private void recvGameOverFromServer(String from, long gameOverTs) {
        MyLog.w(TAG, "游戏结束 gameOverTs=" + gameOverTs + " from:" + from);
        if (gameOverTs > mRoomData.getGameStartTs() && gameOverTs > mRoomData.getGameOverTs()) {
            mRoomData.setGameOverTs(gameOverTs);
            mRoomData.setExpectRoundInfo(null);
            mRoomData.checkRoundInRankMode();
        }
    }

    public void muteAllRemoteAudioStreams(boolean mute, boolean fromUser) {
        if (fromUser) {
            mRoomData.setMute(mute);
        }
        EngineManager.getInstance().muteAllRemoteAudioStreams(mute);
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
    private void uploadRes1ForAi(RoundInfoModel roundInfoModel) {
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
    private void uploadRes2ForAi(RoundInfoModel roundInfoModel, String audioUrl) {
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
    private void sendUploadRequest(RoundInfoModel roundInfoModel, String audioUrl, String midiUrl) {
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
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
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

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(EngineEvent event) {
        if (event.getType() == EngineEvent.TYPE_USER_JOIN) {
            int userId = event.getUserStatus().getUserId();
            onUserSpeakFromEngine("TYPE_USER_JOIN", userId);
        } else if (event.getType() == EngineEvent.TYPE_USER_ROLE_CHANGE) {
            EngineEvent.RoleChangeInfo roleChangeInfo = event.getObj();
            if (roleChangeInfo.getNewRole() == 1) {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 开始开始混伴奏，开始解除引擎mute
                        File accFile = SongResUtils.getAccFileByUrl(mRoomData.getSongModel().getAcc());
                        File midiFile = SongResUtils.getMIDIFileByUrl(mRoomData.getSongModel().getMidi());

                        if (accFile != null && accFile.exists()) {

//                                    EngineManager.getInstance().muteLocalAudioStream(false);
                            EngineManager.getInstance().startAudioMixing((int) MyUserInfoManager.getInstance().getUid(), accFile.getAbsolutePath()
                                    , midiFile == null ? "" : midiFile.getAbsolutePath(), mRoomData.getSongModel().getBeginMs(), false, false, 1);
                            /**
                             * 现在歌儿都是截断过的，getSingBeginMs和getSingEndMs是歌词的时间，伴奏从0位置开始播放
                             */
                            EngineManager.getInstance().setAudioMixingPosition(0);
                            // 还应开始播放歌词
                            mIGameRuleView.playLyric(mRoomData.getSongModel(), true);
                            mIGameRuleView.showLeftTime(mRoomData.getRealRoundInfo().getSingEndMs() - mRoomData.getRealRoundInfo().getSingBeginMs());
                            MyLog.w(TAG, "本人开始唱了，歌词和伴奏响起");

                            //开始录制声音
                            if (SkrConfig.getInstance().isNeedUploadAudioForAI()) {
                                // 需要上传音频伪装成机器人
                                EngineManager.getInstance().startAudioRecording(RoomDataUtils.getSaveAudioForAiFilePath(), Constants.AUDIO_RECORDING_QUALITY_HIGH);
                            }
                            mRobotScoreHelper = new RobotScoreHelper();
                            //尝试再用融云通知对端
                            sendUserSpeakEventToOthers();
                        }
                    }
                });
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
            sendRoundOverInfo();
        } else if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_TIME_FLY_LISTENER) {
            EngineEvent.MixMusicTimeInfo timeInfo = (EngineEvent.MixMusicTimeInfo) event.getObj();
            //这个是唱的时间，先在按长度算时间
            if (timeInfo.getCurrent() >= mRoomData.getSongModel().getEndMs() - mRoomData.getSongModel().getBeginMs()) {
                //可以发结束轮次的通知了
                sendRoundOverInfo();
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
        RoundInfoModel infoModel = RoomDataUtils.getRoundInfoByUserId(mRoomData.getRoundInfoModelList(), muteUserId);
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
                mRoomData.setRealRoundInfo(infoModel);
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
        RoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel != null && !infoModel.isHasSing()) {
            infoModel.setHasSing(true);
            mIGameRuleView.showLeftTime(infoModel.getSingEndMs() - infoModel.getSingBeginMs());
            mIGameRuleView.playLyric(RoomDataUtils.getPlayerSongInfoUserId(mRoomData.getPlayerInfoList(), infoModel.getUserID()), true);
            startLastTwoSecondTask();
        }
    }

    private void startLastTwoSecondTask() {
        final RoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
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
                            .build()
                    )
                    .build();
            String contnet = U.getBase64Utils().encode(roomMsg.toByteArray());
            msgService.sendChatRoomMessage(String.valueOf(mRoomData.getGameId()), CustomMsgType.MSG_TYPE_ROOM, contnet, null);
        }
    }

    // 游戏轮次结束的通知消息（在某人向服务器短连接成功后推送)
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventMainThread(RoundOverEvent roundOverEvent) {
        MyLog.w(TAG, "收到服务器的某一个人轮次结束的push，id是 " + roundOverEvent.currenRound.getUserID()
                + ", exitUserID 是 " + roundOverEvent.exitUserID + " timets 是" + roundOverEvent.info.getTimeMs());
        if (mRoomData.getLastSyncTs() > roundOverEvent.info.getTimeMs()) {
            MyLog.w(TAG, "但是是旧数据");
            return;
        }

        if (RoomDataUtils.roundInfoEqual(roundOverEvent.currenRound, mRoomData.getRealRoundInfo())) {
            // 确实等于当前轮次
            if (mRoomData.getRealRoundInfo() != null) {
                MyLog.w(TAG, "确实是当前轮次结束了");
                mRoomData.getRealRoundInfo().setEndTs(roundOverEvent.roundOverTimeMs);
            }
        }
        // 游戏轮次结束
        if (RoomDataUtils.roundSeqLarger(roundOverEvent.nextRound, mRoomData.getExpectRoundInfo())) {
            // 轮次确实比当前的高，可以切换
            MyLog.w(TAG, "轮次确实比当前的高，可以切换");
            mRoomData.setExpectRoundInfo(roundOverEvent.nextRound);
            mRoomData.checkRoundInRankMode();
        }
    }

    //轮次和游戏结束通知，除了已经结束状态，别的任何状态都要变成
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventMainThread(RoundAndGameOverEvent roundAndGameOverEvent) {
        MyLog.w(TAG, "收到服务器的游戏结束的push timets 是 " + roundAndGameOverEvent.info.getTimeMs());
        isConfirmRoundAndGameOver = true;

        mUiHandler.removeMessages(MSG_GET_VOTE);
        recvGameOverFromServer("push", roundAndGameOverEvent.roundOverTimeMs);
        cancelSyncGameStateTask();

        MyLog.d(TAG, "onEventMainThread" + " roundAndGameOverEvent mVoteInfoModels =" + roundAndGameOverEvent.mVoteInfoModels);
        MyLog.d(TAG, "onEventMainThread" + " roundAndGameOverEvent mScoreResultModel =" + roundAndGameOverEvent.mScoreResultModel);
        MyLog.d(TAG, "onEventMainThread" + " roundAndGameOverEvent mWinResultModels =" + roundAndGameOverEvent.mWinResultModels);

        mGameFinishActionSubject.onNext(new RecordData(roundAndGameOverEvent.mVoteInfoModels, roundAndGameOverEvent.mScoreResultModel, roundAndGameOverEvent.mWinResultModels));
        mGameFinishActionSubject.onComplete();
    }

    // 应用进程切到后台通知
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AppSwapEvent appSwapEvent) {
        MyLog.w(TAG, "onEventMainThread syncStatusEvent");
    }

    // 长连接 状态同步信令， 以11秒为单位检测
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SyncStatusEvent syncStatusEvent) {
        String msg = "收到服务器更新状态的push了, currentRound 是 ";
        msg = msg + (syncStatusEvent.currentInfo == null ? "null" : syncStatusEvent.currentInfo.getUserID() + "");
        msg = msg + ", nextInfo 是 " + (syncStatusEvent.nextInfo == null ? "null" : syncStatusEvent.nextInfo.getUserID() + "");
        msg = msg + ", timeMs" + syncStatusEvent.info.getTimeMs();
        MyLog.w(TAG, msg);
        startSyncGameStateTask(sSyncStateTaskInterval);
        updatePlayerState(syncStatusEvent.gameOverTimeMs, syncStatusEvent.syncStatusTimes, syncStatusEvent.onlineInfos, syncStatusEvent.currentInfo, syncStatusEvent.nextInfo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ExitGameEvent exitGameEvent) {
        MyLog.w(TAG, "收到一个人退出的push了，type是" + exitGameEvent.type + ",timeMs是" + exitGameEvent.info.getTimeMs());
        if (exitGameEvent.type == EXIT_GAME_AFTER_PLAY) {   //我在唱歌，有一个人退出
            U.getToastUtil().showShort("游戏结束后，某一个人退出了");
        } else if (exitGameEvent.type == EXIT_GAME_OUT_ROUND) {   //我是观众，有一个人退出
            U.getToastUtil().showShort("游戏中，某一个人退出了");
        }

        UserInfoModel userInfo = mRoomData.getUserInfo(exitGameEvent.exitUserID);
        BasePushInfo basePushInfo = new BasePushInfo();
        basePushInfo.setRoomID(mRoomData.getGameId());
        basePushInfo.setSender(new UserInfo.Builder()
                .setUserID(1)
                .setAvatar("http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/common/system_default.png")
                .setNickName("系统消息")
                .setSex(ESex.fromValue(0))
                .build());
        String text = userInfo.getNickname() + "偷偷溜走啦～";
        CommentMsgEvent msgEvent = new CommentMsgEvent(basePushInfo, CommentMsgEvent.MSG_TYPE_SEND, text);
        EventBus.getDefault().post(msgEvent);
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
    public void onEvent(LrcEvent.LineEndEvent event) {
        if (RoomDataUtils.isMyRound(mRoomData.getRealRoundInfo())) {
            mLastLineNum = event.getLineNum();
            EngineManager.getInstance().recognizeInManualMode();
//            int score = EngineManager.getInstance().getLineScore();
//            processScore(score, event.getLineNum());
        } else {
            if (RoomDataUtils.isRobotRound(mRoomData.getRealRoundInfo(), mRoomData.getPlayerInfoList())) {
                // 尝试算机器人的演唱得分
                if (mRobotScoreHelper != null) {
                    int score = mRobotScoreHelper.tryGetScoreByLine(event.getLineNum());
                    if (score >= 0) {
                        U.getToastUtil().showShort("score:" + score);
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mIGameRuleView.updateScrollBarProgress(score);
                            }
                        });
                    }
                }
            } else {
                // 尝试拿其他人的演唱打分
            }
        }
    }

    void processScore(int score, int line) {
        U.getToastUtil().showShort("score:" + score);
        MyLog.d(TAG, "onEvent" + " 得分=" + score);
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mIGameRuleView.updateScrollBarProgress(score);
            }
        });
        MachineScoreItem machineScoreItem = new MachineScoreItem();
        machineScoreItem.setScore(score);
        long ts = EngineManager.getInstance().getAudioMixingCurrentPosition();
        machineScoreItem.setTs(ts);
        machineScoreItem.setNo(line);
        // 打分信息传输给其他人
        sendScoreToOthers(machineScoreItem);
        if (mRobotScoreHelper != null) {
            mRobotScoreHelper.add(machineScoreItem);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MachineScoreEvent event) {
        //收到其他人的机器打分消息，比较复杂，暂时简单点，轮次正确就直接展示
        if (RoomDataUtils.isThisUserRound(mRoomData.getRealRoundInfo(), event.userId)) {
            mIGameRuleView.updateScrollBarProgress(event.score);
        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEvent(PlayerEvent.TimeFly event) {
//        if (!RoomDataUtils.isMyRound(mRoomData.getRealRoundInfo())) {
//            // 尝试算打分
//            if (mRobotScoreHelper != null) {
//                int score = mRobotScoreHelper.tryGetScoreByTs(event.curPostion);
//                if (score >= 0) {
//                    U.getToastUtil().showShort("score:" + score);
//                    mIGameRuleView.updateScrollBarProgress(score);
//                }
//            }
//        }
//    }
}
