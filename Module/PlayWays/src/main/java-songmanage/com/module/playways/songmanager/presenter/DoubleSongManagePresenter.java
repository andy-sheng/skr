package com.module.playways.songmanager.presenter;

import com.alibaba.fastjson.JSONObject;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.songmanager.model.RecommendTagModel;
import com.module.playways.songmanager.view.ISongManageView;

import java.util.List;

public class DoubleSongManagePresenter extends RxLifeCyclePresenter {

    GrabRoomServerApi mGrabRoomServerApi;
    ISongManageView mSongManageView;

    public DoubleSongManagePresenter(ISongManageView songManageView) {
        this.mSongManageView = songManageView;
        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
    }

    public void getRecommendTag() {
        ApiMethods.subscribe(mGrabRoomServerApi.getDoubleStandBillBoards(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<RecommendTagModel> recommendTagModelArrayList = JSONObject.parseArray(result.getData().getString("items"), RecommendTagModel.class);
                    mSongManageView.showRecommendSong(recommendTagModelArrayList);
                } else {
                    U.getToastUtil().showShort(result.getErrmsg() + "");
                }

            }
        }, this, new ApiMethods.RequestControl("getStandBillBoards", ApiMethods.ControlType.CancelThis));
    }
}
