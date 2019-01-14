package com.module.playways.rank.room.presenter;

import android.os.Handler;
import android.os.Message;
import android.support.v4.util.Pair;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.playways.rank.msg.event.VoteResultEvent;
import com.module.playways.rank.room.RoomServerApi;
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.rank.room.scoremodel.ScoreDetailModel;
import com.module.playways.rank.room.scoremodel.UserScoreModel;
import com.module.playways.rank.room.model.VoteInfoModel;
import com.module.playways.rank.room.view.IVoteView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class EndGamePresenter extends RxLifeCyclePresenter {

    public final static String TAG = "EndGamePresenter";

    final static int MSG_GET_VOTE = 1;

    RoomServerApi mRoomServerApi = ApiManager.getInstance().createService(RoomServerApi.class);

    IVoteView mView;

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_GET_VOTE) {
                Pair<Integer, Integer> pair = (Pair<Integer, Integer>) msg.obj;
                getVoteResult(pair.first, pair.second);
            }
        }
    };

    public EndGamePresenter(IVoteView view) {
        this.mView = view;
    }

    @Override
    public void start() {
        super.start();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        mUiHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 投票
     *
     * @param gameID     游戏标识别
     * @param pickUserID 被灭灯用户id
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
                    mView.voteSucess(pickUserID);
                } else {
                    MyLog.e(TAG, "vote result errno is " + result.getErrmsg());
                    mView.voteFailed();
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
    public void getVoteResult(int gameID, int deep) {
        if (deep > 3) {
            MyLog.d(TAG, "拉了 5次都没数据，过分了。getVoteResult" + " gameID=" + gameID + " deep=" + deep);
            return;
        }
        ApiMethods.subscribe(mRoomServerApi.getVoteResult(gameID), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<VoteInfoModel> voteInfoModelList = JSON.parseArray(result.getData().getString("voteInfo"), VoteInfoModel.class);
                    List<UserScoreModel> userScoreModelList = JSON.parseArray(result.getData().getString("userScoreRecord"), UserScoreModel.class);

                    if (voteInfoModelList != null && voteInfoModelList.size() > 0) {
                        ScoreDetailModel scoreDetailModel = new ScoreDetailModel();
                        scoreDetailModel.parse(userScoreModelList);
                        MyLog.d(TAG, " getVoteResult " + " scoreDetailModel " +  scoreDetailModel.toString());
                        MyLog.d(TAG, " getVoteResult " + " scoreDetailModel " +  scoreDetailModel.toString());
                        mView.showRecordView(new RecordData(voteInfoModelList, scoreDetailModel));
                    } else {
                        mUiHandler.removeMessages(MSG_GET_VOTE);
                        Message message = mUiHandler.obtainMessage(MSG_GET_VOTE);
                        message.obj = new Pair<Integer, Integer>(gameID, deep + 1);
                        mUiHandler.sendMessageDelayed(message, 1000*deep);
                    }
                } else {
                    MyLog.e(TAG, "getVoteResult result errno is " + result.getErrmsg());
                    mUiHandler.removeMessages(MSG_GET_VOTE);
                    Message message = mUiHandler.obtainMessage(MSG_GET_VOTE);
                    message.obj = new Pair<Integer, Integer>(gameID, deep + 1);
                    mUiHandler.sendMessageDelayed(message, 1000*deep);
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(VoteResultEvent event) {
        MyLog.d(TAG, "VoteResultEvent" + " event TimeMs = " + event.mBasePushInfo.getTimeMs());
        MyLog.d(TAG, "VoteResultEvent" + " event = " + event.mScoreDetailModel.toString());
        MyLog.d(TAG, "VoteResultEvent" + " event = " + event.mVoteInfoModels.toString());
        mView.showRecordView(new RecordData(event.mVoteInfoModels, event.mScoreDetailModel));
    }
}
