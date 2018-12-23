package com.module.rankingmode.prepare.presenter;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.rankingmode.msg.event.JoinActionEvent;
import com.module.rankingmode.msg.event.JoinNoticeEvent;
import com.module.rankingmode.prepare.GameModeType;
import com.module.rankingmode.prepare.MatchServerApi;
import com.module.rankingmode.prepare.model.JsonGameInfo;
import com.module.rankingmode.prepare.view.IMatchingView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.common.rxretrofit.ApiManager.APPLICATION_JSOIN;

// 只处理匹配 请求匹配 取消匹配 和 收到加入游戏通知
public class MatchPresenter extends RxLifeCyclePresenter {
    public static final String TAG = "MatchingPresenter";

    IMatchingView view;
    MatchServerApi matchServerApi;

    Disposable startMatchTask;
    HandlerTaskTimer loopMatchTask;
    HandlerTaskTimer checkJoinStateTask;

    int currentGameId; // 游戏标识
    long gameCreateTime;

    int currentMusicId; //选择的歌曲id

    JsonGameInfo mJsonGameInfo;

    volatile MatchState matchState = MatchState.IDLE;

    public MatchPresenter(@NonNull IMatchingView view) {
        this.view = view;
        matchServerApi = ApiManager.getInstance().createService(MatchServerApi.class);
        addToLifeCycle();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    //这里获取圆圈动画内的头像
    public void getLoadingUserListIcon() {

    }

    public void startLoopMatchTask(int playbookItemID) {
        MyLog.d(TAG, "startLoopMatchTask");
        this.currentMusicId = playbookItemID;
        //现在只需要发一次，不轮询发
//        loopMatchTask = HandlerTaskTimer.newBuilder()
//                .interval(10000)
//                .start(new HandlerTaskTimer.ObserverW() {
//                    @Override
//                    public void onNext(Integer integer) {
//                        MyLog.d(TAG, "startLoopMatchTask onNext");
        startMatch(currentMusicId);
//                    }
//                });
    }

    private void disposeLoopMatchTask() {
        if (loopMatchTask != null) {
            loopMatchTask.dispose();
        }
    }

    public void disposeMatchTask() {
        if (startMatchTask != null && !startMatchTask.isDisposed()) {
            startMatchTask.dispose();
        }
    }

    /**
     * 开始匹配
     *
     * @param playbookItemID 选择歌曲itemID
     */
    private void startMatch(int playbookItemID) {
        MyLog.d(TAG, "startMatch");
        disposeMatchTask();
        matchState = MatchState.Matching;

        HashMap<String, Object> map = new HashMap<>();
        map.put("mode", GameModeType.GAME_MODE_CLASSIC_RANK);
        map.put("playbookItemID", playbookItemID);
        map.put("platform", 2);   // 代表是android平台

        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSOIN), JSON.toJSONString(map));

