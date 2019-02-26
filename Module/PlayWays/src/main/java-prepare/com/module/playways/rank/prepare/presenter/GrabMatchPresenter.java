package com.module.playways.rank.prepare.presenter;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.rx.RxRetryAssist;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.HandlerTaskTimer;
import com.component.busilib.constans.GameModeType;
import com.module.ModuleServiceManager;
import com.module.common.ICallback;
import com.module.playways.rank.msg.event.JoinActionEvent;
import com.module.playways.rank.msg.event.JoinNoticeEvent;
import com.module.playways.rank.prepare.MatchServerApi;
import com.module.playways.rank.prepare.model.GameInfoModel;
import com.module.playways.rank.prepare.model.MatchingUserIconListInfo;
import com.module.playways.rank.prepare.view.IMatchingView;

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
public class GrabMatchPresenter extends BaseMatchPresenter {
    public final static String TAG = "GrabMatchPresenter";

    IMatchingView mView;
    MatchServerApi mMatchServerApi;
    Disposable mStartMatchTask;
    HandlerTaskTimer mLoopMatchTask;
    HandlerTaskTimer mCheckJoinStateTask;
    int mCurrentMusicId; //选择的歌曲id
    int mGameType; // 当前游戏类型
    // TODO: 2018/12/12 怎么确定一个push肯定是当前一轮的push？？？
    JoinActionEvent mJoinActionEvent;

//    int mCurrentGameId; // 游戏标识
//    long mGameCreateTime;
//    private List<SongModel> mSongModelList;

    GameInfoModel mJsonGameInfo;

    volatile MatchState mMatchState = MatchState.IDLE;

