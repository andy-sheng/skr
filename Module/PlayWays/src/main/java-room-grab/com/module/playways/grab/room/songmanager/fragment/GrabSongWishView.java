package com.module.playways.grab.room.songmanager.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.common.rxretrofit.ApiManager;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.grab.room.songmanager.adapter.RecommendSongAdapter;
import com.module.playways.grab.room.songmanager.adapter.WishSongAdapter;
import com.module.playways.grab.room.songmanager.presenter.GrabSongManagePresenter;
import com.module.playways.room.song.model.SongModel;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import io.reactivex.disposables.Disposable;

/**
 * 愿望歌单
 */
public class GrabSongWishView extends FrameLayout {

    public final static String TAG = "GrabSongWishView";

    private RecyclerView mRecyclerView;
    SmartRefreshLayout mRefreshLayout;
    Disposable mDisposable;
    WishSongAdapter mWishSongAdapter;
    int mOffset = 0;
    int mLimit = 20;

    GrabRoomServerApi mGrabRoomServerApi;
    GrabSongManagePresenter mGrabSongManagePresenter;


    public GrabSongWishView(@NonNull Context context) {
        super(context);
        initView();
    }

    public GrabSongWishView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public GrabSongWishView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.grab_song_wish_view_layout, this);

        mRefreshLayout = (SmartRefreshLayout) findViewById(R.id.refreshLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mWishSongAdapter = new WishSongAdapter(new WishSongAdapter.Listener() {
            @Override
            public void onClickDeleteWish(View view, int position, SongModel songModel) {

            }

            @Override
            public void onClickSelectWish(View view, int position, SongModel songModel) {

            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mWishSongAdapter);
        mGrabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false);
        mRefreshLayout.setEnableOverScrollDrag(false);

        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }
        });

    }

    public void destroy() {
        if (mGrabSongManagePresenter != null) {
            mGrabSongManagePresenter.destroy();
        }
    }
}
