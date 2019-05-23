package com.module.playways.grab.room.invite.presenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.utils.UserInfoDataUtils;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.invite.model.GrabFriendModel;
import com.module.playways.grab.room.invite.view.IInviteSearchView;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class InviteSearchPresenter extends RxLifeCyclePresenter {

    GrabRoomServerApi mGrabRoomServerApi;
    IInviteSearchView mView;

    public InviteSearchPresenter(IInviteSearchView view) {
        this.mView = view;
        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
    }

    public void inviteFriend(int roomID, GrabFriendModel model) {
        MyLog.d(TAG, "deleteSong");
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", roomID);
        map.put("userID", model.getUserID());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mGrabRoomServerApi.inviteFriend(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    model.setInvited(true);
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

    public void searchFans(String searchContent) {
        ApiMethods.subscribe(mGrabRoomServerApi.searchFans(searchContent), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<JSONObject> list = JSON.parseArray(result.getData().getString("fans"), JSONObject.class);
                    List<UserInfoModel> userInfoModels = UserInfoDataUtils.parseRoomUserInfo(list);
                    if (mView != null) {
                        mView.showUserInfoList(userInfoModels);
                    }
                }
            }
        }, this);
    }
}
