package com.module.playways.grab.room.songmanager.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.common.base.BaseFragment;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.songmanager.adapter.RecommendSongAdapter;
import com.module.playways.grab.room.songmanager.model.RecommendTagModel;
import com.module.playways.room.song.model.SongModel;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

public class RecommendSongFragment extends BaseFragment {
    public final static String TAG = "GrabSongManageFragment";
    private RecyclerView mRecyclerView;
    private RecommendTagModel mRecommendTagModel;
    RecommendSongAdapter mRecommendSongAdapter;
    GrabRoomServerApi mGrabRoomServerApi;
    SmartRefreshLayout mRefreshLayout;
    int mOffset = 0;
    int mLimit = 20;

    @Override
    public int initView() {
        return R.layout.recommend_song_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mRecommendSongAdapter = new RecommendSongAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mRecommendSongAdapter);
        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false);
        mRefreshLayout.setEnableOverScrollDrag(false);

        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                getSongList();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }
        });

        Bundle bundle = getArguments();
        mRecommendTagModel = (RecommendTagModel) bundle.getSerializable("tag_model");
        getSongList();
    }

    private void getSongList() {
        if (mRecommendTagModel == null) {
            MyLog.e(TAG, "getSongList mRecommendTagModel is null");
            return;
        }
        ApiMethods.subscribe(mGrabRoomServerApi.getListStandBoards(mRecommendTagModel.getType(), mOffset, mLimit), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                mRefreshLayout.finishLoadMore();

                if (result.getErrno() == 0) {
                    List<SongModel> recommendTagModelArrayList = JSONObject.parseArray(result.getData().getString("items"), SongModel.class);
                    if (recommendTagModelArrayList == null || recommendTagModelArrayList.size() == 0) {
                        mRefreshLayout.setEnableLoadMore(false);
                        return;
                    }

                    mRecommendSongAdapter.getDataList().addAll(recommendTagModelArrayList);
                    mOffset = mRecommendSongAdapter.getDataList().size();
                    mRecommendSongAdapter.notifyDataSetChanged();
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
