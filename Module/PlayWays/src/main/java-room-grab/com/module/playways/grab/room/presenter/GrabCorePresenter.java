package com.module.playways.grab.room.presenter;

import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
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
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.component.busilib.SkrConfig;
import com.engine.EngineManager;
import com.engine.Params;
import com.engine.arccloud.ArcRecognizeListener;
import com.engine.arccloud.RecognizeConfig;
import com.engine.arccloud.SongInfo;
import com.module.playways.RoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.event.GrabGameOverEvent;
import com.module.playways.grab.room.event.GrabRoundChangeEvent;
import com.module.playways.grab.room.event.GrabRoundStatusChangeEvent;
import com.module.playways.grab.room.event.SomeOneLightOffEvent;
import com.module.playways.grab.room.inter.IGrabView;
import com.module.playways.grab.room.model.GrabResultInfoModel;
import com.module.playways.rank.msg.BasePushInfo;
import com.module.playways.rank.msg.event.CommentMsgEvent;
import com.module.playways.rank.msg.event.ExitGameEvent;
import com.module.playways.rank.msg.event.QExitGameMsgEvent;
import com.module.playways.rank.msg.event.QGetSingChanceMsgEvent;
import com.module.playways.rank.msg.event.QNoPassSingMsgEvent;
import com.module.playways.rank.msg.event.QRoundAndGameOverMsgEvent;
import com.module.playways.rank.msg.event.QRoundOverMsgEvent;
import com.module.playways.rank.msg.event.QSyncStatusMsgEvent;
import com.module.playways.rank.msg.event.QWantSingChanceMsgEvent;
import com.module.playways.rank.msg.filter.PushMsgFilter;
import com.module.playways.rank.msg.manager.ChatRoomMsgManager;
import com.module.playways.rank.prepare.model.OnlineInfoModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.prepare.model.RoundInfoModel;
import com.module.playways.rank.room.SwapStatusType;
import com.module.playways.RoomDataUtils;
import com.module.playways.rank.room.score.MachineScoreItem;
import com.module.playways.rank.room.score.RobotScoreHelper;
import com.zq.live.proto.Common.ESex;
import com.zq.live.proto.Common.UserInfo;
import com.zq.live.proto.Room.EQRoundResultType;
import com.zq.live.proto.Room.PlayerInfo;
import com.zq.live.proto.Room.RoomMsg;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.annotation.NotNull;

import java.util.HashMap;
import java.util.List;

import io.agora.rtc.Constants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.module.playways.rank.msg.event.ExitGameEvent.EXIT_GAME_AFTER_PLAY;
import static com.module.playways.rank.msg.event.ExitGameEvent.EXIT_GAME_OUT_ROUND;

public class GrabCorePresenter extends RxLifeCyclePresenter {
    public String TAG = "GrabCorePresenter";

    private static long sSyncStateTaskInterval = 4500;
    static final int MSG_ROBOT_SING_BEGIN = 10;

    RoomData mRoomData;

