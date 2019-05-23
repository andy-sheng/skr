package com.module.playways.grab.room.invite.presenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.invite.model.GrabFriendModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class InviteSearchPresenter extends RxLifeCyclePresenter {

    GrabRoomServerApi mGrabRoomServerApi;

    public InviteSearchPresenter() {
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
                    if (list != null && list.size() > 0) {
                        List<UserInfoModel> userInfoModels = new ArrayList<>();
                        for (JSONObject jsonObject : list) {
                            UserInfoModel userInfoModel = new UserInfoModel();
                            jsonObject.getIntValue("userID");
                            jsonObject.getString("avatar");
                            jsonObject.getBooleanValue("isFollow");
                            jsonObject.getBooleanValue("isFriend");
                            jsonObject.getBooleanValue("isOnline");
                            jsonObject.getString("nickname");
                            jsonObject.getLongValue("offlineTime");
                            jsonObject.getLongValue("onlineTime");
                            jsonObject.getIntValue("sex");
                            jsonObject.getIntValue("status");
                        }
                    }
                }
            }
        }, this);
    }
}
