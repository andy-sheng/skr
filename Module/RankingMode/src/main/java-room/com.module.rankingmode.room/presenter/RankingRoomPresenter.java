package com.module.rankingmode.room.presenter;

import com.alibaba.fastjson.JSON;
import com.common.core.account.UserAccountManager;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.rankingmode.msg.event.AppSwapEvent;
import com.module.rankingmode.msg.event.QuitGameEvent;
import com.module.rankingmode.msg.event.RoundAndGameOverEvent;
import com.module.rankingmode.msg.event.RoundOverEvent;
import com.module.rankingmode.msg.event.SyncStatusEvent;
import com.module.rankingmode.prepare.model.JsonRoundInfo;
import com.module.rankingmode.room.RoomServerApi;
import com.zq.live.proto.Room.RoundAndGameOverMsg;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class RankingRoomPresenter extends RxLifeCyclePresenter {

    public final static String TAG = "RankingRoomPresenter";

    RoomServerApi roomServerApi = ApiManager.getInstance().createService(RoomServerApi.class);

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
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }


    // 上报轮次结束信息
    public void sendRoundOver(int gameID) {
        MyLog.d(TAG, "sendRoundOver" + " gameID=" + gameID);
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", gameID);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(roomServerApi.sendRoundOver(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    long roundOverTimeMs = result.getData().getLong("roundOverTimeMs");
                    JsonRoundInfo currentRound = JSON.parseObject(result.getData().getString("currentRound"), JsonRoundInfo.class);
                    JsonRoundInfo nextRound = JSON.parseObject(result.getData().getString("nextRound"), JsonRoundInfo.class);
                }

            }
        }, this);
    }

    // 上报心跳，只有当前演唱者上报 2s一次
    public void sendHeartBeat(int gameID) {
        MyLog.d(TAG, "sendHeartBeat" + " gameID=" + gameID);
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", gameID);
        map.put("userID", UserAccountManager.getInstance().getUuid());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(roomServerApi.sendHeartBeat(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    // TODO: 2018/12/13  当前postman返回的为空 待补充
                }

            }
        }, this);
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
                }

            }
        }, this);
    }


    // 游戏轮次结束的通知消息（在某人向服务器短连接成功后推送）
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoundOverEvent roundOverEvent) {
        MyLog.d(TAG, "onEventMainThread RoundOverEvent");
        if (roundOverEvent != null) {
            // 游戏轮次结束
            long roundOverTime = roundOverEvent.roundOverTimeMs;
            JsonRoundInfo curren = roundOverEvent.currenRound;
            JsonRoundInfo next = roundOverEvent.nextRound;

        }
    }


//    // 轮次和游戏结束通知
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onEventMainThread(RoundAndGameOverEvent roundAndGameOverEvent) {
//        MyLog.d(TAG, "onEventMainThread RoundOverEvent");
//        if (roundAndGameOverEvent != null) {
//            // 游戏轮次结束
//
//        }
//    }


    // 退出游戏的通知
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(QuitGameEvent quitGameEvent) {
        MyLog.d(TAG, "onEventMainThread syncStatusEvent");
        if (quitGameEvent != null) {

        }
    }


    // 应用进程切到后台通知
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AppSwapEvent appSwapEvent) {
        MyLog.d(TAG, "onEventMainThread syncStatusEvent");
        if (appSwapEvent != null) {
            // 游戏轮次结束

        }
    }

    // 长连接 状态同步信令， 以11秒为单位检测
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SyncStatusEvent syncStatusEvent) {
        MyLog.d(TAG, "onEventMainThread syncStatusEvent");
        if (syncStatusEvent != null) {
            // 状态同步

        }
    }


}
