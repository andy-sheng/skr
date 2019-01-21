package com.module.playways.grab.room.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.common.base.BaseFragment;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.playways.RoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.model.GrabResultInfoModel;
import com.module.rank.R;

import java.util.List;

/**
 * 一唱到底结果页面
 */
public class GrabResultFragment extends BaseFragment {

    RoomData mRoomData;

    @Override
    public int initView() {
        return R.layout.grab_result_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        List<GrabResultInfoModel> list = mRoomData.getResultList();
        if (list == null || list.isEmpty()) {
            /**
             * 游戏结束会由sync或者push触发
             * push触发的话带着结果数据
             */
            syncFromServer();
        } else {
            bindData(list);
        }
    }

    private void bindData(List<GrabResultInfoModel> list) {

    }

    private void syncFromServer() {
        GrabRoomServerApi getStandResult = ApiManager.getInstance().createService(GrabRoomServerApi.class);
        ApiMethods.subscribe(getStandResult.getStandResult(mRoomData.getGameId()), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {

                }
            }
        }, this);
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mRoomData = (RoomData) data;
        }
    }
}
