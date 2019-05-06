package com.module.playways.grab.room.presenter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.ToastUtils;
import com.common.utils.U;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.model.GrabRedPkgTaskModel;
import com.module.playways.grab.room.view.IRedPkgCountDownView;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class GrabRedPkgPresenter extends RxLifeCyclePresenter {
    public final static String TAG = "GrabRedPkgPresenter";
    public final static String KEY_HAS_RESEIVE_RED_PKG = "hasReceiveRedPkg";
    public static final int RED_PKG_COUNT_DOWN_TIME = 15000;
    public static final long ERROR_CODE_RED_RULE = 8302202;
    GrabRoomServerApi mGrabRoomServerApi;
    IRedPkgCountDownView view;
    boolean mIsHasShow = false;
    boolean mCanReceive = false;

    public GrabRedPkgPresenter(IRedPkgCountDownView view) {
        this.view = view;
        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
    }

    public void checkRedPkg() {
        if (mIsHasShow) {
            MyLog.d(TAG, "has checkRedPkg");
            return;
        }

        if (!UserAccountManager.getInstance().hasAccount()) {
            MyLog.w(TAG, "no account");
            return;
        }

        if (U.getPreferenceUtils().getSettingBoolean(KEY_HAS_RESEIVE_RED_PKG, false)) {
            MyLog.w(TAG, "has receive red pkg");
            mIsHasShow = true;
            return;
        }

        ApiMethods.subscribe(mGrabRoomServerApi.checkNewBieTask(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.w(TAG, "process" + " result=" + result);
                if (result.getErrno() == 0) {
                    GrabRedPkgTaskModel grabRedPkgTaskModel = JSONObject.parseObject(result.getData().getString("task"), GrabRedPkgTaskModel.class);
                    if (grabRedPkgTaskModel != null && !grabRedPkgTaskModel.isDone()) {
//                        view.redPkgCountDown(RED_PKG_COUNT_DOWN_TIME);
                        mCanReceive = true;
                    } else {
                        MyLog.w(TAG, "checkRedPkg redPkgTaskModelList is null or grabRedPkgTaskModel is done,traceid is " + result.getTraceId());
                    }
                } else {
                    MyLog.w(TAG, "checkRedPkg failed, " + " ,traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, this);
    }

    public boolean isCanReceive() {
        return mCanReceive;
    }

    public void getRedPkg() {
        MyLog.d(TAG, "getRedPkg");
        long ts = System.currentTimeMillis();
        HashMap<String, Object> map = new HashMap<>();
        map.put("taskID", "1");
        map.put("nonce", ts);
        String sign = U.getMD5Utils().MD5_32("skrer" + "|"
                + MyUserInfoManager.getInstance().getUid() + "|"
                + "OTQ2MmE0ZjAtZDNkNi00Mzc1LWE1OdyN" + "|"
                + ts);
        map.put("sign", sign);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mGrabRoomServerApi.triggerNewBieTask(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.w(TAG, "process" + " result=" + result);
                if (result.getErrno() == 0) {
                    GrabRedPkgTaskModel grabRedPkgTaskModel = JSONObject.parseObject(result.getData().getString("task"), GrabRedPkgTaskModel.class);
                    if (grabRedPkgTaskModel != null && grabRedPkgTaskModel.isDone()) {
                        mIsHasShow = true;
                        mCanReceive = false;
                        U.getPreferenceUtils().setSettingBoolean(KEY_HAS_RESEIVE_RED_PKG, true);
                        view.getCashSuccess(Float.parseFloat(grabRedPkgTaskModel.getRedbagExtra().getCash()));
                    } else {
                        MyLog.w(TAG, "getRedPkg redPkgTaskModelList is null or grabRedPkgTaskModel is not done, " + " traceid is " + result.getTraceId());
                    }
                } else if (result.getErrno() == ERROR_CODE_RED_RULE) {
//                    ToastUtils.showShort(result.getErrmsg());
                    view.showGetRedPkgFailed();
                } else {
                    MyLog.w(TAG, "getRedPkg failed, " + " traceid is " + result.getTraceId());
                }
            }

            @Override
            public void onError(Throwable e) {
                MyLog.e(TAG, e);
            }
        }, this);
    }
}
