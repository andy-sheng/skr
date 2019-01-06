package com.module.playways.rank.prepare.presenter;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.module.playways.rank.msg.event.ExitGameEvent;
import com.module.playways.rank.msg.event.ReadyNoticeEvent;
import com.module.playways.rank.prepare.MatchServerApi;
import com.module.playways.rank.prepare.model.GameReadyModel;
import com.module.playways.rank.prepare.model.PrepareData;
import com.module.playways.rank.prepare.model.ReadyInfoModel;
import com.module.playways.rank.prepare.view.IMatchSucessView;
import com.module.playways.rank.room.RoomServerApi;
import com.zq.lyrics.model.UrlRes;
import com.zq.lyrics.utils.ZipUrlResourceManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.common.rxretrofit.ApiManager.APPLICATION_JSOIN;
import static com.module.playways.rank.msg.event.ExitGameEvent.EXIT_GAME_BEFORE_PLAY;

// 处理匹配成功之后   加入房间  检查房间
public class MatchSucessPresenter extends RxLifeCyclePresenter {

    IMatchSucessView iMatchSucessView;
    MatchServerApi matchServerApi;

    HandlerTaskTimer checkTask;

    int currentGameId; // 当前游戏id，即融云的房间号

    ZipUrlResourceManager zipUrlResourceManager;

    public MatchSucessPresenter(@NonNull IMatchSucessView view, int currentGameId, PrepareData prepareData) {
        MyLog.d(TAG, "MatchSucessPresenter" + " view=" + view + " currentGameId=" + currentGameId);
        this.iMatchSucessView = view;
        this.currentGameId = currentGameId;
        matchServerApi = ApiManager.getInstance().createService(MatchServerApi.class);
        addToLifeCycle();

        checkPlayerReadyState();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        ArrayList<UrlRes> urlResArrayList = new ArrayList<>();
        Observable.fromIterable(prepareData.getPlayerInfoList())
                .subscribe(playerInfo -> {
                            String url = playerInfo.getSongList().get(0).getLyric();
                            UrlRes lyric = new UrlRes(url, SongResUtils.getLyricDir(), U.getFileUtils().getSuffixFromUrl(url,SongResUtils.SUFF_ZRCE));
                            urlResArrayList.add(lyric);
                        }, throwable -> MyLog.e(TAG, throwable)
                        , () -> {
                            zipUrlResourceManager = new ZipUrlResourceManager(urlResArrayList, null);
                            zipUrlResourceManager.go();
                        });
    }

    private void checkPlayerReadyState() {
        MyLog.d(TAG, "checkPlayerReadyState");

        checkTask = HandlerTaskTimer.newBuilder().delay(10000).start(new HandlerTaskTimer.ObserverW() {
            @Override
            public void onNext(Integer integer) {
                if (!iMatchSucessView.isReady()) {
                    iMatchSucessView.needReMatch(false);
                    return;
                }

                ApiMethods.subscribe(matchServerApi.getCurrentReadyData(currentGameId), new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult result) {
                        MyLog.w(TAG, "checkPlayerReadyState result = " + result.getErrno() + " traceId = " + result.getTraceId());
                        if (result.getErrno() == 0) {
                            MyLog.w(TAG, "checkPlayerReadyState 成功，traceid是：" + result.getTraceId());
                            // todo 带回所有已准备人的信息
                            GameReadyModel jsonGameReadyInfo = JSON.parseObject(result.getData().toString(), GameReadyModel.class);
                            if (jsonGameReadyInfo.isIsGameStart()) {
                                iMatchSucessView.allPlayerIsReady(jsonGameReadyInfo);
                            } else {
                                iMatchSucessView.needReMatch(false);
                            }
                        } else {
                            MyLog.w(TAG, "checkPlayerReadyState 失败，traceid是：" + result.getTraceId());
                            iMatchSucessView.needReMatch(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        iMatchSucessView.needReMatch(false);
                        MyLog.w(TAG, "checkPlayerReadyState 错误");
                    }
                }, MatchSucessPresenter.this);
            }
        });
    }

    /**
     * 退出游戏
     */
    public void exitGame() {
        RoomServerApi roomServerApi = ApiManager.getInstance().createService(RoomServerApi.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", currentGameId);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(roomServerApi.exitGame(body), null);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (checkTask != null) {
            checkTask.dispose();
        }
        EventBus.getDefault().unregister(this);
    }

    /**
     * @param isPrepare true为准备，false为取消准备
     */
    public void prepare(boolean isPrepare) {
        MyLog.d(TAG, "prepare");
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", currentGameId);

        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(matchServerApi.readyGame(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.w(TAG, "prepare result = " + result.getErrno() + " traceId = " + result.getTraceId());
                if (result.getErrno() == 0) {
                    MyLog.w(TAG, "准备成功，traceid is " + result.getTraceId());
                    List<ReadyInfoModel> model = JSON.parseArray(result.getData().getString("readyInfo"), ReadyInfoModel.class);
                    iMatchSucessView.ready(isPrepare);
                    //  已准备人
                    iMatchSucessView.readyList(model);
                } else {
                    MyLog.w(TAG, "准备失败，traceid 是" + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.w(TAG, "准备错误");
            }
        }, MatchSucessPresenter.this);
    }

    // 加入指令，即服务器通知加入房间的指令
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ReadyNoticeEvent readyNoticeEvent) {
        MyLog.w(TAG, "onEventMainThread readyNoticeEvent " + readyNoticeEvent + " timeMs = " + readyNoticeEvent.info.getTimeMs());
        if (readyNoticeEvent.jsonGameReadyInfo.isIsGameStart()) {
            if (checkTask != null) {
                checkTask.dispose();
            }

            iMatchSucessView.allPlayerIsReady(readyNoticeEvent.jsonGameReadyInfo);
        }

        // 已准备人
        iMatchSucessView.readyList(readyNoticeEvent.jsonGameReadyInfo.getJsonReadyInfo());
    }

    // TODO: 2018/12/18 有人退出游戏了
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ExitGameEvent exitGameEvent) {
        if (exitGameEvent.type == EXIT_GAME_BEFORE_PLAY) {
            MyLog.w(TAG, "onEventMainThread EXIT_GAME_BEFORE_PLAY " + " timeMs = " + exitGameEvent.info.getTimeMs());
//            iMatchSucessView.needReMatch(true);
        }
    }
}


