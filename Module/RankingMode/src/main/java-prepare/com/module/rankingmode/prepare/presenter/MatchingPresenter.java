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
import com.module.rankingmode.prepare.MatchServerApi;
import com.module.rankingmode.prepare.model.JsonGameInfo;
import com.module.rankingmode.prepare.view.IMatchingView;
import com.zq.live.proto.Room.PlayerInfo;

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
public class MatchingPresenter extends RxLifeCyclePresenter {
    public static final String TAG = "MatchingPresenter";

    IMatchingView view;
    MatchServerApi matchServerApi;

    Disposable startMatchTask;
    HandlerTaskTimer loopMatchTask;
    HandlerTaskTimer checkJoinStateTask;

    int currentGameId;

    JsonGameInfo mJsonGameInfo;

    volatile MatchState matchState = MatchState.IDLE;

    public MatchingPresenter(@NonNull IMatchingView view) {
        this.view = view;
        matchServerApi = ApiManager.getInstance().createService(MatchServerApi.class);
        addToLifeCycle();

        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this);
        }
    }

    //这里获取圆圈动画内的头像
    public void getLoadingUserListIcon(){

    }

    public void startLoopMatchTask(){
        MyLog.d(TAG, "startLoopMatchTask");

//        loopMatchTask = HandlerTaskTimer.newBuilder()
//                .interval(10000)
//                .start(new HandlerTaskTimer.ObserverW() {
//            @Override
//            public void onNext(Integer integer) {
//                MyLog.d(TAG, "startLoopMatchTask onNext");
                startMatch();
//            }
//        });
    }

    private void disposeLoopMatchTask(){
        if(loopMatchTask != null){
            loopMatchTask.dispose();
        }
    }

    public void disposeMatchTask(){
        if(startMatchTask != null && !startMatchTask.isDisposed()){
            startMatchTask.dispose();
        }
    }

    // 开始匹配
    private void startMatch() {
        MyLog.d(TAG, "startMatch");
        disposeMatchTask();
        matchState = MatchState.Matching;

        HashMap<String, Object> map = new HashMap<>();
        map.put("mode", 1);
        map.put("playbookItemID", 1);
        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSOIN), JSON.toJSONString(map));

        startMatchTask = ApiMethods.subscribeWith(matchServerApi.startMatch(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    U.getToastUtil().showShort("开始匹配");
                } else {
                    U.getToastUtil().showShort("开始匹配错误");
                }
            }
        }, this);
    }

    // 取消匹配,重新回到开始匹配，这里需要判断是因为可以准备了还是因为用户点击了取消
    public void cancelMatch(int mode) {
        MyLog.d(TAG, "cancelMatch");
        disposeLoopMatchTask();
        disposeMatchTask();

        matchState = MatchState.IDLE;
        HashMap<String, Object> map = new HashMap<>();
        map.put("mode", mode);

        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(matchServerApi.cancleMatch(body).retry(3), null);
    }

    // TODO: 2018/12/12 怎么确定一个push肯定是当前一轮的push？？？
    JoinActionEvent joinActionEvent;

    // 加入指令，即服务器通知加入房间的指令
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(JoinActionEvent joinActionEvent) {
        MyLog.d(TAG, "onEventMainThread JoinActionEvent 1");
        if (joinActionEvent != null) {
            // todo 收到服务器加入游戏的通知
            // 是否要对加入通知进行过滤
            if(matchState == MatchState.Matching){
                MyLog.d(TAG, "onEventMainThread JoinActionEvent 1 currentGameId is " + joinActionEvent.gameId);
                this.joinActionEvent = joinActionEvent;
                disposeLoopMatchTask();
                disposeMatchTask();
                matchState = MatchState.MatchSucess;
                this.currentGameId = joinActionEvent.gameId;
                joinRoom(joinActionEvent.gameId);
            }
        }
    }

    // 加入游戏通知（别人进房间也会给我通知）
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(JoinNoticeEvent joinNoticeEvent) {
        MyLog.d(TAG, "onEventMainThread JoinNoticeEvent 1");
        if (joinNoticeEvent != null) {
            // 需要去更新GameInfo
            MyLog.d(TAG, "onEventMainThread JoinNoticeEvent 2");
            updateUserListState();
        }
    }

    public List<PlayerInfo> getPlayerInfoList(){
        if(joinActionEvent != null && joinActionEvent.playerInfoList != null){
            return joinActionEvent.playerInfoList;
        }

        return null;
    }

    @Override
    public void destroy() {
        super.destroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 加入融云房间，失败的话继续match，这里的失败得统计一下
     * @param gameId
     */
    private void joinRoom(int gameId) {
        this.currentGameId = gameId;
        MyLog.d(TAG, "joinRoom gameId " + gameId);
        ModuleServiceManager.getInstance().getMsgService().joinChatRoom(String.valueOf(gameId), new ICallback() {
            @Override
            public void onSucess(Object obj) {
                if(matchState == MatchState.MatchSucess){
                    matchState = MatchState.JoinRongYunRoomSuccess;
                    joinGame();
                }
            }

            @Override
            public void onFailed(Object obj, int errcode, String message) {
                U.getToastUtil().showShort("加入房间失败");
                startLoopMatchTask();
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
                    U.getToastUtil().showShort("加入房间失败 resule.errMsg is " + result.getErrmsg());
                    startLoopMatchTask();
                }
            }

            @Override
            public void onError(Throwable e) {
                U.getToastUtil().showShort("加入房间失败");
                startLoopMatchTask();
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
                        ApiMethods.subscribeWith(matchServerApi.getCurrentGameDate(currentGameId), new ApiObserver<ApiResult>() {
                            @Override
                            public void process(ApiResult result) {
                                MyLog.d(TAG, "3 秒钟过去，需要拉去此刻的房间信息");
                                if (result.getErrno() == 0) {
                                    JsonGameInfo jsonGameInfo = JSON.parseObject(result.getData().toString(), JsonGameInfo.class);
                                    if (jsonGameInfo.getHasJoinedUserCnt() == 3) {
                                        if(matchState == MatchState.JoinRongYunRoomSuccess){
                                            matchState = MatchState.JoinGameSuccess;
                                            mJsonGameInfo = jsonGameInfo;
                                            view.matchSucess(currentGameId, System.currentTimeMillis());
                                        }else {
                                            MyLog.d(TAG, "3 秒后拉去信息回来发现当前状态不是 JoinRongYunRoomSuccess");
                                            //跟下面的更新唯一的区别就是三秒钟之后人还不全就从新match
                                            startLoopMatchTask();
                                        }
                                    }else {
                                        MyLog.d(TAG, "3秒后拉完房间信息人数不够3个，需要重新match了");
//                                        startLoopMatchTask();
                                    }
                                } else {
                                    MyLog.d(TAG, "3秒钟后拉去的信息返回的resule error code不是 0,是" + result.getErrno());
                                    startLoopMatchTask();
                                }

                            }

                            @Override
                            public void onError(Throwable e) {
                                MyLog.d(MatchingPresenter.TAG, "checkCurrentGameData2 process" + " e=" + e);
                            }
                        }, MatchingPresenter.this);
                    }
                });
    }

    /**
     * 由于涉及到返回时序问题，都从服务器
     */
    private void updateUserListState(){
        MyLog.d(TAG, "updateUserListState" );
        ApiMethods.subscribeWith(matchServerApi.getCurrentGameDate(currentGameId), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    MyLog.d(TAG, "process updateUserListState 1" + " result=" + result);
                    JsonGameInfo jsonGameInfo = JSON.parseObject(result.getData().toString(), JsonGameInfo.class);
                    if (jsonGameInfo.getHasJoinedUserCnt() == 3) {
                        if(matchState == MatchState.JoinRongYunRoomSuccess){
                            matchState = MatchState.JoinGameSuccess;
                            mJsonGameInfo = jsonGameInfo;
                            view.matchSucess(currentGameId, System.currentTimeMillis());
                        }
                    }else {
                        MyLog.d(TAG, "process updateUserListState else");
                    }
                }else {
                    MyLog.d(TAG, "process updateUserListState 2" + " result=" + result);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.d(TAG, "onError updateUserListState 3" + " e=" + e);
            }
        }, MatchingPresenter.this);
    }

    enum MatchState{
        IDLE,
        Matching,
        MatchSucess,
        JoinRongYunRoomSuccess,
        JoinGameSuccess
    }
}
