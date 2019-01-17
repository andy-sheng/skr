package com.module.playways.grab.room.presenter;

import android.os.Handler;

import com.alibaba.fastjson.JSON;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.ActivityUtils;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.engine.EngineEvent;
import com.engine.EngineManager;
import com.engine.Params;
import com.module.playways.RoomData;
import com.module.playways.grab.room.inter.IGrabView;
import com.module.playways.rank.msg.event.AppSwapEvent;
import com.module.playways.rank.msg.event.ExitGameEvent;
import com.module.playways.rank.msg.event.RoundAndGameOverEvent;
import com.module.playways.rank.msg.event.RoundOverEvent;
import com.module.playways.rank.msg.event.SyncStatusEvent;
import com.module.playways.rank.prepare.model.OnlineInfoModel;
import com.module.playways.rank.prepare.model.PlayerInfoModel;
import com.module.playways.rank.prepare.model.RoundInfoModel;
import com.module.playways.rank.room.RoomServerApi;
import com.module.playways.rank.room.SwapStatusType;
import com.module.playways.rank.room.event.RoundInfoChangeEvent;
import com.module.playways.rank.room.model.RankDataUtils;
import com.module.playways.rank.room.model.RecordData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.module.playways.rank.msg.event.ExitGameEvent.EXIT_GAME_AFTER_PLAY;
import static com.module.playways.rank.msg.event.ExitGameEvent.EXIT_GAME_OUT_ROUND;

public class GrabCorePresenter extends RxLifeCyclePresenter {
    public String TAG = "GrabCorePresenter";

    private static long sHeartBeatTaskInterval = 3000;
    private static long sSyncStateTaskInterval = 12000;

    RoomData mRoomData;

    RoomServerApi mRoomServerApi = ApiManager.getInstance().createService(RoomServerApi.class);

    HandlerTaskTimer mHeartBeatTask;

    HandlerTaskTimer mSyncGameStateTask;

    IGrabView mIGameRuleView;

    Handler mUiHanlder = new Handler();

    public GrabCorePresenter(@NotNull IGrabView iGrebView, @NotNull RoomData roomData) {
        mIGameRuleView = iGrebView;
        mRoomData = roomData;
        TAG = "RankingCorePresenter";
        Params params = Params.getFromPref();
        EngineManager.getInstance().init("rankingroom", params);
        EngineManager.getInstance().joinRoom(String.valueOf(mRoomData.getGameId()), (int) UserAccountManager.getInstance().getUuidAsLong(), true);
        // 不发送本地音频
        EngineManager.getInstance().muteLocalAudioStream(true);
    }

    @Override
    public void start() {
        super.start();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mRoomData.checkRound();
        startSyncGameStateTask(sSyncStateTaskInterval);
    }


    public void vie() {

    }

    public void lightsOff() {

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
        mUiHanlder.removeCallbacksAndMessages(null);
    }