    GrabRoomServerApi mRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);

    HandlerTaskTimer mSyncGameStateTask;

    IGrabView mIGrabView;

    RobotScoreHelper mRobotScoreHelper;

    boolean mDestroyed = false;

    ExoPlayer mExoPlayer;

    Handler mUiHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ROBOT_SING_BEGIN:
                    robotSingBegin((PlayerInfoModel) msg.obj);
                    break;
            }
        }
    };

    PushMsgFilter mPushMsgFilter = new PushMsgFilter() {
        @Override
        public boolean doFilter(RoomMsg msg) {
            if (msg != null && msg.roomID == mRoomData.getGameId()) {
                return true;
            }
            return false;
        }
    };

    public GrabCorePresenter(@NotNull IGrabView iGrebView, @NotNull RoomData roomData) {
        mIGrabView = iGrebView;
        mRoomData = roomData;
        TAG = "GrabCorePresenter";
        if (mRoomData.getGameId() > 0) {
            Params params = Params.getFromPref();
            EngineManager.getInstance().init("grabroom", params);
            EngineManager.getInstance().joinRoom(String.valueOf(mRoomData.getGameId()), (int) UserAccountManager.getInstance().getUuidAsLong(), true);
            // 不发送本地音频
            EngineManager.getInstance().muteLocalAudioStream(true);
        }
        if (mRoomData.getGameId() > 0) {
            for (PlayerInfoModel playerInfoModel : mRoomData.getPlayerInfoList()) {
                BasePushInfo basePushInfo = new BasePushInfo();
                basePushInfo.setRoomID(mRoomData.getGameId());
                basePushInfo.setSender(new UserInfo.Builder()
                        .setUserID(playerInfoModel.getUserInfo().getUserId())
                        .setAvatar(playerInfoModel.getUserInfo().getAvatar())
                        .setNickName(playerInfoModel.getUserInfo().getNickname())
                        .setSex(ESex.fromValue(playerInfoModel.getUserInfo().getSex()))
                        .build());
                String text = String.format("%s加入房间", playerInfoModel.getUserInfo().getNickname());
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
        }
    }

    @Override
    public void start() {
        super.start();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    /**
     * 由ui层告知
     * 开场动画结束
     */
    public void onOpeningAnimationOver() {
        // 开始触发触发轮次变化
        mRoomData.checkRoundInGrabMode();
        startSyncGameStateTask(sSyncStateTaskInterval);
    }

    /**
     * 播放导唱
     */
    public void playGuide() {
        if (mDestroyed) {
            return;
        }
        RoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null) {
            if (mExoPlayer == null) {
                mExoPlayer = new ExoPlayer();
            }
            mExoPlayer.startPlay(now.getSongModel().getStandIntro());
        }
    }

    /**
     * 停止播放导唱
     */
    public void stopGuide() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
        }
    }

    /**
     * 开始演唱
     */
    public void beginSing() {
        // 打开引擎，变为主播
        if (mRoomData.getGameId() > 0) {
            EngineManager.getInstance().setClientRole(true);
            //开始录制声音
            if (SkrConfig.getInstance().isNeedUploadAudioForAI()) {
                // 需要上传音频伪装成机器人
                EngineManager.getInstance().startAudioRecording(RoomDataUtils.getSaveAudioForAiFilePath(), Constants.AUDIO_RECORDING_QUALITY_HIGH);
                RoundInfoModel now = mRoomData.getRealRoundInfo();
                if (mRobotScoreHelper == null) {
                    mRobotScoreHelper = new RobotScoreHelper();
                }
                mRobotScoreHelper.reset();
                EngineManager.getInstance().startRecognize(RecognizeConfig.newBuilder()
                        .setSongName(now.getSongModel().getItemName())
                        .setArtist(now.getSongModel().getOwner())
                        .setMode(RecognizeConfig.MODE_AUTO)
                        .setMResultListener(new ArcRecognizeListener() {
                            @Override
                            public void onResult(String result, List<SongInfo> list, SongInfo targetSongInfo) {
                                int score = 0;
                                if (targetSongInfo != null) {
                                    score = (int) (targetSongInfo.getScore() * 100);
                                }

                                MachineScoreItem machineScoreItem = new MachineScoreItem();
                                machineScoreItem.setNo(0);
                                machineScoreItem.setTs(System.currentTimeMillis() - mRobotScoreHelper.getBeginRecordTs());
                                machineScoreItem.setScore(score);
                                mRobotScoreHelper.add(machineScoreItem);
                            }
                        })
                        .build());
            }
        }
    }

    /**
     * 抢唱歌权
     */
    public void grabThisRound() {
        RoundInfoModel now = mRoomData.getRealRoundInfo();
        //TEST
//        if (true && MyLog.isDebugLogOpen()) {
//            U.getToastUtil().showShort("执行测试抢代码");
//            now.addGrabUid(RoomDataUtils.isCurrentRound(now.getRoundSeq(), mRoomData), (int) MyUserInfoManager.getInstance().getUid());
//            mUiHanlder.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    now.setUserID((int) MyUserInfoManager.getInstance().getUid());
//                    now.updateStatus(true, RoundInfoModel.STATUS_SING);
//                }
//            }, 3000);
//
//            HandlerTaskTimer.newBuilder()
//                    .interval(4000)
//                    .start(new HandlerTaskTimer.ObserverW() {
//                        @Override
//                        public void onNext(Integer integer) {
//                            SomeOneLightOffEvent event = new SomeOneLightOffEvent(5, now);
//                            EventBus.getDefault().post(event);
//                            mUiHanlder.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    SomeOneLightOffEvent event2 = new SomeOneLightOffEvent(3, now);
//                                    EventBus.getDefault().post(event2);
//                                }
//                            },1000);
//                        }
//                    });
//            return;
//        }
        if (now == null) {
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());
        map.put("roundSeq", now.getRoundSeq());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.wangSingChance(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.e(TAG, "grabThisRound erro code is " + result.getErrno() + ",traceid is " + result.getTraceId());
                if (result.getErrno() == 0) {
                    // 抢成功了
                    now.addGrabUid(RoomDataUtils.isCurrentRound(now.getRoundSeq(), mRoomData), (int) MyUserInfoManager.getInstance().getUid());
                } else {

                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, "grabThisRound error " + e);

            }
        }, this);
    }

    /**
     * 灭灯
     */
    public void lightsOff() {
        RoundInfoModel now = mRoomData.getRealRoundInfo();
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());
        map.put("roundSeq", mRoomData.getRealRoundSeq());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.lightOff(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.e(TAG, "lightsOff erro code is " + result.getErrno() + ",traceid is " + result.getTraceId());
                if (result.getErrno() == 0) {
                    boolean notify = RoomDataUtils.isCurrentRound(now.getRoundSeq(), mRoomData);
                    now.addLightOffUid(notify, (int) MyUserInfoManager.getInstance().getUid());
                } else {

                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, "lightsOff error " + e);

            }
        }, this);
    }

    private void robotSingBegin(PlayerInfoModel playerInfo) {
        String skrerUrl = playerInfo.getResourceInfoList().get(0).getAudioURL();
        if (mRobotScoreHelper == null) {
            mRobotScoreHelper = new RobotScoreHelper();
        }

        if (mExoPlayer == null) {
            mExoPlayer = new ExoPlayer();
        }
        mExoPlayer.startPlay(skrerUrl);
        if (!EngineManager.getInstance().getParams().isAllRemoteAudioStreamsMute()) {
            mExoPlayer.setVolume(1);
        } else {
            mExoPlayer.setVolume(0);
        }
    }

    private void tryStopRobotPlay() {
        if (mExoPlayer != null) {
            mExoPlayer.reset();
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
     * 自己的轮次结束了
     *
     * @param roundInfoModel
     */
    private void onSelfRoundOver(RoundInfoModel roundInfoModel) {
        // 上一轮演唱是自己，开始上传资源
        if (SkrConfig.getInstance().isNeedUploadAudioForAI()) {
            //属于需要上传音频文件的状态
            // 上一轮是我的轮次，暂停录音
            if (mRoomData.getGameId() > 0) {
                EngineManager.getInstance().stopAudioRecording();
            }
            // 上传打分
            if (mRobotScoreHelper != null) {
                if (mRobotScoreHelper != null && mRobotScoreHelper.isScoreEnough()
                        && roundInfoModel.getOverReason() == EQRoundResultType.ROT_TYPE_1.getValue()) {
                    // 是一唱到底的才上传
                    roundInfoModel.setSysScore(mRobotScoreHelper.getAverageScore());
                    uploadRes1ForAi(roundInfoModel);
                }
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
                            sendUploadRequest(roundInfoModel, url);
                        }

                        @Override
                        public void onFailure(String msg) {

                        }
                    });
        }
    }

    /**
     * 上传机器人资源相关文件到服务器
     *
     * @param roundInfoModel
     * @param audioUrl
     */
    private void sendUploadRequest(RoundInfoModel roundInfoModel, String audioUrl) {
        long timeMs = System.currentTimeMillis();
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());
        map.put("itemID", roundInfoModel.getPlaybookID());
        map.put("sysScore", roundInfoModel.getSysScore());
        map.put("audioURL", audioUrl);
