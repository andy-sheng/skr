package com.module.playways.songmanager.presenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.playways.doubleplay.DoubleRoomData;
import com.module.playways.doubleplay.DoubleRoomServerApi;
import com.module.playways.room.song.model.SongModel;
import com.module.playways.songmanager.event.AddSongEvent;
import com.module.playways.songmanager.model.RecommendTagModel;
import com.module.playways.songmanager.view.ISongManageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class DoubleSongManagePresenter extends RxLifeCyclePresenter {

    DoubleRoomServerApi mDoubleRoomServerApi;
    ISongManageView mSongManageView;
    DoubleRoomData mDoubleRoomData;

    public DoubleSongManagePresenter(ISongManageView songManageView, DoubleRoomData roomData) {
        this.mSongManageView = songManageView;
        this.mDoubleRoomData = roomData;
        mDoubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi.class);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void getRecommendTag() {
        ApiMethods.subscribe(mDoubleRoomServerApi.getDoubleStandBillBoards(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<RecommendTagModel> recommendTagModelArrayList = JSONObject.parseArray(result.getData().getString("items"), RecommendTagModel.class);
                    mSongManageView.showRecommendSong(recommendTagModelArrayList);
                } else {
                    U.getToastUtil().showShort(result.getErrmsg() + "");
                }

            }
        }, this, new ApiMethods.RequestControl("getStandBillBoards", ApiMethods.ControlType.CancelThis));
    }

    // 添加新歌
    public void addSong(SongModel songModel) {
        MyLog.d(TAG, "addSong");
        HashMap<String, Object> map = new HashMap<>();
        map.put("itemID", songModel.getItemID());
        map.put("roomID", mDoubleRoomData.getGameId());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mDoubleRoomServerApi.addSong(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "addSong process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    U.getToastUtil().showShort(songModel.getItemName() + " 添加成功");
                } else {
                    MyLog.w(TAG, "addSong failed, " + " traceid is " + result.getTraceId());
                    U.getToastUtil().showShort(result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, this);
    }

    /**
     * 自己添加的歌曲
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AddSongEvent event) {
        // 双人房都可以点歌
        addSong(event.getSongModel());
    }

    @Override
    public void destroy() {
        super.destroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
