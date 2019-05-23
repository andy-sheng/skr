package com.module.playways.grab.room.invite.presenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseFragment;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.utils.UserInfoDataUtils;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.inter.IGrabInviteView;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class GrabInvitePresenter {
    public final static String TAG = "GrabRedPkgPresenter";

    GrabRoomServerApi mGrabRoomServerApi;
    IGrabInviteView mIGrabInviteView;
    BaseFragment mBaseFragment;

    public GrabInvitePresenter(BaseFragment fragment, IGrabInviteView view) {
        this.mBaseFragment = fragment;
        this.mIGrabInviteView = view;
        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
    }

    public void getFriendList(int mOffset, int mLimit) {
        ApiMethods.subscribe(mGrabRoomServerApi.getRoomFriendList(mOffset, mLimit), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                mIGrabInviteView.finishRefresh();
                if (result.getErrno() == 0) {
//                    List<GrabFriendModel> grabFriendModelList = JSON.parseArray(result.getData().getString("friends"), GrabFriendModel.class);
//                    int newOffset = result.getData().getIntValue("offset");
//                    mIGrabInviteView.addInviteModelList(grabFriendModelList, newOffset);
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
        }, mBaseFragment);
    }


    public void getFansList(int mOffset, int mLimit) {
        ApiMethods.subscribe(mGrabRoomServerApi.getRoomFansList(mOffset, mLimit), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                mIGrabInviteView.finishRefresh();
                if (result.getErrno() == 0) {
                    List<JSONObject> list = JSON.parseArray(result.getData().getString("fans"), JSONObject.class);
                    List<UserInfoModel> userInfoModels = UserInfoDataUtils.parseRoomUserInfo(list);
                    int newOffset = result.getData().getIntValue("offset");
                    mIGrabInviteView.addInviteModelList(userInfoModels, newOffset);
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
        }, mBaseFragment);
    }


    public void inviteFriend(int roomID, UserInfoModel model) {
        MyLog.d(TAG, "deleteSong");
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", roomID);
        map.put("userID", model.getUserId());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mGrabRoomServerApi.inviteFriend(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    // 更新视图
                    mIGrabInviteView.hasInvitedModel(model);
                } else {
                    MyLog.w(TAG, "inviteFriend failed, " + " traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, mBaseFragment);
    }
}