//        map.put("midiURL", midiUrl);
        map.put("timeMs", timeMs);
        StringBuilder sb = new StringBuilder();
        sb.append("skrer")
                .append("|").append(mRoomData.getGameId())
                .append("|").append(roundInfoModel.getPlaybookID())
                .append("|").append(roundInfoModel.getSysScore())
                .append("|").append(audioUrl)
//                .append("|").append(midiUrl)
                .append("|").append(timeMs);
        String sign = U.getMD5Utils().MD5_32(sb.toString());
        map.put("sign", sign);
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.saveRes(body), new ApiObserver<ApiResult>() {
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

    @Override
    public void destroy() {
        MyLog.d(TAG, "destroy begin");
        super.destroy();
        mDestroyed = true;
        exitGame();
        cancelSyncGameStateTask();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        EngineManager.getInstance().destroy("grabroom");
        mUiHanlder.removeCallbacksAndMessages(null);
        ChatRoomMsgManager.getInstance().removeFilter(mPushMsgFilter);
        if (mExoPlayer != null) {
            mExoPlayer.release();
        } else {
            MyLog.d(TAG, "mExoPlayer == null ");
        }
        MyLog.d(TAG, "destroy over");
    }

    /**
     * 告知我的的抢唱阶段结束了
     */
    public void sendMyGrabOver() {
        MyLog.d(TAG, "上报我的抢唱结束 ");
        RoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
        if (roundInfoModel == null) {
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());
        map.put("roundSeq", roundInfoModel.getRoundSeq());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.sendGrapOver(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    MyLog.w(TAG, "我的抢唱结束上报成功 traceid is " + result.getTraceId());
                } else {
                    MyLog.w(TAG, "我的抢唱结束上报失败 traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.w(TAG, "sendRoundOverInfo" + " error " + e);
            }
        }, this);
    }

    /**
     * 上报轮次结束信息
     */
    public void sendRoundOverInfo() {
        MyLog.w(TAG, "上报我的演唱结束");
        estimateOverTsThisRound();

        RoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
        if (roundInfoModel == null || roundInfoModel.getUserID() != MyUserInfoManager.getInstance().getUid()) {
            return;
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());
        map.put("roundSeq", roundInfoModel.getRoundSeq());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.sendRoundOver(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    MyLog.w(TAG, "演唱结束上报成功 traceid is " + result.getTraceId());
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
     * 轮询同步状态task
     */
    public void startSyncGameStateTask(long delayTime) {
        cancelSyncGameStateTask();

        if (mRoomData.isIsGameFinish()) {
            MyLog.w(TAG, "游戏结束了，还特么Sync");
            return;
        }

        mSyncGameStateTask = HandlerTaskTimer.newBuilder()
                .delay(delayTime)
                .interval(sSyncStateTaskInterval)
                .take(-1)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        MyLog.w(TAG, "4秒钟的 syncGameTask 去更新状态了");
                        syncGameStatus(mRoomData.getGameId());
                    }
                });
    }

    public void cancelSyncGameStateTask() {
        if (mSyncGameStateTask != null) {
            mSyncGameStateTask.dispose();
        }
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
                    String msg = "";
                    if (currentInfo != null) {
                        msg = "syncGameStatus成功了, currentRound 是 " + currentInfo;
                    } else {
                        msg = "syncGameStatus成功了, currentRound 是 null";
                    }

                    msg = msg + ",traceid is " + result.getTraceId();
                    MyLog.w(TAG, msg);

                    if (currentInfo == null) {
                        onGameOver("syncGameStatus", gameOverTimeMs, null);
                        return;
                    }

                    currentInfo.setType(RoundInfoModel.TYPE_GRAB);
                    updatePlayerState(gameOverTimeMs, syncStatusTimes, onlineInfos, currentInfo);
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
    private synchronized void updatePlayerState(long gameOverTimeMs, long syncStatusTimes, List<OnlineInfoModel> onlineInfos, RoundInfoModel newRoundInfo) {
        MyLog.w(TAG, "updatePlayerState" + " gameOverTimeMs=" + gameOverTimeMs + " syncStatusTimes=" + syncStatusTimes + " onlineInfos=" + onlineInfos + " currentInfo=" + newRoundInfo.getRoundSeq());
        if (syncStatusTimes > mRoomData.getLastSyncTs()) {
            mRoomData.setLastSyncTs(syncStatusTimes);
            if (onlineInfos != null) {
                for (OnlineInfoModel onlineInfoModel : onlineInfos) {
                    mRoomData.setOnline(onlineInfoModel.getUserID(), onlineInfoModel.isIsOnline());
                }
            }
            mUiHanlder.post(new Runnable() {
                @Override
                public void run() {
                    mIGrabView.updateUserState(onlineInfos);
                }
            });
        }

        if (gameOverTimeMs != 0) {
            if (gameOverTimeMs > mRoomData.getGameStartTs()) {
                MyLog.w(TAG, "gameOverTimeMs ！= 0 游戏应该结束了");
                // 游戏结束了
                onGameOver("sync", gameOverTimeMs, null);
            } else {
                MyLog.w(TAG, "服务器结束时间不合法 startTs:" + mRoomData.getGameStartTs() + " overTs:" + gameOverTimeMs);
            }
        } else {
            // 没结束 current 不应该为null
            if (newRoundInfo != null) {
                // 服务下发的轮次已经大于当前轮次了，说明本地信息已经不对了，更新
                if (RoomDataUtils.roundSeqLarger(newRoundInfo, mRoomData.getRealRoundInfo())) {
                    MyLog.w(TAG, "updatePlayerState" + " sync发现本地轮次信息滞后，更新");
                    // 轮次确实比当前的高，可以切换
                    mRoomData.setExpectRoundInfo(RoomDataUtils.getRoundInfoFromRoundInfoList(mRoomData, newRoundInfo));
                    mRoomData.checkRoundInGrabMode();
                } else if (RoomDataUtils.isCurrentRound(newRoundInfo.getRoundSeq(), mRoomData)) {
                    /**
                     * 是当前轮次，最近状态就更新整个轮次
                     */
                    if (syncStatusTimes > mRoomData.getLastSyncTs()) {
                        mRoomData.getRealRoundInfo().tryUpdateByRoundInfoModel(newRoundInfo, true);
                    }
                }
            } else {
                MyLog.w(TAG, "服务器结束时间不合法 currentInfo=null");
            }
        }
    }

    /**
     * 通知游戏结束
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING, priority = 9)
    public void onEvent(GrabGameOverEvent event) {
        MyLog.d(TAG, "GrabGameOverEvent");
        estimateOverTsThisRound();
        tryStopRobotPlay();
        EngineManager.getInstance().stopRecognize();
        mRoomData.setIsGameFinish(true);
        cancelSyncGameStateTask();
        // 游戏结束了,处理相应的ui逻辑
        mUiHanlder.post(new Runnable() {
            @Override
            public void run() {
                mIGrabView.roundOver(event.lastRoundInfo.getOverReason(), event.lastRoundInfo.getResultType(), false, null);
            }
        });
        // 销毁引擎，减小成本
        EngineManager.getInstance().destroy("grabroom");
        mUiHanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIGrabView.gameFinish();
            }
        }, 2000);
    }

    /**
     * 轮次信息有更新
     */
    @Subscribe(threadMode = ThreadMode.POSTING, priority = 9)
    public void onEvent(GrabRoundChangeEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        estimateOverTsThisRound();
        closeEngine();
        tryStopRobotPlay();
        EngineManager.getInstance().stopRecognize();
        RoundInfoModel now = event.newRoundInfo;
        if (now.getStatus() == RoundInfoModel.STATUS_GRAB) {
            //抢唱阶段，播抢唱卡片
            if (event.lastRoundInfo != null && event.lastRoundInfo.getStatus() >= RoundInfoModel.STATUS_SING) {
                // 新一轮的抢唱阶段，得告诉上一轮演唱结束了啊，上一轮演唱结束卡片播完，才播歌曲卡片

                mUiHanlder.post(new Runnable() {
                    @Override
                    public void run() {
                        mIGrabView.roundOver(event.lastRoundInfo.getOverReason(), event.lastRoundInfo.getResultType(), true, now);
                    }
                });

                if (event.lastRoundInfo.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                    onSelfRoundOver(event.lastRoundInfo);
                }
            } else {
                mIGrabView.grabBegin(now.getRoundSeq(), now.getSongModel());
            }
        } else if (now.getStatus() == RoundInfoModel.STATUS_SING) {
            // 演唱阶段
            if (now.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                mIGrabView.singBySelf();
            } else {
                mIGrabView.singByOthers(now.getUserID());
            }
        } else if (now.getStatus() == RoundInfoModel.STATUS_OVER) {
            MyLog.w(TAG, "GrabRoundChangeEvent 刚切换到该轮次就告诉我轮次结束？？？roundSeq:" + now.getRoundSeq());
            MyLog.w(TAG, "自动切换到下个轮次");
            RoundInfoModel roundInfoModel = RoomDataUtils.findRoundInfoBySeq(mRoomData.getRoundInfoModelList(), now.getRoundSeq() + 1);
            mRoomData.setExpectRoundInfo(roundInfoModel);
            mRoomData.checkRoundInGrabMode();
        }
    }

    /**
     * 轮次内状态更新
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING, priority = 9)
    public void onEvent(GrabRoundStatusChangeEvent event) {
        MyLog.d(TAG, "onEvent" + " event=" + event);
        estimateOverTsThisRound();
        closeEngine();
        RoundInfoModel now = event.roundInfo;
        tryStopRobotPlay();
        if (now.getStatus() == RoundInfoModel.STATUS_GRAB) {
            //抢唱阶段，播抢唱卡片
            mIGrabView.grabBegin(now.getRoundSeq(), now.getSongModel());
        } else if (now.getStatus() == RoundInfoModel.STATUS_SING) {
            // 演唱阶段
            if (now.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                mIGrabView.singBySelf();
            } else {
                mIGrabView.singByOthers(now.getUserID());
                checkMachineUser(now.getUserID());
            }
        }
    }

    private void closeEngine() {
        if (mRoomData.getGameId() > 0) {
            EngineManager.getInstance().setClientRole(false);
        }
    }

    /**
     * 想要演唱机会的人
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QWantSingChanceMsgEvent event) {
        if (RoomDataUtils.isCurrentRound(event.getRoundSeq(), mRoomData)) {
            MyLog.w(TAG, "有人想唱：userID " + event.getUserID() + ", seq " + event.getRoundSeq());
            RoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
            roundInfoModel.addGrabUid(true, event.getUserID());
        } else {
            MyLog.w(TAG, "有人想唱,但是不是这个轮次：userID " + event.getUserID() + ", seq " + event.getRoundSeq() + "，当前轮次是 " + mRoomData.getRealRoundSeq());
        }
    }

    /**
     * 抢到演唱机会的人
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QGetSingChanceMsgEvent event) {
        if (RoomDataUtils.isCurrentRound(event.getRoundSeq(), mRoomData)) {
            MyLog.w(TAG, "抢到唱歌权：userID " + event.getUserID() + ", seq " + event.getRoundSeq());
            RoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
            roundInfoModel.setHasSing(true);
            roundInfoModel.setUserID(event.getUserID());
            roundInfoModel.updateStatus(true, RoundInfoModel.STATUS_SING);
        } else {
            MyLog.w(TAG, "抢到唱歌权,但是不是这个轮次：userID " + event.getUserID() + ", seq " + event.getRoundSeq() + "，当前轮次是 " + mRoomData.getRealRoundSeq());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QExitGameMsgEvent event) {
        MyLog.w(TAG, "有人退出了：userID is " + event.getUserID());
        mRoomData.setOnline(event.userID, false);
        BasePushInfo basePushInfo = new BasePushInfo();
        basePushInfo.setRoomID(mRoomData.getGameId());
        basePushInfo.setSender(new UserInfo.Builder()
                .setUserID(1)
                .setAvatar("http://bucket-oss-inframe.oss-cn-beijing.aliyuncs.com/common/system_default.png")
                .setNickName("系统消息")
                .setSex(ESex.fromValue(0))
                .build());
        PlayerInfoModel playerInfoModel = RoomDataUtils.getPlayerInfoById(mRoomData, event.getUserID());
        if(playerInfoModel != null){
            String text = playerInfoModel.getUserInfo().getNickname() + "偷偷溜走啦～";
            MyLog.d(TAG, "onEvent" + " event.getInfo().getSender:" + event.getInfo().getSender());
            CommentMsgEvent msgEvent = new CommentMsgEvent(basePushInfo, CommentMsgEvent.MSG_TYPE_SEND, text);
            EventBus.getDefault().post(msgEvent);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QNoPassSingMsgEvent event) {
        if (RoomDataUtils.isCurrentRound(event.getRoundSeq(), mRoomData)) {
            MyLog.w(TAG, "有人灭灯了：userID " + event.getUserID() + ", seq " + event.getRoundSeq());
            RoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
            //都开始灭灯肯定是已经开始唱了
            roundInfoModel.updateStatus(true, RoundInfoModel.STATUS_SING);
            roundInfoModel.addLightOffUid(true, event.getUserID());
        } else {
            MyLog.w(TAG, "有人灭灯了,但是不是这个轮次：userID " + event.getUserID() + ", seq " + event.getRoundSeq() + "，当前轮次是 " + mRoomData.getRealRoundSeq());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QRoundOverMsgEvent event) {
        MyLog.w(TAG, "收到服务器的某一个人轮次结束的push event:" + event);
        if (mRoomData.getLastSyncTs() >= event.getInfo().getTimeMs()) {
            MyLog.w(TAG, "但是是个旧数据");
            return;
        }

        if (RoomDataUtils.isCurrentRound(event.getCurrentRound().getRoundSeq(), mRoomData)) {
            // 如果是当前轮次
            mRoomData.getRealRoundInfo().tryUpdateByRoundInfoModel(event.currentRound, true);
        }
        // 游戏轮次结束
        if (RoomDataUtils.roundSeqLarger(event.nextRound, mRoomData.getRealRoundInfo())) {
            // 轮次确实比当前的高，可以切换
            MyLog.w(TAG, "轮次确实比当前的高，可以切换");
            mRoomData.setExpectRoundInfo(RoomDataUtils.getRoundInfoFromRoundInfoList(mRoomData, event.nextRound));
            mRoomData.checkRoundInGrabMode();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QRoundAndGameOverMsgEvent event) {
        cancelSyncGameStateTask();
        if (RoomDataUtils.isCurrentRound(event.roundInfoModel.getRoundSeq(), mRoomData)) {
            // 如果是当前轮次
            mRoomData.getRealRoundInfo().tryUpdateByRoundInfoModel(event.roundInfoModel, true);
        }
        onGameOver("QRoundAndGameOverMsgEvent", event.roundOverTimeMs, event.resultInfo);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(QSyncStatusMsgEvent event) {
        MyLog.w(TAG, "收到服务器更新状态,event.currentRound是" + event.getCurrentRound().getRoundSeq() + ", timets 是" + event.info.getTimeMs());
        updatePlayerState(event.getGameOverTimeMs(), event.getSyncStatusTimeMs(), event.getOnlineInfo(), event.getCurrentRound());
    }

    private void onGameOver(String from, long gameOverTs, List<GrabResultInfoModel> grabResultInfoModels) {
        MyLog.w(TAG, "游戏结束 gameOverTs=" + gameOverTs + " from:" + from);
        if (gameOverTs > mRoomData.getGameStartTs() && gameOverTs > mRoomData.getGameOverTs()) {
            cancelSyncGameStateTask();
            if (grabResultInfoModels != null && grabResultInfoModels.size() > 0) {
                mRoomData.setResultList(grabResultInfoModels);
            }
            mRoomData.setGameOverTs(gameOverTs);
            mRoomData.setExpectRoundInfo(null);
            mRoomData.checkRoundInGrabMode();
        } else {
            MyLog.w(TAG, "游戏结束 gameOverTs 不合法，取消");
        }
    }

    public void checkMachineUser(long uid) {
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
            RoundInfoModel roundInfoModel = mRoomData.getRealRoundInfo();
            //这个时间现在待定
            //移除之前的要发生的机器人演唱
            mUiHanlder.removeMessages(MSG_ROBOT_SING_BEGIN);
            Message message = mUiHanlder.obtainMessage(MSG_ROBOT_SING_BEGIN);
            message.obj = playerInfo;
            mUiHanlder.sendMessage(message);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ActivityUtils.ForeOrBackgroundChange event) {
        MyLog.w(TAG, event.foreground ? "切换到前台" : "切换到后台");
        swapGame(!event.foreground, event.foreground);
        if (event.foreground) {
            muteAllRemoteAudioStreams(mRoomData.isMute(), false);
        } else {
            muteAllRemoteAudioStreams(true, false);
        }
    }

    private int estimateOverTsThisRound() {
        int pt = RoomDataUtils.estimateTs2End(mRoomData, mRoomData.getRealRoundInfo());
        MyLog.w(TAG, "估算出距离本轮结束还有" + pt + "ms");
        return pt;
    }


}
