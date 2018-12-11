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
import com.module.rankingmode.prepare.model.GameInfo;
import com.module.rankingmode.prepare.view.IMatchingView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

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

    public MatchingPresenter(@NonNull IMatchingView view) {
        this.view = view;
        ApiManager.getInstance().testInterceptor(new TestInterceptor());
        matchServerApi = ApiManager.getInstance().createService(MatchServerApi.class);
        addToLifeCycle();

    }

    //这里获取圆圈动画内的头像
    public void getLoadingUserListIcon(){

    }

    public void startLoopMatchTask(){
        MyLog.d(TAG, "startLoopMatchTask");

        loopMatchTask = HandlerTaskTimer.newBuilder()
                .interval(10000)
                .start(new HandlerTaskTimer.ObserverW() {
            @Override
            public void onNext(Integer integer) {
                MyLog.d(TAG, "startLoopMatchTask onNext");
                startMatch();
            }
        });
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
        // todo 短链接向服务器发送开始匹配请求
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

        HashMap<String, Object> map = new HashMap<>();
        map.put("mode", mode);

        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribeWith(matchServerApi.cancleMatch(body).retry(3), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    U.getToastUtil().showShort("取消匹配成功");
                }
            }
        }, this);
    }

    // 加入指令，即服务器通知加入房间的指令
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(JoinActionEvent joinActionEvent) {
        if (joinActionEvent != null) {
            // todo 收到服务器加入游戏的通知
            // 是否要对加入通知进行过滤
            view.matchSucess(joinActionEvent.gameId, joinActionEvent.gameCreateMs);
            joinRoom(joinActionEvent.gameId);
        }
    }

    // 加入游戏通知（别人进房间也会给我通知）
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(JoinNoticeEvent joinNoticeEvent) {
        if (joinNoticeEvent != null) {
            // 需要去更新GameInfo
//            if(three user is prepared){
//                if(checkJoinStateTask != null){
//                    checkJoinStateTask.dispose();
//                }
//                goto prepare sence
//            }
        }
    }

    /**
     * 加入融云房间，失败的话继续match，这里的失败得统计一下
     * @param gameId
     */
    private void joinRoom(int gameId) {
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

    /**
     * 加入我们自己的房间，失败的话继续match，这里的失败得统计一下
     */
    private void joinGame() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", currentGameId);

        RequestBody body = RequestBody.create(MediaType.parse(APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(matchServerApi.joinGame(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    GameInfo gameInfo = (GameInfo) JSON.parse(result.getData().toString());
                    if (gameInfo != null) {
                        checkCurrentGameData();
                    }
                }
            }
        }, this);
    }

    /**
     * 加入完我们服务器三秒钟后检查房间的情况，
     * 如果三个人都已经加入房间了或者还没到三秒就已经有push告诉客户端已经三个人都加入房间了
     * 就可以跳转到准备界面
     */
    public void checkCurrentGameData() {
        checkJoinStateTask = HandlerTaskTimer.newBuilder()
                .delay(3000)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        ApiMethods.subscribeWith(matchServerApi.getCurrentGameDate(currentGameId), new ApiObserver<ApiResult>() {
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
                        }, MatchingPresenter.this);
                    }
                });

    }
}