        startMatchTask = ApiMethods.subscribeWith(matchServerApi.startMatch(body).retry(10), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    U.getToastUtil().showShort("开始匹配");
                } else {
                    onError(new Throwable("开始匹配失败"));
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
                startMatch(currentMusicId);
            }
        }, this);
    }

    // 取消匹配,重新回到开始匹配，这里需要判断是因为可以准备了还是因为用户点击了取消
    public void cancelMatch() {
        MyLog.d(TAG, "cancelMatch");
        disposeLoopMatchTask();
        disposeMatchTask();

        matchState = MatchState.IDLE;
        HashMap<String, Object> map = new HashMap<>();
        map.put("mode", GameModeType.GAME_MODE_CLASSIC_RANK);

        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(matchServerApi.cancleMatch(body).retry(3), null);
    }

    // TODO: 2018/12/12 怎么确定一个push肯定是当前一轮的push？？？
    JoinActionEvent joinActionEvent;

    // 加入指令，即服务器通知加入房间的指令
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(JoinActionEvent joinActionEvent) {
        if (joinActionEvent != null) {
            MyLog.w(TAG, "onEventMainThread JoinActionEvent currentGameId is " + joinActionEvent.gameId);
            // 是否要对加入通知进行过滤
            if (matchState == MatchState.Matching) {
                matchState = MatchState.MatchSucess;
                this.joinActionEvent = joinActionEvent;
                disposeLoopMatchTask();
                disposeMatchTask();
                this.currentGameId = joinActionEvent.gameId;
                this.gameCreateTime = joinActionEvent.gameCreateMs;
                joinRoom();
            }
        }
    }

    // 加入游戏通知（别人进房间也会给我通知）
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(JoinNoticeEvent joinNoticeEvent) {
        if (joinNoticeEvent != null && joinNoticeEvent.jsonGameInfo != null) {
            MyLog.w(TAG, " onEventMainThread JoinNoticeEvent ");
            // 需要去更新GameInfo
            if (joinNoticeEvent.jsonGameInfo.getHasJoinedUserCnt() == 3) {
                if (matchState == MatchState.JoinRongYunRoomSuccess) {
                    matchState = MatchState.JoinGameSuccess;

                    if (checkJoinStateTask != null) {
                        checkJoinStateTask.dispose();
                    }

                    view.matchSucess(currentGameId, joinActionEvent.gameCreateMs, joinActionEvent.playerInfoList);
                }
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        disposeLoopMatchTask();
        disposeMatchTask();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 加入融云房间，失败的话继续match，这里的失败得统计一下
     */
    private void joinRoom() {
        MyLog.d(TAG, "joinRoom gameId " + currentGameId);
        ModuleServiceManager.getInstance().getMsgService().joinChatRoom(String.valueOf(currentGameId), new ICallback() {
            @Override
            public void onSucess(Object obj) {
                if (matchState == MatchState.MatchSucess) {
                    matchState = MatchState.JoinRongYunRoomSuccess;
                    joinGame();
                }
            }

            @Override
            public void onFailed(Object obj, int errcode, String message) {
                U.getToastUtil().showShort("加入房间失败");
                startLoopMatchTask(currentMusicId);
            }
        });
    }

    /**
     * 加入我们自己的房间，失败的话继续match，这里的失败得统计一下
     */
    private void joinGame() {
        MyLog.d(TAG, "joinGame gameId ");
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", currentGameId);

        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(matchServerApi.joinGame(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    updateUserListState();
                } else {
                    MyLog.w(TAG, "加入房间失败 resule.errMsg is " + result.getErrmsg());
                    startLoopMatchTask(currentMusicId);
                }
            }

            @Override
            public void onError(Throwable e) {
                U.getToastUtil().showShort("加入房间失败");
                startLoopMatchTask(currentMusicId);
            }
        }, this);

        checkCurrentGameData();
    }

    /**
     * 加入完我们服务器三秒钟后检查房间的情况，
     * 如果三个人都已经加入房间了或者还没到三秒就已经有push告诉客户端已经三个人都加入房间了
     * 就可以跳转到准备界面
     */
    public void checkCurrentGameData() {
        MyLog.d(TAG, "checkCurrentGameData");

        checkJoinStateTask = HandlerTaskTimer.newBuilder()
                .delay(3000)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        MyLog.d(TAG, "checkCurrentGameData onNext");
                        matchServerApi = ApiManager.getInstance().createService(MatchServerApi.class);
                        ApiMethods.subscribeWith(matchServerApi.getCurrentGameData(currentGameId), new ApiObserver<ApiResult>() {
                            @Override
                            public void process(ApiResult result) {
                                MyLog.d(TAG, "3 秒钟过去，需要拉去此刻的房间信息");
                                if (result.getErrno() == 0) {
                                    JsonGameInfo jsonGameInfo = JSON.parseObject(result.getData().toString(), JsonGameInfo.class);
                                    if (jsonGameInfo.getHasJoinedUserCnt() == 3) {
                                        if (matchState == MatchState.JoinRongYunRoomSuccess) {
                                            matchState = MatchState.JoinGameSuccess;
                                            mJsonGameInfo = jsonGameInfo;
                                            view.matchSucess(currentGameId, joinActionEvent.gameCreateMs, joinActionEvent.playerInfoList);
                                        } else {
                                            MyLog.d(TAG, "3 秒后拉去信息回来发现当前状态不是 JoinRongYunRoomSuccess");
                                            //跟下面的更新唯一的区别就是三秒钟之后人还不全就从新match
                                            startLoopMatchTask(currentMusicId);
                                        }
                                    } else {
                                        MyLog.d(TAG, "3秒后拉完房间信息人数不够3个，需要重新match了");
                                        // TODO: 2018/12/12 方便测试这个先注掉
                                        startLoopMatchTask(currentMusicId);
                                    }
                                } else {
                                    MyLog.d(TAG, "3秒钟后拉去的信息返回的resule error code不是 0,是" + result.getErrno());
                                    startLoopMatchTask(currentMusicId);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                MyLog.d(MatchPresenter.TAG, "checkCurrentGameData2 process" + " e=" + e);
                            }
                        }, MatchPresenter.this);
                    }
                });
    }

    /**
     * 由于涉及到返回时序问题，都从服务器
     */
    private void updateUserListState() {
        MyLog.d(TAG, "updateUserListState");
        ApiMethods.subscribeWith(matchServerApi.getCurrentGameData(currentGameId), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    MyLog.d(TAG, "process updateUserListState 1" + " result=" + result);
                    JsonGameInfo jsonGameInfo = JSON.parseObject(result.getData().toString(), JsonGameInfo.class);
                    if (jsonGameInfo.getHasJoinedUserCnt() == 3) {
                        if (matchState == MatchState.JoinRongYunRoomSuccess) {
                            matchState = MatchState.JoinGameSuccess;
                            mJsonGameInfo = jsonGameInfo;
                            view.matchSucess(currentGameId, joinActionEvent.gameCreateMs, joinActionEvent.playerInfoList);
                        }
                    } else {
                        MyLog.d(TAG, "process updateUserListState else");
                    }
                } else {
                    MyLog.d(TAG, "process updateUserListState 2" + " result=" + result);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, MatchPresenter.this);
    }

    enum MatchState {
        IDLE,
        Matching,
        MatchSucess,
        JoinRongYunRoomSuccess,
        JoinGameSuccess
    }
}
