package com.module.rankingmode.room.presenter;

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
import com.module.rankingmode.msg.event.AppSwapEvent;
import com.module.rankingmode.msg.event.ExitGameEvent;
import com.module.rankingmode.msg.event.RoundAndGameOverEvent;
import com.module.rankingmode.msg.event.RoundOverEvent;
import com.module.rankingmode.msg.event.SyncStatusEvent;
import com.module.rankingmode.prepare.model.OnLineInfoModel;
import com.module.rankingmode.prepare.model.RoundInfoModel;
import com.module.rankingmode.room.RoomServerApi;
import com.module.rankingmode.room.event.RoundInfoChangeEvent;
import com.module.rankingmode.room.model.RoomData;
import com.module.rankingmode.room.model.RoomDataUtils;
import com.module.rankingmode.room.view.IGameRuleView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.module.rankingmode.msg.event.ExitGameEvent.EXIT_GAME_AFTER_PLAY;
import static com.module.rankingmode.msg.event.ExitGameEvent.EXIT_GAME_OUT_ROUND;

public class RankingCorePresenter extends RxLifeCyclePresenter {
    String TAG = "RankingCorePresenter";
    private static long heartBeatTaskInterval = 3000;
    private static long checkStateTaskDelay = 10000;
    private static long syncStateTaskInterval = 12000;

    RoomData mRoomData;

    RoomServerApi mRoomServerApi = ApiManager.getInstance().createService(RoomServerApi.class);

    HandlerTaskTimer mHeartBeatTask;

    HandlerTaskTimer mSyncGameStateTask;

    IGameRuleView mIGameRuleView;

    Handler mUiHanlder = new Handler();

    public RankingCorePresenter(@NotNull IGameRuleView iGameRuleView, @NotNull RoomData roomData) {
        mIGameRuleView = iGameRuleView;
        mRoomData = roomData;
        TAG = "RankingCorePresenter_" + mRoomData.getGameId();
        Params params = Params.getFromPref();
        EngineManager.getInstance().init("rankingroom", params);
        EngineManager.getInstance().joinRoom(String.valueOf(mRoomData.getGameId()), (int) UserAccountManager.getInstance().getUuidAsLong(), true);
        // 不发送本地音频
        EngineManager.getInstance().muteLocalAudioStream(true);
    }

    public RoomData getRoomData() {
        return mRoomData;
    }

    @Override
    public void start() {
        super.start();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        mRoomData.checkRound();
        startSyncGameStateTask(checkStateTaskDelay);
    }