    /**
     * 上报轮次结束信息
     */
    public void sendRoundOverInfo() {
        MyLog.w(TAG, "上报我的演唱结束");
        estimateOverTsThisRound();

        // TODO: 2018/12/27 机器评分先写死，都给90分
        long timeMs = System.currentTimeMillis();
        int sysScore = 90;
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
        mHeartBeatTask = HandlerTaskTimer.newBuilder()
                .interval(sHeartBeatTaskInterval)
                .take(-1)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        sendHeartBeat();
                    }
                });
    }

    public void cancelHeartBeatTask(String from) {
        MyLog.w(TAG, "取消心跳，是从 " + from);
        if (mHeartBeatTask != null) {
            mHeartBeatTask.dispose();
        }
    }

    // 上报心跳，只有当前演唱者上报 2s一次
    public void sendHeartBeat() {
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

        mSyncGameStateTask = HandlerTaskTimer.newBuilder()
                .delay(delayTime)
                .interval(sSyncStateTaskInterval)
                .take(-1)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        MyLog.w(TAG, "12秒钟的 syncGameTask 去更新状态了");
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
//            mIGameRuleView.updateUserState(onlineInfos);
        }
        if (gameOverTimeMs != 0) {
            if (gameOverTimeMs > mRoomData.getGameStartTs()) {
                MyLog.w(TAG, "gameOverTimeMs ！= 0 游戏应该结束了");
                // 游戏结束了
                onGameOver("sync", gameOverTimeMs);
            } else {
                MyLog.w(TAG, "服务器结束时间不合法 startTs:" + mRoomData.getGameStartTs() + " overTs:" + gameOverTimeMs);
            }
        } else {
            // 没结束 current 不应该为null
            if (currentInfo != null) {
                // 服务下发的轮次已经大于当前轮次了，说明本地信息已经不对了，更新
                if (RankDataUtils.roundSeqLarger(currentInfo, mRoomData.getExpectRoundInfo())) {
                    MyLog.w(TAG, "updatePlayerState" + " sync发现本地轮次信息滞后，更新");
                    // 轮次确实比当前的高，可以切换
                    mRoomData.setExpectRoundInfo(currentInfo);
                    mRoomData.checkRound();
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
        if (event.myturn) {
            // 轮到我唱了
            // 开始发心跳
            if (U.getActivityUtils().isAppForeground()) {
                startHeartBeatTask();
            }

            mUiHanlder.post(new Runnable() {
                @Override
                public void run() {
                    // 开始倒计时 3 2 1
                    mIGameRuleView.startSelfCountdown(new Runnable() {
                        @Override
                        public void run() {
                            //再次确认
                            if (mRoomData.getRealRoundInfo() != null && mRoomData.getRealRoundInfo().getUserID() == MyUserInfoManager.getInstance().getUid()) {
                                // 开始开始混伴奏，开始解除引擎mute
                                File accFile = SongResUtils.getAccFileByUrl(mRoomData.getSongModel().getAcc());
                                if (accFile != null && accFile.exists()) {
                                    EngineManager.getInstance().muteLocalAudioStream(false);
                                    EngineManager.getInstance().startAudioMixing(accFile.getAbsolutePath(), false, false, 1);
                                    EngineManager.getInstance().setAudioMixingPosition(mRoomData.getSongModel().getBeginMs());
                                    // 还应开始播放歌词
//                                    mIGameRuleView.playLyric(mRoomData.getSongModel(), true);
//                                    mIGameRuleView.showLeftTime(mRoomData.getRealRoundInfo().getSingEndMs() - mRoomData.getRealRoundInfo().getSingBeginMs());
                                    MyLog.w(TAG, "本人开始唱了，歌词和伴奏响起");
                                }
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
            EngineManager.getInstance().stopAudioMixing();
            EngineManager.getInstance().muteLocalAudioStream(true);
            // 收到其他的人onMute消息 开始播放其他人的歌的歌词，应该提前下载好
            if (mRoomData.getRealRoundInfo() != null) {
                // 其他人演唱
                mUiHanlder.post(new Runnable() {
                    @Override
                    public void run() {
                        int uid = RankDataUtils.getUidOfRoundInfo(mRoomData.getRealRoundInfo());
                        String avatar = "";
                        PlayerInfoModel playerInfoModel = RankDataUtils.getPlayerInfoById(mRoomData, uid);
                        if(playerInfoModel != null && playerInfoModel.getUserInfo() != null){
                            avatar = playerInfoModel.getUserInfo().getAvatar();
                        }
//                        mIGameRuleView.startRivalCountdown(uid, avatar);
                        if (mRoomData.getRealRoundInfo() != null) {
                            MyLog.w(TAG, uid + "开始唱了，歌词走起,演唱的时间是：" + U.getDateTimeUtils().formatTimeStringForDate(mRoomData.getGameStartTs() + mRoomData.getRealRoundInfo().getSingBeginMs(), "HH:mm:ss:SSS")
                                    + "--" + U.getDateTimeUtils().formatTimeStringForDate(mRoomData.getGameStartTs() + mRoomData.getRealRoundInfo().getSingEndMs(), "HH:mm:ss:SSS"));
                        } else {
                            MyLog.w(TAG, "mRoomData.getRealRoundInfo() 为空啊！！！！");
                        }

//                        mIGameRuleView.playLyric(RankDataUtils.getPlayerSongInfoUserId(mRoomData.getPlayerInfoList(), uid), false);

                    }
                });
            } else if (mRoomData.getRealRoundInfo() == null) {
                if (mRoomData.getGameOverTs() > mRoomData.getGameStartTs()) {
                    // 取消轮询
                    cancelSyncGameStateTask();
                    // 游戏结束了,处理相应的ui逻辑
                    mUiHanlder.post(new Runnable() {
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

    /**
     * 游戏真的结束了
     */
    private void gameIsFinish() {
        mRoomData.setIsGameFinish(true);
        cancelHeartBeatTask("gameIsFinish");
        cancelSyncGameStateTask();
    }

    private void onGameOver(String from, long gameOverTs) {
        MyLog.w(TAG, "游戏结束 gameOverTs=" + gameOverTs + " from:" + from);
        if (gameOverTs > mRoomData.getGameStartTs() && gameOverTs > mRoomData.getGameOverTs()) {
            mRoomData.setGameOverTs(gameOverTs);
            mRoomData.setExpectRoundInfo(null);
            mRoomData.checkRound();
        }
    }

    private int estimateOverTsThisRound() {
        int pt = RankDataUtils.estimateTs2End(mRoomData, mRoomData.getRealRoundInfo());
        MyLog.w(TAG, "估算出距离本轮结束还有" + pt + "ms");
        return pt;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(EngineEvent event) {
        if (event.getType() == EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION) {
            if (RankDataUtils.isMyRound(mRoomData.getRealRoundInfo())) {
                if (event.getObj() != null) {
                    List<EngineEvent.UserVolumeInfo> list = (List<EngineEvent.UserVolumeInfo>) event.getObj();
                    for (EngineEvent.UserVolumeInfo info : list) {

                        if (info.getUid() == UserAccountManager.getInstance().getUuidAsLong()
                                || info.getUid() == 0) {
                            mUiHanlder.post(new Runnable() {
                                @Override
                                public void run() {
//                                    mIGameRuleView.updateScrollBarProgress(info.getVolume() / 3);
                                }
                            });
                            break;
                        }
                    }
                }
            }
        } else if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_FINISH) {
            //伴奏播放结束了也发结束轮次的通知了
            sendRoundOverInfo();
        } else if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_TIME_FLY_LISTENER) {
            EngineEvent.MixMusicTimeInfo timeInfo = (EngineEvent.MixMusicTimeInfo) event.getObj();
            if (timeInfo.getCurrent() >= mRoomData.getSongModel().getEndMs()) {
                //可以发结束轮次的通知了
                sendRoundOverInfo();
            }
        } else if (event.getType() == EngineEvent.TYPE_USER_MUTE_AUDIO) {
            int muteUserId = event.getUserStatus().getUserId();
            RoundInfoModel infoModel = RankDataUtils.getRoundInfoByUserId(mRoomData.getRoundInfoModelList(), muteUserId);
            if (!event.getUserStatus().isAudioMute()) {
                MyLog.w(TAG, "EngineEvent muteUserId=" + muteUserId + "解麦了");
                /**
                 * 用户开始解开mute了，说明某个用户自己认为轮到自己唱了
                 * 这里考虑下要不要加个判断，如果当前轮次是这个用户，才播放他的歌词
                 * 就是是自己状态对，还是别人状态对的问题，这里先认为自己状态对.
                 * 状态依赖服务器
                 */
                if (infoModel != null) {
                    if (RankDataUtils.roundInfoEqual(infoModel, mRoomData.getRealRoundInfo())) {
                        //正好相等，没问题,放歌词
                        MyLog.w(TAG, "是当前轮次，没问题,放歌词");
                        mUiHanlder.post(new Runnable() {
                            @Override
                            public void run() {
                                MyLog.d(TAG, "引擎监测到有人开始唱了，正好是当前的人，播放歌词 这个人的id是" + muteUserId);
//                                mIGameRuleView.playLyric(RankDataUtils.getPlayerSongInfoUserId(mRoomData.getPlayerInfoList(), muteUserId), true);
                            }
                        });
                    } else if (RankDataUtils.roundSeqLarger(infoModel, mRoomData.getExpectRoundInfo())) {
                        // 假设演唱的轮次在当前轮次后面，说明本地滞后了
                        MyLog.w(TAG, "演唱的轮次在当前轮次后面，说明本地滞后了,矫正并放歌词");
                        mRoomData.setExpectRoundInfo(infoModel);
                        mRoomData.checkRound();
                        mUiHanlder.post(new Runnable() {
                            @Override
                            public void run() {
                                MyLog.w(TAG, "引擎监测到有人开始唱了，演唱的轮次在当前轮次后面，说明本地滞后了,矫正并放歌词  这个人的id是" + muteUserId);
//                                mIGameRuleView.playLyric(RankDataUtils.getPlayerSongInfoUserId(mRoomData.getPlayerInfoList(), muteUserId), true);
                            }
                        });
                    }
                } else {
                    MyLog.w(TAG, "引擎监测到有人开始唱了， 找不到该人的轮次信息？？？为什么？？？");
                }
            } else {
                /**
                 * 有人闭麦了，可以考虑加个逻辑，如果闭麦的人是当前演唱的人
                 * 说明此人演唱结束，可以考虑进入下一轮
                 */
                MyLog.w(TAG, "引擎监测到有人有人闭麦了，id是" + muteUserId);
            }
        }
    }

    // 游戏轮次结束的通知消息（在某人向服务器短连接成功后推送)
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventMainThread(RoundOverEvent roundOverEvent) {
        MyLog.w(TAG, "收到服务器的某一个人轮次结束的push，id是 " + roundOverEvent.currenRound.getUserID()
                + ", exitUserID 是 " + roundOverEvent.exitUserID + " timets 是" + roundOverEvent.info.getTimeMs());
        if (RankDataUtils.roundInfoEqual(roundOverEvent.currenRound, mRoomData.getRealRoundInfo())) {
            // 确实等于当前轮次
            if (mRoomData.getRealRoundInfo() != null) {
                MyLog.w(TAG, "确实是当前轮次结束了");
                mRoomData.getRealRoundInfo().setEndTs(roundOverEvent.roundOverTimeMs);
            }
        }
        // 游戏轮次结束
        if (RankDataUtils.roundSeqLarger(roundOverEvent.nextRound, mRoomData.getExpectRoundInfo())) {
            // 轮次确实比当前的高，可以切换
            MyLog.w(TAG, "轮次确实比当前的高，可以切换");
            mRoomData.setExpectRoundInfo(roundOverEvent.nextRound);
            mRoomData.checkRound();
        }

        if (roundOverEvent.exitUserID != 0) {
            // TODO: 2018/12/18 有人退出了
        }
    }

    //轮次和游戏结束通知，除了已经结束状态，别的任何状态都要变成
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoundAndGameOverEvent roundAndGameOverEvent) {
        MyLog.w(TAG, "收到服务器的游戏结束的push timets 是 " + roundAndGameOverEvent.info.getTimeMs());
        onGameOver("push", roundAndGameOverEvent.roundOverTimeMs);
        cancelSyncGameStateTask();

        if (roundAndGameOverEvent.mVoteInfoModels != null && roundAndGameOverEvent.mVoteInfoModels.size() > 0) {
            //不需要跳转评论页,直接跳转战绩页
            mIGameRuleView.showRecordView(new RecordData(roundAndGameOverEvent.mVoteInfoModels, roundAndGameOverEvent.mScoreDetailModel));
        } else {
//            mIGameRuleView.showVoteView();
        }
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

    // TODO: 2018/12/18 退出游戏了
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ExitGameEvent exitGameEvent) {
        MyLog.w(TAG, "收到一个人退出的push了，type是" + exitGameEvent.type + ",timeMs是" + exitGameEvent.info.getTimeMs());
        if (exitGameEvent.type == EXIT_GAME_AFTER_PLAY) {   //我在唱歌，有一个人退出
            U.getToastUtil().showShort("游戏结束后，某一个人退出了");
        } else if (exitGameEvent.type == EXIT_GAME_OUT_ROUND) {   //我是观众，有一个人退出
            U.getToastUtil().showShort("游戏中，某一个人退出了");
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
    }

}
