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
     * @param pickUserID  被灭灯用户id
     */
    public void vote(int gameID, long pickUserID) {
        MyLog.d(TAG, "vote" + " gameID=" + gameID + " votedUserID=" + pickUserID);

        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", gameID);
        map.put("pickUserID", pickUserID);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSOIN), JSON.toJSONString(map));
        ApiMethods.subscribe(mRoomServerApi.vote(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    U.getToastUtil().showShort("投票成功");
                    view.voteSucess(pickUserID);
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
