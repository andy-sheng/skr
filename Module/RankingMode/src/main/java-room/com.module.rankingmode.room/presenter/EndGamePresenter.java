package com.module.rankingmode.room.presenter;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.rankingmode.room.RoomServerApi;
import com.module.rankingmode.room.view.IVoteView;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class EndGamePresenter extends RxLifeCyclePresenter {

    public final static String TAG = "EndGamePresenter";

    RoomServerApi mRoomServerApi = ApiManager.getInstance().createService(RoomServerApi.class);

    IVoteView view;

    public EndGamePresenter(IVoteView view) {
        this.view = view;
    }

    /**
     * 投票
     *
     * @param gameID      游戏标识别
     * @param votedUserID 被投人id
     * @param sysScoreVal 系统对自己的打分
     */
    public void vote(int gameID, long votedUserID, int sysScoreVal) {
        MyLog.d(TAG, "vote" + " gameID=" + gameID + " votedUserID=" + votedUserID + " sysScoreVal=" + sysScoreVal);
        long timeMs = System.currentTimeMillis();
        String sign = U.getMD5Utils().MD5_32("skrer" + String.valueOf(gameID) +
                String.valueOf(votedUserID) + String.valueOf(sysScoreVal) + String.valueOf(timeMs));

        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", gameID);
        map.put("votedUserID", votedUserID);
        map.put("sysScoreVal", sysScoreVal);
        map.put("timeMs", timeMs);
        map.put("sign", sign);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.vote(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    U.getToastUtil().showShort("投票成功");
                    view.voteSucess(votedUserID);
                } else {
                    MyLog.e(TAG, "vote result errno is " + result.getErrmsg());
                    view.voteFailed();
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, this);
    }

    /**
     * 获取投票结果
     *
     * @param gameID
     */
    public void getVoteResult(int gameID) {
        ApiMethods.subscribe(mRoomServerApi.getVoteResult(gameID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    U.getToastUtil().showShort("获取投票结果成功");
                } else {
                    MyLog.e(TAG, "getVoteResult result errno is " + result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, this);
    }

}
