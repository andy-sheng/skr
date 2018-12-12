package com.module.rankingmode.prepare.presenter;

import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.rankingmode.msg.event.ReadyNoticeEvent;
import com.module.rankingmode.prepare.MatchServerApi;
import com.module.rankingmode.prepare.model.GameInfo;
import com.module.rankingmode.prepare.model.GameReadyInfo;
import com.module.rankingmode.prepare.view.IMatchSucessView;

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

    IMatchSucessView view;
    MatchServerApi matchServerApi;

    int currentGameId; // 当前游戏id，即融云的房间号
    GameInfo currGameInfo; // 当前游戏的信息

    public MatchSucessPresenter(@NonNull IMatchSucessView view) {
        this.view = view;
        matchServerApi = ApiManager.getInstance().createService(MatchServerApi.class);
        addToLifeCycle();
    }

    // 处理检查房间的逻辑
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHECK_CURREN_GAME_INFO:
                    getCurrentGameData();
                default:
                    break;
            }

        }
    };

    // 加入房间
    public void joinRoom(int gameId) {
        this.currentGameId = gameId;
        ModuleServiceManager.getInstance().getMsgService().joinChatRoom(String.valueOf(gameId), new ICallback() {
            @Override
            public void onSucess(Object obj) {
                joinGame();
            }

            @Override
            public void onFailed(Object obj, int errcode, String message) {
                U.getToastUtil().showShort("加入房间失败");
            }
        });
    }

    // 通知服务器，加入游戏
    public void joinGame() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", currentGameId);

        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(matchServerApi.joinGame(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    GameInfo gameInfo = (GameInfo) JSON.parse(result.getData().toString());
                    if (gameInfo != null) {
                        if (gameInfo.getHasJoinedUserCnt() < 3) {
                            // todo 人数不足即开始3秒倒计时检测，向服务器查询游戏
                            handler.sendEmptyMessageDelayed(CHECK_CURREN_GAME_INFO, CHECK_DEFAULT_TIME);
                        }
                        // todo 加入游戏成功 即可以进入准备
                        currGameInfo = gameInfo;
                    }
                }
            }
        }, this);
    }


    // 获取加入游戏的数据
    public void getCurrentGameData() {
        ApiMethods.subscribe(matchServerApi.getCurrentGameDate(currentGameId), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    // todo 带回所有加房间人信息
                    GameInfo gameInfo = (GameInfo) JSON.parse(result.getData().toString());
                    if (gameInfo.getHasJoinedUserCnt() < 3) {
                        // todo 人数未到 退出房间

                    } else {
                        // todo 人数已满 准备开始

                    }
                }
            }
        }, this);
    }


    // 准备游戏
    public void readyGame() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", currentGameId);

        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(matchServerApi.readyGame(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    // todo 带回所有已准备人的信息
                    GameReadyInfo gameReadyInfo = JSON.parseObject(result.getData().toString(), GameReadyInfo.class);

                }
            }
        }, this);
    }

    // 获取准备游戏的数据
    public void checkCurrentReadyData() {
        ApiMethods.subscribe(matchServerApi.getCurrentGameDate(currentGameId), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    // todo 带回所有已准备人的信息
                    GameReadyInfo gameReadyInfo = JSON.parseObject(result.getData().toString(), GameReadyInfo.class);
                }
            }
        }, this);
    }

    // 加入游戏通知（别人进房间也会给我通知）
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ReadyNoticeEvent readyNoticeEvent) {
        MyLog.d(TAG, "onEventMainThread" + " readyNoticeEvent");
        if (readyNoticeEvent != null) {
            // 拿到最新所有ready信息

        }
    }
}


