package com.module.playways.songmanager.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.component.busilib.friends.SpecialModel;
import com.module.playways.R;
import com.module.playways.doubleplay.DoubleRoomData;
import com.module.playways.songmanager.SongManagerActivity;
import com.module.playways.songmanager.adapter.ManageSongAdapter;
import com.module.playways.songmanager.model.GrabRoomSongModel;
import com.module.playways.songmanager.presenter.DoubleExitSongManagePresenter;
import com.module.playways.songmanager.view.IExistSongManageView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

public class DoubleExistSongManageFragment extends BaseFragment implements IExistSongManageView {

    CommonTitleBar mTitlebar;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mRecyclerView;

    DoubleExitSongManagePresenter mPresenter;
    DoubleRoomData mDoubleRoomData;
    ManageSongAdapter mManageSongAdapter;

    @Override
    public int initView() {
        return R.layout.double_exist_song_manage_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mTitlebar = mRootView.findViewById(R.id.titlebar);
        mRefreshLayout = mRootView.findViewById(R.id.refreshLayout);
        mRecyclerView = mRootView.findViewById(R.id.recycler_view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mManageSongAdapter = new ManageSongAdapter(SongManagerActivity.TYPE_FROM_DOUBLE);
        mRecyclerView.setAdapter(mManageSongAdapter);

        mPresenter = new DoubleExitSongManagePresenter(this, mDoubleRoomData);
        addPresent(mPresenter);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                mPresenter.getPlayBookList();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mPresenter.getPlayBookList();
            }
        });

        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        defaultItemAnimator.setAddDuration(50);
        defaultItemAnimator.setRemoveDuration(50);
        mRecyclerView.setItemAnimator(defaultItemAnimator);

        mPresenter.getPlayBookList();


        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().popFragment(DoubleExistSongManageFragment.this);
            }
        });

        mManageSongAdapter.setOnClickDeleteListener(grabRoomSongModel -> {
            mPresenter.deleteSong(grabRoomSongModel);
        });
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mDoubleRoomData = (DoubleRoomData) data;
        }
    }

    @Override
    public void showTagList(List<SpecialModel> specialModelList) {

    }

    @Override
    public void updateSongList(List<GrabRoomSongModel> grabRoomSongModelsList) {
        mManageSongAdapter.setDataList(grabRoomSongModelsList);
    }

    @Override
    public void hasMoreSongList(boolean hasMore) {
        mRefreshLayout.setEnableLoadMore(hasMore);
        mRefreshLayout.finishLoadMore();
    }

    @Override
    public void changeTagSuccess(SpecialModel specialModel) {

    }

    @Override
    public void showNum(int num) {

    }

    @Override
    public void deleteSong(GrabRoomSongModel grabRoomSongModel) {
        mManageSongAdapter.deleteSong(grabRoomSongModel);
    }
}
