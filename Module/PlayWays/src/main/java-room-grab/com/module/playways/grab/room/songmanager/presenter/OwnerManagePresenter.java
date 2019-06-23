package com.module.playways.grab.room.songmanager.presenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.songmanager.SongManageData;
import com.module.playways.grab.room.songmanager.event.AddSongEvent;
import com.module.playways.grab.room.songmanager.event.RoomNameChangeEvent;
import com.module.playways.grab.room.songmanager.model.RecommendTagModel;
import com.module.playways.grab.room.songmanager.view.IOwnerManageView;
import com.module.playways.room.song.model.SongModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class OwnerManagePresenter extends RxLifeCyclePresenter {
    IOwnerManageView mIOwnerManageView;
    GrabRoomServerApi mGrabRoomServerApi;
    SongManageData mSongManageData;

    public OwnerManagePresenter(IOwnerManageView IOwnerManageView, SongManageData songManageData) {
        mIOwnerManageView = IOwnerManageView;
        mSongManageData = songManageData;
        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void updateRoomName(int roomID, String roomName) {
        MyLog.d(TAG, "updateRoomName" + " roomID=" + roomID + " roomName=" + roomName);
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", roomID);
        map.put("roomName", roomName);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mGrabRoomServerApi.updateRoomName(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    U.getToastUtil().showShort("修改房间名成功");
                    mSongManageData.setRoomName(roomName);
                    mIOwnerManageView.showRoomName(roomName);
                    // TODO: 2019-06-23 由于此时的roomData和Room里面的不一样，需要发一个改变的事件
                    EventBus.getDefault().post(new RoomNameChangeEvent(roomName));
                } else {
                    U.getToastUtil().showShort(result.getErrmsg() + "");
                }

            }
        }, this);
    }

    public void getRecommendTag() {
        ApiMethods.subscribe(mGrabRoomServerApi.getStandBillBoards(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<RecommendTagModel> recommendTagModelArrayList = JSONObject.parseArray(result.getData().getString("items"), RecommendTagModel.class);
                    mIOwnerManageView.showRecommendSong(recommendTagModelArrayList);
                } else {
                    U.getToastUtil().showShort(result.getErrmsg() + "");
                }

            }
        }, this, new ApiMethods.RequestControl("getStandBillBoards", ApiMethods.ControlType.CancelThis));
    }

    // 向房主推荐新歌
    private void suggestSong(SongModel songModel) {
        MyLog.d(TAG, "suggestSong" + " songModel=" + songModel);
        HashMap<String, Object> map = new HashMap<>();
        map.put("itemID", songModel.getItemID());
        map.put("roomID", mSongManageData.getGameId());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mGrabRoomServerApi.suggestMusic(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "addSong process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    U.getToastUtil().showShort(songModel.getItemName() + " 推荐成功");
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AddSongEvent event) {
        if (mSongManageData.isGrabRoom() && !mSongManageData.isOwner()) {
            suggestSong(event.getSongModel());
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
