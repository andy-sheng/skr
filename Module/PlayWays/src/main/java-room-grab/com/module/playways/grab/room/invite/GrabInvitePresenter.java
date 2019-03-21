package com.module.playways.grab.room.invite;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.account.UserAccountManager;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.HandlerTaskTimer;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.inter.IGrabInviteView;
import com.module.playways.grab.room.model.GrabRedPkgTaskModel;
import com.module.playways.grab.room.view.IRedPkgCountDownView;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class GrabInvitePresenter extends RxLifeCyclePresenter {
    public final static String TAG = "GrabRedPkgPresenter";
    public static final int RED_PKG_COUNT_DOWN_TIME = 15000;
    GrabRoomServerApi mGrabRoomServerApi;
    IGrabInviteView view;

    GrabRoomData mGrabRoomData;

    public GrabInvitePresenter(IGrabInviteView view, GrabRoomData grabRoomData) {
        this.view = view;
        mGrabRoomData = grabRoomData;
        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
    }
}