    @Override
    public void destroy() {
        super.destroy();
        exitGame();
        cancelHeartBeatTask();
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
        MyLog.d(TAG, "上报我的演唱结束");
        mIGameRuleView.showMsg("去上报演唱结束");
        estimateOverTsThisRound();

        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());

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
                    mIGameRuleView.showMsg("演唱结束上报成功");
                    MyLog.d(TAG, "我的演唱结束上报成功");
                    // 尝试自己切换到下一个轮次
                    if (finalRoundSeq >= 0) {
                        RoundInfoModel roundInfoModel = RoomDataUtils.findRoundInfoBySeq(mRoomData.getRoundInfoModelList(), finalRoundSeq + 1);
                        mRoomData.setExpectRoundInfo(roundInfoModel);
                        mRoomData.checkRound();
                    }
                } else {
                    mIGameRuleView.showMsg("演唱结束上报失败");
                    U.getToastUtil().showShort("请求轮次结束失败");
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.d(TAG, "sendRoundOverInfo" + " error " + e);
                mIGameRuleView.showMsg("演唱结束上报失败");
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
        ApiMethods.subscribe(mRoomServerApi.exitGame(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    U.getToastUtil().showShort("退出游戏成功");
                } else {
                    MyLog.e(TAG, "exitGame result errno is " + result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, "exitGame error " + e);
            }
        }, this);
    }

    /**
     * 游戏切后台或切回来
     *
     * @param out 切出去
     * @param in  切回来
     */
    public void swapGame(boolean out, boolean in) {
        MyLog.d(TAG, "swapGame" + " out=" + out + " in=" + in);
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());
        map.put("out", out);
        map.put("in", in);

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
        cancelHeartBeatTask();
        mHeartBeatTask = HandlerTaskTimer.newBuilder()
                .interval(heartBeatTaskInterval)
                .take(-1)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        sendHeartBeat();
                    }
                });
    }

    public void cancelHeartBeatTask() {
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
                    mIGameRuleView.showMsg("心跳ok");
                } else {
                    MyLog.e(TAG, "sendHeartBeat " + result.getErrmsg());
                    MyLog.e(TAG, "sendHeartBeat traceId" + result.getTraceId());
                    mIGameRuleView.showMsg("心跳 not ok,resule code is " + result.getErrno());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, "sendHeartBeat errer " + e);
                mIGameRuleView.showMsg("心跳 not ok,errer is  " + e);
            }
        }, this);
    }

    /**
     * 轮询同步状态task
     */
    public void startSyncGameStateTask(long delayTime) {
        cancelSyncGameStateTask();
        mSyncGameStateTask = HandlerTaskTimer.newBuilder()
                .delay(delayTime)
                .interval(syncStateTaskInterval)
                .take(-1)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        mIGameRuleView.showMsg("12秒钟的 syncGameTask 去更新状态了");
                        MyLog.d(TAG, "startSyncGameStateTask" + " integer=" + integer + " exec time = " + System.currentTimeMillis());
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
                    mIGameRuleView.showMsg("syncGameStatus成功了");
                    long syncStatusTimes = result.getData().getLong("syncStatusTimeMs");  //状态同步时的毫秒时间戳
                    long gameOverTimeMs = result.getData().getLong("gameOverTimeMs");  //游戏结束时间
                    MyLog.d("AAAAAA", " syncGameStatus " + " gameOverTimeMs =" + gameOverTimeMs);
                    List<OnLineInfoModel> onlineInfos = JSON.parseArray(result.getData().getString("onlineInfo"), OnLineInfoModel.class); //在线状态
                    RoundInfoModel currentInfo = JSON.parseObject(result.getData().getString("currentRound"), RoundInfoModel.class); //当前轮次信息
                    RoundInfoModel nextInfo = JSON.parseObject(result.getData().getString("nextRound"), RoundInfoModel.class); //下个轮次信息
                    updatePlayerState(gameOverTimeMs, syncStatusTimes, onlineInfos, currentInfo, nextInfo);
                } else {
                    MyLog.e(TAG, "syncGameStatus " + result.getErrmsg());
                    mIGameRuleView.showMsg("syncGameStatus失败了，errno是" + result.getErrno());
                    estimateOverTsThisRound();
                }
            }

            @Override
            public void onError(Throwable e) {
                mIGameRuleView.showMsg("syncGameStatus失败了，errno是" + e);
                MyLog.e(TAG, "syncGameStatus error " + e);
            }
        }, this);
    }

    /**
     * 根据时间戳更新选手状态,目前就只有两个入口，SyncStatusEvent push了sycn，不写更多入口
     */
    private synchronized void updatePlayerState(long gameOverTimeMs, long syncStatusTimes, List<OnLineInfoModel> onlineInfos, RoundInfoModel currentInfo, RoundInfoModel nextInfo) {
        MyLog.d(TAG, "updatePlayerState" + " gameOverTimeMs=" + gameOverTimeMs + " syncStatusTimes=" + syncStatusTimes + " onlineInfos=" + onlineInfos + " currentInfo=" + currentInfo + " nextInfo=" + nextInfo);
        mIGameRuleView.showMsg("updatePlayerState");
        if (syncStatusTimes > mRoomData.getLastSyncTs()) {
            mRoomData.setLastSyncTs(syncStatusTimes);
            mRoomData.setOnlineInfoList(onlineInfos);
            mIGameRuleView.updateUserState(onlineInfos);
        }
        if (gameOverTimeMs != 0) {
            if (gameOverTimeMs > mRoomData.getGameStartTs()) {
                mIGameRuleView.showMsg("gameOverTimeMs ！= 0 游戏应该结束了");
                // 游戏结束了
                onGameOver("sync", gameOverTimeMs);
            } else {
                mIGameRuleView.showMsg("服务器结束时间不合法 startTs:" + mRoomData.getGameStartTs() + " overTs:" + gameOverTimeMs);

                MyLog.w(TAG, "服务器结束时间不合法 startTs:" + mRoomData.getGameStartTs() + " overTs:" + gameOverTimeMs);
            }
        } else {
            // 没结束 current 不应该为null
            if (currentInfo != null) {
                // 服务下发的轮次已经大于当前轮次了，说明本地信息已经不对了，更新
                if (RoomDataUtils.roundSeqLarger(currentInfo, mRoomData.getExpectRoundInfo())) {
                    MyLog.d(TAG, "updatePlayerState" + " sync发现本地轮次信息滞后，更新");
                    // 轮次确实比当前的高，可以切换
                    mRoomData.setExpectRoundInfo(currentInfo);
                    mRoomData.checkRound();
                }
            } else {
                mIGameRuleView.showMsg("服务器结束时间不合法 currentInfo=null");
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
        MyLog.d(TAG, "RoundInfoChangeEvent  轮次变更事件发生 " + " myturn=" + event.myturn);
        mIGameRuleView.showMsg("开始切换唱将 myturn=" + event.myturn);
        estimateOverTsThisRound();
        if (event.myturn) {
            // 轮到我唱了
            // 开始发心跳
            startHeartBeatTask();
            // 开始倒计时 3 2 1
            mIGameRuleView.startSelfCountdown(new Runnable() {
                @Override
                public void run() {
                    // 开始开始混伴奏，开始解除引擎mute
                    File accFile = SongResUtils.getAccFileByUrl(mRoomData.getSongModel().getAcc());
                    if (accFile != null && accFile.exists()) {
                        EngineManager.getInstance().muteLocalAudioStream(false);
                        EngineManager.getInstance().startAudioMixing(accFile.getAbsolutePath(), false, false, 1);
                        EngineManager.getInstance().setAudioMixingPosition(mRoomData.getSongModel().getBeginMs());
                        // 还应开始播放歌词
                        mIGameRuleView.playLyric(mRoomData.getSongModel());
                        mIGameRuleView.showMsg("开始唱了，歌词和伴奏响起");
                    }
                }
            });

            mIGameRuleView.showMsg("演唱的时间是：" + U.getDateTimeUtils().formatTimeStringForDate(mRoomData.getGameStartTs() + mRoomData.getRealRoundInfo().getSingBeginMs(), "HH:mm:ss:SSS")
                    + "--" + U.getDateTimeUtils().formatTimeStringForDate(mRoomData.getGameStartTs() + mRoomData.getRealRoundInfo().getSingEndMs(), "HH:mm:ss:SSS"));
        } else {
            MyLog.d(TAG, "不是我的轮次，停止发心跳，停止混音，闭麦");
            cancelHeartBeatTask();
            EngineManager.getInstance().stopAudioMixing();
            EngineManager.getInstance().muteLocalAudioStream(true);
            // 收到其他的人onMute消息 开始播放其他人的歌的歌词，应该提前下载好
            if (mRoomData.getRealRoundInfo() != null) {
                // 其他人演唱
                mUiHanlder.post(new Runnable() {
                    @Override
                    public void run() {
                        int uid = RoomDataUtils.getUidOfRoundInfo(mRoomData.getRealRoundInfo());
                        mIGameRuleView.startRivalCountdown(uid);
                        mIGameRuleView.showMsg("演唱的时间是：" + U.getDateTimeUtils().formatTimeStringForDate(mRoomData.getGameStartTs() + mRoomData.getRealRoundInfo().getSingBeginMs(), "HH:mm:ss:SSS")
                                + "--" + U.getDateTimeUtils().formatTimeStringForDate(mRoomData.getGameStartTs() + mRoomData.getRealRoundInfo().getSingEndMs(), "HH:mm:ss:SSS"));
//                        mUiHanlder.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                //因为在三秒钟之内可能发生了变化，所以需要再判断一下
//                                if (uid == RoomDataUtils.getUidOfRoundInfo(mRoomData.getRealRoundInfo())) {
//                                    mIGameRuleView.playLyric(mRoomData.getRealRoundInfo().getPlaybookID());
//                                    mIGameRuleView.showMsg(uid + "开始唱了，伴奏走起 one");
//                                }
//                            }
//                        }, 3000);
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
                        }
                    });
                } else {
                    MyLog.d(TAG, "结束时间比开始时间小，不应该吧 startTs:" + mRoomData.getGameStartTs() + " overTs:" + mRoomData.getGameOverTs());
                }
            }
        }
    }

    private void onGameOver(String from, long gameOverTs) {
        MyLog.d(TAG, "游戏结束 gameOverTs=" + gameOverTs + " from:" + from);
        if (gameOverTs > mRoomData.getGameStartTs() && gameOverTs > mRoomData.getGameOverTs()) {
            mRoomData.setGameOverTs(gameOverTs);
            mRoomData.setExpectRoundInfo(null);
            mRoomData.checkRound();
        }
    }

    private int estimateOverTsThisRound() {
        int pt = RoomDataUtils.estimateTs2End(mRoomData, mRoomData.getRealRoundInfo());
        MyLog.w(TAG, "估算出距离本轮结束还有" + pt + "ms");
        return pt;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(EngineEvent event) {
        if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_FINISH) {
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
            RoundInfoModel infoModel = RoomDataUtils.getRoundInfoByUserId(mRoomData.getRoundInfoModelList(), muteUserId);
            if (!event.getUserStatus().isAudioMute()) {
                MyLog.w(TAG, "EngineEvent muteUserId=" + muteUserId + "解麦了");
                /**
                 * 用户开始解开mute了，说明某个用户自己认为轮到自己唱了
                 * 这里考虑下要不要加个判断，如果当前轮次是这个用户，才播放他的歌词
                 * 就是是自己状态对，还是别人状态对的问题，这里先认为自己状态对.
                 * 状态依赖服务器
                 */
                if (infoModel != null) {
                    if (RoomDataUtils.roundInfoEqual(infoModel, mRoomData.getRealRoundInfo())) {
                        //正好相等，没问题,放歌词
                        MyLog.w(TAG, "是当前轮次，没问题,放歌词");
                        mUiHanlder.post(new Runnable() {
                            @Override
                            public void run() {
                                mIGameRuleView.showMsg("引擎监测到有人开始唱了，正好是当前的人，播放歌词 这个人的id是" + muteUserId);
                                mIGameRuleView.playLyric(RoomDataUtils.getPlayerInfoUserId(mRoomData.getPlayerInfoList(), muteUserId));
                            }
                        });
                    } else if (RoomDataUtils.roundSeqLarger(infoModel, mRoomData.getExpectRoundInfo())) {
                        // 假设演唱的轮次在当前轮次后面，说明本地滞后了
                        MyLog.w(TAG, "演唱的轮次在当前轮次后面，说明本地滞后了,矫正并放歌词");
                        mRoomData.setExpectRoundInfo(infoModel);
                        mRoomData.checkRound();
                        mUiHanlder.post(new Runnable() {
                            @Override
                            public void run() {
                                mIGameRuleView.showMsg("引擎监测到有人开始唱了，演唱的轮次在当前轮次后面，说明本地滞后了,矫正并放歌词  这个人的id是" + muteUserId);
                                mIGameRuleView.playLyric(RoomDataUtils.getPlayerInfoUserId(mRoomData.getPlayerInfoList(), muteUserId));
                            }
                        });
                    }
                } else {
                    mIGameRuleView.showMsg("引擎监测到有人开始唱了， 找不到该人的轮次信息？？？为什么？？？");
                    MyLog.w(TAG, "找不到该人的轮次信息？？？为什么？？？");
                }
            } else {
                /**
                 * 有人闭麦了，可以考虑加个逻辑，如果闭麦的人是当前演唱的人
                 * 说明此人演唱结束，可以考虑进入下一轮
                 */
                mIGameRuleView.showMsg("引擎监测到有人有人闭麦了，id是" + muteUserId);
                MyLog.w(TAG, "EngineEvent muteUserId=" + muteUserId + "闭麦了");
            }
        }
    }

    // 游戏轮次结束的通知消息（在某人向服务器短连接成功后推送)
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventMainThread(RoundOverEvent roundOverEvent) {
        MyLog.d(TAG, "onRoundOverEvent 轮次结束push通知:" + roundOverEvent.currenRound + " nextRound:" + roundOverEvent.nextRound);
        mIGameRuleView.showMsg("收到服务器的某一个人结束的push，轮次结束的人是 " + roundOverEvent.currenRound.getUserID()+ ", exitUserID 是 " + roundOverEvent.exitUserID);
        if (RoomDataUtils.roundInfoEqual(roundOverEvent.currenRound, mRoomData.getRealRoundInfo())) {
            // 确实等于当前轮次
            if (mRoomData.getRealRoundInfo() != null) {
                mIGameRuleView.showMsg("确实是当前轮次结束了");
                mRoomData.getRealRoundInfo().setEndTs(roundOverEvent.roundOverTimeMs);
            }
        }
        // 游戏轮次结束
        if (RoomDataUtils.roundSeqLarger(roundOverEvent.nextRound, mRoomData.getExpectRoundInfo())) {
            // 轮次确实比当前的高，可以切换
            mIGameRuleView.showMsg("轮次确实比当前的高，可以切换");
            mRoomData.setExpectRoundInfo(roundOverEvent.nextRound);
            mRoomData.checkRound();
        }

        if (roundOverEvent.exitUserID != 0) {
            // TODO: 2018/12/18 有人退出了
        }
    }

    //轮次和游戏结束通知，除了已经结束状态，别的任何状态都要变成
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventMainThread(RoundAndGameOverEvent roundAndGameOverEvent) {
        MyLog.d(TAG, "onEventMainThread 游戏结束push通知");
        mIGameRuleView.showMsg("收到服务器的游戏结束的push");
        onGameOver("push", roundAndGameOverEvent.roundOverTimeMs);
    }

    // 应用进程切到后台通知
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AppSwapEvent appSwapEvent) {
        MyLog.d(TAG, "onEventMainThread syncStatusEvent");
    }

    // 长连接 状态同步信令， 以11秒为单位检测
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SyncStatusEvent syncStatusEvent) {
        MyLog.d(TAG, "onEventMainThread receive syncStatusEvent");
        mIGameRuleView.showMsg("收到服务器更新状态的push了");
        startSyncGameStateTask(checkStateTaskDelay);
        updatePlayerState(syncStatusEvent.gameOverTimeMs, syncStatusEvent.syncStatusTimes, syncStatusEvent.onlineInfos, syncStatusEvent.currentInfo, syncStatusEvent.nextInfo);
    }

    // TODO: 2018/12/18 退出游戏了
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ExitGameEvent exitGameEvent) {
        MyLog.d(TAG, "onEventMainThread syncStatusEvent");
        mIGameRuleView.showMsg("收到一个人退出的push了，");
        if (exitGameEvent.type == EXIT_GAME_AFTER_PLAY) {
            U.getToastUtil().showShort("游戏开始后，某一个人退出了");
        } else if (exitGameEvent.type == EXIT_GAME_OUT_ROUND) {
            U.getToastUtil().showShort("不在你的轮次，某一个人退出了");
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(ActivityUtils.ForeOrBackgroundChange event) {
        mIGameRuleView.showMsg("一个人退出到后台了");
        swapGame(!event.foreground, event.foreground);
    }

}
