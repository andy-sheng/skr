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
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.songmanager.model.RecommendTagModel;
import com.module.playways.grab.room.songmanager.view.IOwnerManageView;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class OwnerManagePresenter extends RxLifeCyclePresenter {
    IOwnerManageView mIOwnerManageView;
    GrabRoomServerApi mGrabRoomServerApi;
    GrabRoomData mGrabRoomData;

    public OwnerManagePresenter(IOwnerManageView IOwnerManageView, GrabRoomData grabRoomData) {
        mIOwnerManageView = IOwnerManageView;
        mGrabRoomData = grabRoomData;
        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
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
                    mGrabRoomData.setRoomName(roomName);
                    mIOwnerManageView.showRoomName(roomName);
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
        }, this,new ApiMethods.RequestControl("getStandBillBoards",ApiMethods.ControlType.CancelThis));
    }
}
