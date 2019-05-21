package com.module.playways.grab.room.songmanager.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseFragment;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.songmanager.model.RecommendTagModel;
import com.module.playways.grab.room.songmanager.adapter.RecommendSongAdapter;
import com.module.playways.room.song.model.SongModel;

import java.util.List;

public class RecommendSongFragment extends BaseFragment {
    public final static String TAG = "GrabSongManageFragment";
    private RecyclerView mRecyclerView;
    private RecommendTagModel mRecommendTagModel;
    RecommendSongAdapter mRecommendSongAdapter;
    GrabRoomServerApi mGrabRoomServerApi;
    int mTotalNum = 0;
    int mLimit = 100;

    @Override
    public int initView() {
        return R.layout.recommend_song_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mRecommendSongAdapter = new RecommendSongAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mRecommendSongAdapter);
        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
        getSongList();
    }

    public void setRecommendTagModel(RecommendTagModel recommendTagModel) {
        mRecommendTagModel = recommendTagModel;
    }

    private void getSongList() {
        ApiMethods.subscribe(mGrabRoomServerApi.getListStandBoards(mRecommendTagModel.getType(), 0, 100), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<SongModel> recommendTagModelArrayList = JSONObject.parseArray(result.getData().getString("items"), SongModel.class);
                    mRecommendSongAdapter.setDataList(recommendTagModelArrayList);
                } else {
                    U.getToastUtil().showShort(result.getErrmsg() + "");
                }

            }
        }, this);
    }

    @Override
    protected boolean onBackPressed() {
        return super.onBackPressed();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