    public GrabMatchPresenter(@NonNull IMatchingView view) {
        this.mView = view;
        mMatchServerApi = ApiManager.getInstance().createService(MatchServerApi.class);
        addToLifeCycle();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void startLoopMatchTask(int playbookItemID, int gameType) {
        MyLog.d(TAG, "startLoopMatchTask");
        this.mCurrentMusicId = playbookItemID;
        this.mGameType = gameType;

        disposeLoopMatchTask();
        mLoopMatchTask = HandlerTaskTimer.newBuilder()
                .interval(10000)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        MyLog.d(TAG, "startLoopMatchTask onNext");
                        startMatch(mCurrentMusicId, mGameType);
                    }
                });
    }

    private void disposeLoopMatchTask() {
        if (mLoopMatchTask != null) {
            mLoopMatchTask.dispose();
        }
    }

    public void disposeMatchTask() {
        if (mStartMatchTask != null && !mStartMatchTask.isDisposed()) {
            mStartMatchTask.dispose();
        }
    }

    /**
     * 开始匹配
     *
     * @param playbookItemID 选择歌曲itemID
     */
    private void startMatch(int playbookItemID, int gameType) {
        MyLog.d(TAG, "startMatch" + " playbookItemID=" + playbookItemID + " gameType=" + gameType);
        disposeMatchTask();
        mMatchState = MatchState.Matching;

        HashMap<String, Object> map = new HashMap<>();
        map.put("mode", gameType);
        if (mGameType == GameModeType.GAME_MODE_GRAB) {
            map.put("playbookTagID", playbookItemID);
        } else {
            map.put("playbookItemID", playbookItemID);
        }
        map.put("platform", 20);   // 代表是android平台
        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSOIN), JSON.toJSONString(map));
        mStartMatchTask = ApiMethods.subscribeWith(mMatchServerApi.startMatch(body).retryWhen(new RxRetryAssist(1, 5, false)), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.w(TAG, "process" + " result =" + result.getErrno() + " traceId =" + result.getTraceId());
                if (result.getErrno() == 0) {
//                    U.getToastUtil().showShort("开始匹配");
                } else {
                    onError(new Throwable("开始匹配失败"));
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
                // 不能这么弄，会导致死循环
//                startMatch(mCurrentMusicId, mGameType);
            }
        }, this);
    }

    // 取消匹配,重新回到开始匹配，这里需要判断是因为可以准备了还是因为用户点击了取消
    public void cancelMatch() {
        MyLog.d(TAG, "cancelMatch");
        disposeLoopMatchTask();
        disposeMatchTask();

        mMatchState = MatchState.IDLE;
        HashMap<String, Object> map = new HashMap<>();
        map.put("mode", mGameType);

        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mMatchServerApi.cancleMatch(body).retry(3), null);
    }

    // 加入指令，即服务器通知加入房间的指令
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(JoinActionEvent joinActionEvent) {
        if (joinActionEvent != null) {
            MyLog.w(TAG, "onEventMainThread JoinActionEvent currentGameId is " + joinActionEvent.gameId
                    + " timeMs = " + joinActionEvent.info.getTimeMs()
                    + " songSize = " + joinActionEvent.songModelList.size()
            );
            // 是否要对加入通知进行过滤
            if (mMatchState == MatchState.Matching) {
                mMatchState = MatchState.MatchSucess;
                this.mJoinActionEvent = joinActionEvent;
                disposeLoopMatchTask();
                disposeMatchTask();
                joinRoom();
            }
        }
    }

    // 加入游戏通知（别人进房间也会给我通知）
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(JoinNoticeEvent joinNoticeEvent) {
        if (joinNoticeEvent != null && joinNoticeEvent.jsonGameInfo != null) {
            MyLog.w(TAG, " onEventMainThread JoinNoticeEvent timeMs = " + joinNoticeEvent.info.getTimeMs() + ", joinNoticeEvent.jsonGameInfo.getReadyClockResMs() " + joinNoticeEvent.jsonGameInfo.getReadyClockResMs());
            // 需要去更新GameInfo
            if (joinNoticeEvent.jsonGameInfo.getReadyClockResMs() != 0) {
                if (mMatchState == MatchState.JoinRongYunRoomSuccess) {
                    mMatchState = MatchState.JoinGameSuccess;

                    if (mCheckJoinStateTask != null) {
                        mCheckJoinStateTask.dispose();
                    }

                    mView.matchSucess(mJoinActionEvent);
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
        if (mMatchState == MatchState.JoinRongYunRoomSuccess) {
            // 只是加入融云成功但是并没有返回进入准备页面
            ModuleServiceManager.getInstance().getMsgService().leaveChatRoom(String.valueOf(mJoinActionEvent.gameId));
        }
    }

    /**
     * 加入融云房间，失败的话继续match，这里的失败得统计一下
     */
    private void joinRoom() {
        MyLog.d(TAG, "joinRoom gameId " + mJoinActionEvent.gameId);
        ModuleServiceManager.getInstance().getMsgService().joinChatRoom(String.valueOf(mJoinActionEvent.gameId), new ICallback() {
            @Override
            public void onSucess(Object obj) {
                if (mMatchState == MatchState.MatchSucess) {
                    mMatchState = MatchState.JoinRongYunRoomSuccess;
                    joinGame();
                }
            }

            @Override
            public void onFailed(Object obj, int errcode, String message) {
//                U.getToastUtil().showShort("加入房间失败");
                startLoopMatchTask(mCurrentMusicId, mGameType);
            }
        });
    }

    /**
     * 加入我们自己的房间，失败的话继续match，这里的失败得统计一下
     */
    private void joinGame() {
        MyLog.d(TAG, "joinGame gameId ");
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mJoinActionEvent.gameId);

        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mMatchServerApi.joinGame(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.w(TAG, "加入房间 result =  " + result.getErrno() + " traceId = " + result.getTraceId());
                if (result.getErrno() == 0) {
                    sendIntoRoomReq();
                } else {
                    startLoopMatchTask(mCurrentMusicId, mGameType);
                }
            }

            @Override
            public void onError(Throwable e) {
//                U.getToastUtil().showShort("加入房间失败");
                startLoopMatchTask(mCurrentMusicId, mGameType);
            }
        }, this);
    }

    /**
     * 请求进入房间
     */
    private void sendIntoRoomReq() {

    }

    /**
     * 获取头像
     */
    public void getMatchingUserIconList() {
        MyLog.d(TAG, "getMatchingUserIconList gameId ");

        ApiMethods.subscribe(mMatchServerApi.getMatchingAvatar(1), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    MyLog.d(TAG, "getMatchingUserIconList result =  " + result.getErrno() + " traceId = " + result.getTraceId());
                    MatchingUserIconListInfo matchingUserIconListInfo = JSON.parseObject(result.getData().toString(), MatchingUserIconListInfo.class);
                    mView.showUserIconList(matchingUserIconListInfo.getPlayers());
                } else {
                    MyLog.w(TAG, "getMatchingUserIconList result =  " + result.getErrno() + " traceId = " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, "getMatchingUserIconList 失败");
            }
        }, this);
    }

    /**
     * 退出游戏
     */
    public void exitGame(int gameId) {
        if (gameId <= 0) {
            MyLog.w(TAG, "exitGame gameId <= 0");
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", gameId);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mMatchServerApi.exitGame(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.w(TAG, "退出房间 resule no is " + result.getErrno() + ", traceid is " + result.getTraceId());
            }

            @Override
            public void onError(Throwable e) {
                MyLog.w(GrabMatchPresenter.TAG, "exitGame error, " + " e=" + e);
            }
        }, GrabMatchPresenter.this);
    }

    enum MatchState {
        IDLE(1),
        Matching(2),
        MatchSucess(3),
        JoinRongYunRoomSuccess(4),
        JoinGameSuccess(5);

        MatchState(int v) {
            this.v = v;
        }

        int v;

        public int getV() {
            return v;
        }
    }
}
