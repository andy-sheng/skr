package com.module.rankingmode.prepare.presenter;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.HandlerTaskTimer;
import com.module.rankingmode.msg.event.ReadyNoticeEvent;
import com.module.rankingmode.prepare.MatchServerApi;
import com.module.rankingmode.prepare.model.GameInfo;
import com.module.rankingmode.prepare.model.GameReadyInfo;
import com.module.rankingmode.prepare.view.IMatchSucessView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import io.reactivex.annotations.NonNull;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.common.rxretrofit.ApiManager.APPLICATION_JSOIN;

// 处理匹配成功之后   加入房间  检查房间
public class MatchSucessPresenter extends RxLifeCyclePresenter {

    private static final int CHECK_CURREN_GAME_INFO = 0;
    private static final int CHECK_DEFAULT_TIME = 3000;

    IMatchSucessView iMatchSucessView;
    MatchServerApi matchServerApi;

    HandlerTaskTimer checkTask;

    int currentGameId; // 当前游戏id，即融云的房间号
    GameInfo currGameInfo; // 当前游戏的信息

    public MatchSucessPresenter(@NonNull IMatchSucessView view, int currentGameId) {
        MyLog.d(TAG, "MatchSucessPresenter" + " view=" + view + " currentGameId=" + currentGameId);
        this.iMatchSucessView = view;
        this.currentGameId = currentGameId;
        matchServerApi = ApiManager.getInstance().createService(MatchServerApi.class);
        addToLifeCycle();

        checkPlayerReadyState();

        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    private void checkPlayerReadyState(){
        MyLog.d(TAG, "checkPlayerReadyState");
        checkTask = HandlerTaskTimer.newBuilder().delay(100000).start(new HandlerTaskTimer.ObserverW() {
            @Override
            public void onNext(Integer integer) {
                ApiMethods.subscribe(matchServerApi.getCurrentReadyData(currentGameId), new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult result) {
                        MyLog.d(TAG, "checkPlayerReadyState result：" + result);
                        if (result.getErrno() == 0) {
                            // todo 带回所有已准备人的信息
                            GameReadyInfo gameReadyInfo = JSON.parseObject(result.getData().toString(), GameReadyInfo.class);
                            if(gameReadyInfo.isIsGameStart()){
                                iMatchSucessView.allPlayerIsReady();
                            }else {
                                iMatchSucessView.needReMatch();
                            }
                        }else {
                            iMatchSucessView.needReMatch();
                        }
                    }
                }, MatchSucessPresenter.this);
            }
        });
    }

    @Override
    public void destroy() {
        super.destroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * @param isPrepare  true为准备，false为取消准备
     */
    public void prepare(boolean isPrepare){
        MyLog.d(TAG, "prepare");
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", currentGameId);

        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(matchServerApi.readyGame(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "prepare " + result);
                if (result.getErrno() == 0) {
                    iMatchSucessView.ready(isPrepare);
                }
            }
        }, MatchSucessPresenter.this);
    }

    // 加入指令，即服务器通知加入房间的指令
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ReadyNoticeEvent readyNoticeEvent) {
        MyLog.d(TAG, "onEventMainThread readyNoticeEvent " + readyNoticeEvent);
        if(readyNoticeEvent.isGameStart){
            if(checkTask != null){
                checkTask.dispose();
            }

            iMatchSucessView.allPlayerIsReady();
        }
    }
}


