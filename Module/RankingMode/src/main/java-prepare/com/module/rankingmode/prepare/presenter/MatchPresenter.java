package com.module.rankingmode.prepare.presenter;

import com.alibaba.fastjson.JSON;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.rankingmode.msg.event.JoinActionEvent;
import com.module.rankingmode.msg.event.JoinNoticeEvent;
import com.module.rankingmode.msg.event.ReadyAndStartNoticeEvent;
import com.module.rankingmode.msg.event.ReadyNoticeEvent;
import com.module.rankingmode.prepare.MatchServerApi;
import com.module.rankingmode.prepare.model.GameInfo;
import com.module.rankingmode.prepare.model.JoinInfo;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

// 处理MatchingFragment中请求相关
public class MatchPresenter extends RxLifeCyclePresenter {

    // 开始匹配
    public void startMatch() {
        // todo 短链接向服务器发送开始匹配请求
        HashMap<String, Object> map = new HashMap<>();
        map.put("mode", 1);
        map.put("playbookItemID", 1);

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(map));
        MatchServerApi matchServerApi = ApiManager.getInstance().createService(MatchServerApi.class);
        ApiMethods.subscribe(matchServerApi.startMatch(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
//                    EventBus.getDefault().post(new MatchStatusChangeEvent(MatchStatusChangeEvent.MATCH_STATUS_MATCHING));
                } else {
                    U.getToastUtil().showShort("出错");
                }
            }
        });
    }

    // 取消匹配,重新回到开始匹配
    public void cancelMatch(int mode) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("mode", mode);

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(map));
        MatchServerApi matchServerApi = ApiManager.getInstance().createService(MatchServerApi.class);
        ApiMethods.subscribe(matchServerApi.cancleMatch(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
//                    EventBus.getDefault().post(new MatchStatusChangeEvent(MatchStatusChangeEvent.MATCH_STATUS_START));
                    U.getToastUtil().showShort("取消匹配成功");
                }
            }
        });
    }

    // 通知服务器，加入游戏
    public void joinRoom(int gameId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", 3315200669l);

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(map));
        MatchServerApi matchServerApi = ApiManager.getInstance().createService(MatchServerApi.class);
        ApiMethods.subscribe(matchServerApi.joinRoom(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    // todo 带回所有加房间人信息
                    // todo 人数不足即开始3秒倒计时检测，向服务器查询房间
                }
            }
        });
    }

    // 获取加入游戏的数据
    public void getCurrentGameData(int gameId) {
        // todo 作为测试
        MatchServerApi matchServerApi = ApiManager.getInstance().createService(MatchServerApi.class);
        ApiMethods.subscribe(matchServerApi.getCurrentGameDate(20000217), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    // todo 带回所有加房间人信息
                    GameInfo gameInfo = (GameInfo) JSON.parse(result.getData().toString());
                    if (gameInfo.getHasJoinedUserCnt() < 3){
                       // todo 人数未到 退出房间
                    }else {
                       // todo 人数已满 准备开始
                    }
                }
            }
        });
    }



    // 加入指令，即服务器通知加入房间的指令
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(JoinActionEvent joinActionEvent) {
        if (joinActionEvent != null) {
            // todo 先加入融云房间，再加入游戏
        }

    }

    // 加入游戏通知（别人进房间也会给我通知）
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(JoinNoticeEvent joinNoticeEvent) {
        if (joinNoticeEvent != null) {

        }
    }

    // 准备游戏的通知消息
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ReadyNoticeEvent readyNoticeEvent) {
        if (readyNoticeEvent != null) {

        }
    }

    // 准备并开始游戏的通知
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ReadyAndStartNoticeEvent readyAndStartNoticeEvent) {
        if (readyAndStartNoticeEvent != null) {

        }
    }


}
