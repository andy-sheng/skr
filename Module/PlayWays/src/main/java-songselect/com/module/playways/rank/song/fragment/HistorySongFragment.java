package com.module.playways.rank.song.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.utils.U;
import com.common.view.ex.ExImageView;

import android.support.v7.widget.RecyclerView;

import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.rank.song.adapter.SongSelectAdapter;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.module.playways.rank.song.presenter.SongTagDetailsPresenter;
import com.module.playways.rank.song.view.ISongTagDetailView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HistorySongFragment extends BaseFragment implements ISongTagDetailView {

    public static final int DEFAULT_SONG_COUNT = 30;  // 每次从服务器拉去歌曲数
    int offset = 0; //偏移量
    List<SongModel> datas; // 歌曲的数据源

    RelativeLayout mMainActContainer;
    ExImageView mHistoryBack;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mHistoryRecycle;

    SongSelectAdapter songSelectAdapter;
    SongTagDetailsPresenter presenter;

    @Override
    public int initView() {
        return R.layout.history_song_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mHistoryBack = (ExImageView) mRootView.findViewById(R.id.history_back);
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mHistoryRecycle = (RecyclerView) mRootView.findViewById(R.id.history_recycle);

        mHistoryRecycle.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        songSelectAdapter = new SongSelectAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {

            }
        });
        mHistoryRecycle.setAdapter(songSelectAdapter);

        presenter = new SongTagDetailsPresenter(this);
        addPresent(presenter);
        presenter.getClickedMusicItmes(0, DEFAULT_SONG_COUNT);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                presenter.getRcomdMusicItems(offset, DEFAULT_SONG_COUNT);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });

        RxView.clicks(mHistoryBack)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    U.getFragmentUtils().popFragment(this);
                });

    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void loadSongsDetailItems(List<SongModel> list, int offset) {
        this.offset = offset;
        if (datas == null) {
            datas = new ArrayList<>();
        }
        if (songSelectAdapter != null && list != null) {
            datas.addAll(list);
            songSelectAdapter.setDataList(list);
            songSelectAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void loadSongsDetailItemsFail() {

    }
}
