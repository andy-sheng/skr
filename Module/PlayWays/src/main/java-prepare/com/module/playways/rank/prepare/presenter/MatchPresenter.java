package com.module.playways.rank.prepare.presenter;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rx.RxRetryAssist;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
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
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.common.rxretrofit.ApiManager.APPLICATION_JSOIN;

// 只处理匹配 请求匹配 取消匹配 和 收到加入游戏通知
public class MatchPresenter extends RxLifeCyclePresenter {
    public final static String TAG = "MatchPresenter";

    IMatchingView mView;
    MatchServerApi mMatchServerApi;

    Disposable mStartMatchTask;
    HandlerTaskTimer mLoopMatchTask;
    HandlerTaskTimer mCheckJoinStateTask;

    int mCurrentGameId; // 游戏标识
    long mGameCreateTime;

    int mCurrentMusicId; //选择的歌曲id
    int mGameType; // 当前游戏类型

    GameInfoModel mJsonGameInfo;

    volatile MatchState mMatchState = MatchState.IDLE;
    private List<String> avatarURL;

    public MatchPresenter(@NonNull IMatchingView view) {
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
        map.put("playbookItemID", playbookItemID);
        map.put("platform", 20);   // 代表是android平台

        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSOIN), JSON.toJSONString(map));

        mStartMatchTask = ApiMethods.subscribeWith(mMatchServerApi.startMatch(body).retryWhen(new RxRetryAssist(1,5,false)), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.w(TAG, "process" + " result =" + result.getErrno() + " traceId =" + result.getTraceId());
                if (result.getErrno() == 0) {
                    U.getToastUtil().showShort("开始匹配");
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

    // TODO: 2018/12/12 怎么确定一个push肯定是当前一轮的push？？？
    JoinActionEvent joinActionEvent;

    // 加入指令，即服务器通知加入房间的指令
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(JoinActionEvent joinActionEvent) {
        if (joinActionEvent != null) {
            MyLog.w(TAG, "onEventMainThread JoinActionEvent currentGameId is " + joinActionEvent.gameId + " timeMs = " + joinActionEvent.info.getTimeMs());
            // 是否要对加入通知进行过滤
            if (mMatchState == MatchState.Matching) {
                mMatchState = MatchState.MatchSucess;
                this.joinActionEvent = joinActionEvent;
                disposeLoopMatchTask();
                disposeMatchTask();
                this.mCurrentGameId = joinActionEvent.gameId;
                this.mGameCreateTime = joinActionEvent.gameCreateMs;
                joinRoom();
            }
        }
    }

    // 加入游戏通知（别人进房间也会给我通知）
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(JoinNoticeEvent joinNoticeEvent) {
        if (joinNoticeEvent != null && joinNoticeEvent.jsonGameInfo != null) {
            MyLog.w(TAG, " onEventMainThread JoinNoticeEvent timeMs = " + joinNoticeEvent.info.getTimeMs());
            // 需要去更新GameInfo
            if (joinNoticeEvent.jsonGameInfo.getHasJoinedUserCnt() == 3) {
                if (mMatchState == MatchState.JoinRongYunRoomSuccess) {
                    mMatchState = MatchState.JoinGameSuccess;

                    if (mCheckJoinStateTask != null) {
                        mCheckJoinStateTask.dispose();
                    }

                    mView.matchSucess(mCurrentGameId, joinActionEvent.gameCreateMs, joinActionEvent.playerInfoList, joinActionEvent.info.getSender().getAvatar());
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
        MyLog.d(TAG, "joinRoom gameId " + mCurrentGameId);
        ModuleServiceManager.getInstance().getMsgService().joinChatRoom(String.valueOf(mCurrentGameId), new ICallback() {
            @Override
            public void onSucess(Object obj) {
                if (mMatchState == MatchState.MatchSucess) {
                    mMatchState = MatchState.JoinRongYunRoomSuccess;
                    joinGame();
                }
            }

            @Override
            public void onFailed(Object obj, int errcode, String message) {
                U.getToastUtil().showShort("加入房间失败");
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
        map.put("gameID", mCurrentGameId);

        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mMatchServerApi.joinGame(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.w(TAG, "加入房间 result =  " + result.getErrno() + " traceId = " + result.getTraceId());
                if (result.getErrno() == 0) {
                    updateUserListState();
                } else {
                    startLoopMatchTask(mCurrentMusicId, mGameType);
                }
            }

            @Override
            public void onError(Throwable e) {
                U.getToastUtil().showShort("加入房间失败");
                startLoopMatchTask(mCurrentMusicId, mGameType);
            }
        }, this);

        checkCurrentGameData();
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
                    mView.showUserIconList(matchingUserIconListInfo.getAvatarURL());
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
     * 加入完我们服务器三秒钟后检查房间的情况，
     * 如果三个人都已经加入房间了或者还没到三秒就已经有push告诉客户端已经三个人都加入房间了
     * 就可以跳转到准备界面
     */
    public void checkCurrentGameData() {
        MyLog.d(TAG, "checkCurrentGameData");

        mCheckJoinStateTask = HandlerTaskTimer.newBuilder()
                .delay(3000)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        MyLog.d(TAG, "checkCurrentGameData onNext");
                        mMatchServerApi = ApiManager.getInstance().createService(MatchServerApi.class);
                        ApiMethods.subscribeWith(mMatchServerApi.getCurrentGameData(mCurrentGameId), new ApiObserver<ApiResult>() {
                            @Override
                            public void process(ApiResult result) {
                                MyLog.w(TAG, "checkCurrentGameData result = " + result.getErrno() + " traceId = " + result.getTraceId());
                                if (result.getErrno() == 0) {
                                    GameInfoModel jsonGameInfo = JSON.parseObject(result.getData().toString(), GameInfoModel.class);
                                    if (jsonGameInfo.getHasJoinedUserCnt() == 3) {
                                        if (mMatchState == MatchState.JoinRongYunRoomSuccess) {
                                            mMatchState = MatchState.JoinGameSuccess;
                                            mJsonGameInfo = jsonGameInfo;
                                            mView.matchSucess(mCurrentGameId, joinActionEvent.gameCreateMs, joinActionEvent.playerInfoList, joinActionEvent.info.getSender().getAvatar());
                                        } else {
                                            MyLog.d(TAG, "3 秒后拉去信息回来发现当前状态不是 JoinRongYunRoomSuccess");
                                            //跟下面的更新唯一的区别就是三秒钟之后人还不全就从新match
                                            startLoopMatchTask(mCurrentMusicId, mGameType);
                                        }
                                    } else {
                                        MyLog.d(TAG, "3秒后拉完房间信息人数不够3个，需要重新match了");
                                        // TODO: 2018/12/12 方便测试这个先注掉
                                        startLoopMatchTask(mCurrentMusicId, mGameType);
                                    }
                                } else {
                                    MyLog.d(TAG, "3秒钟后拉去的信息返回的resule error code不是 0,是" + result.getErrno());
                                    startLoopMatchTask(mCurrentMusicId, mGameType);
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
        ApiMethods.subscribeWith(mMatchServerApi.getCurrentGameData(mCurrentGameId), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.w(TAG, "updateUserListState result = " + result.getErrno() + " traceId = " + result.getTraceId());
                if (result.getErrno() == 0) {
                    GameInfoModel jsonGameInfo = JSON.parseObject(result.getData().toString(), GameInfoModel.class);
                    if (jsonGameInfo.getHasJoinedUserCnt() == 3) {
                        if (mMatchState == MatchState.JoinRongYunRoomSuccess) {
                            mMatchState = MatchState.JoinGameSuccess;
                            mJsonGameInfo = jsonGameInfo;
                            mView.matchSucess(mCurrentGameId, joinActionEvent.gameCreateMs, joinActionEvent.playerInfoList, joinActionEvent.info.getSender().getAvatar());
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

    public List<String> getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(List<String> avatarURL) {
        this.avatarURL = avatarURL;
    }

    enum MatchState {
        IDLE,
        Matching,
        MatchSucess,
        JoinRongYunRoomSuccess,
        JoinGameSuccess
    }
}
