package com.module.rankingmode.song.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.common.base.BaseActivity;
import com.common.base.FragmentDataListener;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.fragment.PrepareResFragment;
import com.module.rankingmode.song.adapter.SongSelectAdapter;
import com.module.rankingmode.song.model.SongModel;
import com.module.rankingmode.song.model.TagModel;
import com.module.rankingmode.song.presenter.SongTagDetailsPresenter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

public class SongListView extends FrameLayout implements ISongTagDetailView {

    private static final int DEDAULT_COUNT = 6;

    int offset = 0;

    TagModel tagModel;

    LinearLayout mMainActContainer;

    SmartRefreshLayout mSongRefreshLayout;

    RecyclerView mSongListView;

    SongSelectAdapter mSongSelectAdapter;

    SongTagDetailsPresenter presenter;

    Handler mUiHanlder = new Handler();

    public SongListView(Context context) {
        this(context, null);
    }

    public SongListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SongListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.song_list_view_layout, this);
        mMainActContainer = findViewById(R.id.main_act_container);
        mSongRefreshLayout = findViewById(R.id.song_refreshLayout);
        mSongListView = findViewById(R.id.song_list_view);
        mSongListView.setHasFixedSize(true);
        mSongListView.setLayoutManager(new LinearLayoutManager(getContext()));

        presenter = new SongTagDetailsPresenter(this);

        mSongSelectAdapter = new SongSelectAdapter(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                SongModel songModel = (SongModel) model;
                U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder((BaseActivity) SongListView.this.getContext(), PrepareResFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(false)
                        .addDataBeforeAdd(0, songModel)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {

                            }
                        })
                        .build());
            }
        });
        mSongListView.setAdapter(mSongSelectAdapter);

        mSongRefreshLayout.setEnableLoadMore(true);
        mSongRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mSongRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                mUiHanlder.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (tagModel.getTagID() == 1) {
                            presenter.getRcomdMusicItems(offset, DEDAULT_COUNT);
                        } else {
                            presenter.getClickedMusicItmes(offset, DEDAULT_COUNT);
                        }
                    }
                }, 1500);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mUiHanlder.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSongRefreshLayout.finishRefresh();
                    }
                }, 1500);
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (presenter != null) {
            presenter.destroy();
        }
    }

    public void setTagModel(TagModel tagModel) {
        this.tagModel = tagModel;
        if (this.tagModel != null) {
            // todo 假数据，后期再改
            if (tagModel.getTagID() == 1) {
                presenter.getRcomdMusicItems(offset, DEDAULT_COUNT);
            } else if (tagModel.getTagID() == 2) {
                presenter.getClickedMusicItmes(offset, DEDAULT_COUNT);
            }

        }
    }

    @Override
    public void loadSongsDetailItems(List<SongModel> list, int offset) {
        this.offset = offset;
        mSongSelectAdapter.setDataList(list);
    }

    @Override
    public void loadSongsDetailItemsFail() {

    }
}
