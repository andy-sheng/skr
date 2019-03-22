package com.module.playways.grab.room.presenter;

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
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.model.GrabRedPkgTaskModel;
import com.module.playways.grab.room.view.IRedPkgCountDownView;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class GrabRedPkgPresenter extends RxLifeCyclePresenter {
    public final static String TAG = "GrabRedPkgPresenter";
    public static final int RED_PKG_COUNT_DOWN_TIME = 15000;
    GrabRoomServerApi mGrabRoomServerApi;
    IRedPkgCountDownView view;
    boolean mIsHasShow = false;

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

        ApiMethods.subscribe(mGrabRoomServerApi.checkRedPkg(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    List<GrabRedPkgTaskModel> redPkgTaskModelList = JSONArray.parseArray(result.getData().getString("tasks"), GrabRedPkgTaskModel.class);
                    if (redPkgTaskModelList != null) {
                        for (GrabRedPkgTaskModel model :
                                redPkgTaskModelList) {
                            if ("1".equals(model.getTaskID()) && !model.isDone()) {
                                view.redPkgCountDown(RED_PKG_COUNT_DOWN_TIME);
                                HandlerTaskTimer.newBuilder()
                                        .delay(RED_PKG_COUNT_DOWN_TIME)
                                        .compose(GrabRedPkgPresenter.this)
                                        .start(new HandlerTaskTimer.ObserverW() {
                                            @Override
                                            public void onNext(Integer integer) {
                                                getRedPkg();
                                            }
                                        });
                            }
                        }
                    } else {
                        MyLog.w(TAG, "checkRedPkg redPkgTaskModelList is null,traceid is " + result.getTraceId());
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

    private void getRedPkg() {
        MyLog.d(TAG, "getRedPkg");
        HashMap<String, Object> map = new HashMap<>();
        map.put("taskID", "1");
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        ApiMethods.subscribe(mGrabRoomServerApi.receiveCash(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "process" + " result=" + result.getErrno());
                if (result.getErrno() == 0) {
                    GrabRedPkgTaskModel redPkgTaskModel = JSONObject.parseObject(result.getData().getString("task"), GrabRedPkgTaskModel.class);
                    if (redPkgTaskModel != null) {
                        if ("1".equals(redPkgTaskModel.getTaskID()) && redPkgTaskModel.isDone()) {
                            mIsHasShow = true;
                            view.getCashSuccess(Float.parseFloat(redPkgTaskModel.getRedbagExtra().getCash()));
                        } else {
                            MyLog.w(TAG, "getRedPkg task id is  " + redPkgTaskModel.getTaskID() + ", isDone is " + redPkgTaskModel.isDone());
                        }
                    } else {
                        MyLog.w(TAG, "getRedPkg redPkgTaskModelList is null, " + " traceid is " + result.getTraceId());
                    }
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
