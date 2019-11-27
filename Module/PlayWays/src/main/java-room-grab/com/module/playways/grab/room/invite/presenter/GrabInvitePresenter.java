package com.module.playways.grab.room.invite.presenter;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseFragment;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.core.userinfo.utils.UserInfoDataUtils;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.view.ex.ExTextView;
import com.module.playways.doubleplay.DoubleRoomServerApi;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.inter.IGrabInviteView;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class GrabInvitePresenter {
    public final String TAG = "GrabRedPkgPresenter";

    GrabRoomServerApi mGrabRoomServerApi;
    DoubleRoomServerApi mDoubleRoomServerApi;
    IGrabInviteView mIGrabInviteView;
    BaseFragment mBaseFragment;

    int roomID;
    int gameType;

    public GrabInvitePresenter(BaseFragment fragment, IGrabInviteView view, int roomID, int gameType) {
        this.mBaseFragment = fragment;
        this.mIGrabInviteView = view;
        this.roomID = roomID;
        this.gameType = gameType;
        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
        mDoubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi.class);
    }

    public void getFriendList(int mOffset, int mLimit) {
        UserInfoManager.getInstance().getMyFriends(UserInfoManager.ONLINE_PULL_GAME, roomID, gameType, new UserInfoManager.UserInfoListCallback() {
            @Override
            public void onSuccess(UserInfoManager.FROM from, int offset, List<UserInfoModel> list) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mIGrabInviteView.finishRefresh();
                        mIGrabInviteView.addInviteModelList(list, mOffset, offset);
                    }
                });
            }
        });
    }


    public void getFansList(int mOffset, int mLimit) {
        ApiMethods.subscribe(mGrabRoomServerApi.getRoomFansList(mOffset, mLimit, roomID, gameType), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                mIGrabInviteView.finishRefresh();
                if (result.getErrno() == 0) {
                    List<JSONObject> list = JSON.parseArray(result.getData().getString("fans"), JSONObject.class);
                    List<UserInfoModel> userInfoModels = UserInfoDataUtils.parseRoomUserInfo(list);
                    int newOffset = result.getData().getIntValue("offset");
                    mIGrabInviteView.addInviteModelList(userInfoModels, mOffset, newOffset);
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


    public void inviteGrabFriend(int roomID, int tagID, UserInfoModel model, ExTextView view) {
        MyLog.d(TAG, "inviteGrabFriend" + " roomID=" + roomID + " model=" + model + " view=" + view);
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", roomID);
        map.put("tagID", tagID);
        map.put("userID", model.getUserId());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mGrabRoomServerApi.inviteFriend(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    // 更新视图
                    mIGrabInviteView.updateInvited(view);
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

    public void inviteDoubleFriend(int roomID, UserInfoModel model, ExTextView view) {
        MyLog.d(TAG, "inviteDoubleFriend" + " roomID=" + roomID + " model=" + model + " view=" + view);
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", roomID);
        map.put("inviteUserID", model.getUserId());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mDoubleRoomServerApi.roomSendInvite(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    // 更新视图
                    mIGrabInviteView.updateInvited(view);
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

    public void inviteMicFriend(int roomID, UserInfoModel model, ExTextView view) {
        MyLog.d(TAG, "inviteMicFriend" + " roomID=" + roomID + " model=" + model + " view=" + view);
        HashMap<String, Object> map = new HashMap<>();
        map.put("roomID", roomID);
        map.put("userID", model.getUserId());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mDoubleRoomServerApi.micRoomSendInvite(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    // 更新视图
                    mIGrabInviteView.updateInvited(view);
                } else {
                    MyLog.w(TAG, "inviteMicFriend failed, " + " traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, mBaseFragment);
    }
}
