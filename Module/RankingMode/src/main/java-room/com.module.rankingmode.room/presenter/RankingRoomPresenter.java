package com.module.rankingmode.room.presenter;

import com.alibaba.fastjson.JSON;
import com.common.core.account.UserAccountManager;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.module.rankingmode.msg.event.AppSwapEvent;
import com.module.rankingmode.msg.event.QuitGameEvent;
import com.module.rankingmode.msg.event.RoundAndGameOverEvent;
import com.module.rankingmode.msg.event.RoundOverEvent;
import com.module.rankingmode.msg.event.SyncStatusEvent;
import com.module.rankingmode.prepare.model.JsonOnLineInfo;
import com.module.rankingmode.prepare.model.JsonRoundInfo;
import com.module.rankingmode.room.GameTimeManager;
import com.module.rankingmode.room.RoomServerApi;
import com.module.rankingmode.room.model.RoomData;
import com.module.rankingmode.room.view.IGameRuleView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.greendao.annotation.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class RankingRoomPresenter extends RxLifeCyclePresenter {
    public final static String TAG = "RankingRoomPresenter";
    private static long heartBeatTaskInterval = 2000;
    private static long syncStateTaskInterval = 12000;

    RoomData mRoomData;

    RoomServerApi roomServerApi = ApiManager.getInstance().createService(RoomServerApi.class);

    GameTimeManager mGameTimeManager;

    HandlerTaskTimer heartBeatTask;

    HandlerTaskTimer syncGameStateTask;

    //服务器返回的最近的时间
    private long latestUpateTime = 0;

    IGameRuleView mIGameRuleView;

    //保存所有
    List<JsonOnLineInfo> onlineInfos;

    //当前上场的人，包括三秒倒计时
    JsonRoundInfo mCurrentRound;

    //下一个要上场的人
    JsonRoundInfo mNextRound;

    public RankingRoomPresenter(@NotNull IGameRuleView iGameRuleView, @NotNull RoomData roomData) {
        mIGameRuleView = iGameRuleView;
        mRoomData = roomData;
        mGameTimeManager = new GameTimeManager();
        mGameTimeManager.init(roomData.getGameId());
        mGameTimeManager.startGame(mRoomData.getGameReadyInfo().getJsonGameStartInfo().getStartPassedMs());

        //一进游戏界面就拉一下，更新状态
        syncGameStatus(mRoomData.getGameId());
    }

    @Override
    public void start() {
        super.start();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        cancelHeartBeatTask();
        cancelSyncGameStateTask();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }


    // 上报轮次结束信息
    public void sendRoundOverInfo(int gameID) {
        MyLog.d(TAG, "sendRoundOver" + " gameID=" + gameID);
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", gameID);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(roomServerApi.sendRoundOver(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    MyLog.d(TAG, "sendRoundOverInfo" + " result=" + 0);
                } else {
                    MyLog.d(TAG, "sendRoundOverInfo" + " result=" + result);
                }

                syncGameStatus(mRoomData.getGameId());
            }

            @Override
            public void onError(Throwable e) {
                MyLog.d(TAG, "sendRoundOverInfo" + " error " + e);
            }
        }, this);
    }

    public void cancelHeartBeatTask() {
        if (heartBeatTask != null) {
            heartBeatTask.dispose();
        }
    }

    //开始发心跳
    public void startHeartBeatTask() {
        cancelHeartBeatTask();
        heartBeatTask = HandlerTaskTimer.newBuilder()
                .interval(heartBeatTaskInterval)
                .take(-1)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        sendHeartBeat();
                    }
                });
    }

    //同步状态task
    public void startSyncGameStateTask() {
        cancelSyncGameStateTask();
        syncGameStateTask = HandlerTaskTimer.newBuilder()
                .interval(syncStateTaskInterval)
                .take(-1)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        syncGameStatus(mRoomData.getGameId());
                    }
                });
    }

    public void cancelSyncGameStateTask() {
        if (syncGameStateTask != null) {
            syncGameStateTask.dispose();
        }
    }

    // 上报心跳，只有当前演唱者上报 2s一次
    public void sendHeartBeat() {
        MyLog.d(TAG, "sendHeartBeat" + " gameID=" + mRoomData.getGameId());
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());
        map.put("userID", UserAccountManager.getInstance().getUuid());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(roomServerApi.sendHeartBeat(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    // TODO: 2018/12/13  当前postman返回的为空 待补充
                } else {
                    MyLog.e(TAG, "sendHeartBeat " + result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, "sendHeartBeat errer " + e);
            }
        }, this);
    }

    // 同步游戏详情状态(检测不到长连接调用)
    public void syncGameStatus(int gameID) {
        ApiMethods.subscribe(roomServerApi.syncGameStatus(gameID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    long syncStatusTimes = result.getData().getLong("syncStatusTimeMs");  //状态同步时的毫秒时间戳
                    long gameOverTimeMs = result.getData().getLong("gameOverTimeMs");  //游戏结束时间
                    List<JsonOnLineInfo> onlineInfos = JSON.parseArray(result.getData().getString("onlineInfo"), JsonOnLineInfo.class); //在线状态
                    JsonRoundInfo currentInfo = JSON.parseObject(result.getData().getString("currentRound"), JsonRoundInfo.class); //当前轮次信息
                    JsonRoundInfo nextInfo = JSON.parseObject(result.getData().getString("nextRound"), JsonRoundInfo.class); //下个轮次信息
                    updatePlayerState(gameOverTimeMs, syncStatusTimes, onlineInfos, currentInfo, nextInfo);
                } else {
                    MyLog.e(TAG, "syncGameStatus " + result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, "syncGameStatus error " + e);
            }
        }, this);
    }

    /**
     * 根据时间戳更新选手状态,目前就只有两个入口，SyncStatusEvent push了sycn，不写更多入口
     */
    private synchronized void updatePlayerState(long gameOverTimeMs, long syncStatusTimes, List<JsonOnLineInfo> onlineInfos, JsonRoundInfo currentInfo, JsonRoundInfo nextInfo) {
        if (syncStatusTimes > latestUpateTime) {
            latestUpateTime = syncStatusTimes;
            mGameTimeManager.serverPassTime(gameOverTimeMs);
            this.onlineInfos = new ArrayList<>(onlineInfos);
            mIGameRuleView.updateUserState(onlineInfos);
            parseRoundInfo(currentInfo, nextInfo);
        }
    }

    /**
     * 这里可以认为是更新轮次的信息
     */
    private void parseRoundInfo(JsonRoundInfo currentInfo, JsonRoundInfo nextInfo) {
        if (currentInfo == null) {
            MyLog.e(TAG, "parseRoundInfo currentInfo is null");
            return;
        }

        if (currentInfo != null && currentInfo.getSingBeginMs() - mGameTimeManager.getGamePassTime() <= 3) {
            if (UserAccountManager.getInstance().getUuidAsLong() == currentInfo.getUserID()) {
                mIGameRuleView.startSelfCountdown();
            } else {
                mIGameRuleView.startRivalCountdown();
            }
        }

        if (nextInfo != null && currentInfo.getSingBeginMs() - mGameTimeManager.getGamePassTime() <= 3) {
            if (UserAccountManager.getInstance().getUuidAsLong() == nextInfo.getUserID()) {
                mIGameRuleView.startSelfCountdown();
            } else {
                mIGameRuleView.startRivalCountdown();
            }
        }
    }

    // 退出游戏
    public void exitGame(int gameID) {
        MyLog.d(TAG, "exitGame" + " gameID=" + gameID);
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", gameID);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(roomServerApi.exitGame(body), new ApiObserver<ApiResult>() {
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


    // 游戏轮次结束的通知消息（在某人向服务器短连接成功后推送)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoundOverEvent roundOverEvent) {
        MyLog.d(TAG, "onEventMainThread RoundOverEvent");
        // 游戏轮次结束
        syncGameStatus(mRoomData.getGameId());
    }


    //轮次和游戏结束通知，除了已经结束状态，别的任何状态都要变成
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoundAndGameOverEvent roundAndGameOverEvent) {
        MyLog.d(TAG, "onEventMainThread RoundOverEvent");
    }


    // 退出游戏的通知
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(QuitGameEvent quitGameEvent) {
        MyLog.d(TAG, "onEventMainThread syncStatusEvent");
        U.getToastUtil().showShort("某一个人退出了");
    }


    // 应用进程切到后台通知
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AppSwapEvent appSwapEvent) {
        MyLog.d(TAG, "onEventMainThread syncStatusEvent");
    }

    // 长连接 状态同步信令， 以11秒为单位检测
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SyncStatusEvent syncStatusEvent) {
        MyLog.d(TAG, "onEventMainThread syncStatusEvent");
        //从新开始12秒后拉取
        cancelHeartBeatTask();
        startSyncGameStateTask();
        updatePlayerState(syncStatusEvent.gameOverTimeMs, syncStatusEvent.syncStatusTimes, syncStatusEvent.onlineInfos, syncStatusEvent.currentInfo, syncStatusEvent.nextInfo);
    }
}
