package com.module.rankingmode.room.presenter;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.rankingmode.msg.event.JoinActionEvent;
import com.module.rankingmode.msg.event.RoundOverEvent;
import com.module.rankingmode.prepare.model.JsonRoundInfo;
import com.module.rankingmode.prepare.presenter.MatchingPresenter;
import com.module.rankingmode.room.RoomServerApi;

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
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }


    public void sendRoundOver(int gameID){
        MyLog.d(TAG, "sendRoundOver" + " gameID=" + gameID);
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", gameID);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(roomServerApi.sendRoundOver(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if(result.getErrno() == 0){
                    long roundOverTimeMs = result.getData().getLong("roundOverTimeMs");
                    JsonRoundInfo currentRound = JSON.parseObject(result.getData().getString("currentRound"), JsonRoundInfo.class);
                    JsonRoundInfo nextRound = JSON.parseObject(result.getData().getString("nextRound"), JsonRoundInfo.class);
                }

            }
        }, this);
    }

    // 游戏轮次结束的通知消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoundOverEvent roundOverEvent) {
        MyLog.d(TAG, "onEventMainThread JoinActionEvent 1");
        if (roundOverEvent != null) {
           // 游戏轮次结束

        }
    }

}
