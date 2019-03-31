package com.module.playways.grab.room.invite;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.inter.IGrabInviteView;
import com.module.playways.grab.room.model.GrabFriendModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class GrabInvitePresenter extends RxLifeCyclePresenter {
    public final static String TAG = "GrabRedPkgPresenter";
    GrabRoomServerApi mGrabRoomServerApi;
    IGrabInviteView mIGrabInviteView;
    List<GrabFriendModel> mGrabFriendModelList = new ArrayList<>();

    int mOffset = 0;
    int mLimit = 50;

    GrabRoomData mGrabRoomData;

    public GrabInvitePresenter(IGrabInviteView view, GrabRoomData grabRoomData) {
        this.mIGrabInviteView = view;
        mGrabRoomData = grabRoomData;
        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
    }

    public void getFriendList() {
        ApiMethods.subscribe(mGrabRoomServerApi.getRoomFriendList(mOffset, mLimit), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                mIGrabInviteView.finishRefresh();

                if (result.getErrno() == 0) {
                    List<GrabFriendModel> grabFriendModelList = JSON.parseArray(result.getData().getString("friends"), GrabFriendModel.class);
                    if (grabFriendModelList == null || grabFriendModelList.size() == 0) {
                        //没有更多了
                        mIGrabInviteView.hasMore(false);
                        return;
                    }

                    mGrabFriendModelList.addAll(grabFriendModelList);
                    mIGrabInviteView.updateFriendList(mGrabFriendModelList);
                    mOffset = result.getData().getIntValue("offset");
                } else {
                    MyLog.w(TAG, "getFriendList failed, " + result.getErrmsg() + ", traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                mIGrabInviteView.finishRefresh();
            }

            @Override
            public void onError(Throwable e) {
                mIGrabInviteView.finishRefresh();
                MyLog.e(TAG, e);
            }
        }, this);
    }

    public void inviteFriend(int roomID, GrabFriendModel grabFriendModel) {
        MyLog.d(TAG, "deleteSong");
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", roomID);
        map.put("userID", grabFriendModel.getUserID());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mGrabRoomServerApi.inviteFriend(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    grabFriendModel.setInvited(true);
                    mIGrabInviteView.updateFriendList(mGrabFriendModelList);
                } else {
                    MyLog.w(TAG, "inviteFriend failed, " + " traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, this);
    }
}
