package com.module.playways.grab.room.songmanager.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.inter.IGrabWishManageView;
import com.module.playways.grab.room.songmanager.adapter.WishSongAdapter;
import com.module.playways.grab.room.songmanager.model.GrabWishSongModel;
import com.module.playways.grab.room.songmanager.presenter.GrabWishSongPresenter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

/**
 * 愿望歌单
 */
public class GrabSongWishView extends FrameLayout implements IGrabWishManageView {

    public final static String TAG = "GrabSongWishView";

    private RecyclerView mRecyclerView;
    SmartRefreshLayout mRefreshLayout;
    WishSongAdapter mWishSongAdapter;
    GrabRoomData mGrabRoomData;

    GrabWishSongPresenter mGrabWishSongPresenter;

    public GrabSongWishView(Context context, GrabRoomData grabRoomData) {
        super(context);
        mGrabRoomData = grabRoomData;
        mGrabWishSongPresenter = new GrabWishSongPresenter(this, mGrabRoomData);
        initView();
    }

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
            public void onClickDeleteWish(View view, int position, GrabWishSongModel songModel) {
                // 删除用户选的歌曲
                mGrabWishSongPresenter.deleteWishSong(songModel);
            }

            @Override
            public void onClickSelectWish(View view, int position, GrabWishSongModel songModel) {
                // 添加用户选的歌曲
                mGrabWishSongPresenter.addWishSong(songModel);
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mWishSongAdapter);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false);
        mRefreshLayout.setEnableOverScrollDrag(false);

        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                mGrabWishSongPresenter.getListMusicSuggested();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }
        });

        mGrabWishSongPresenter.getListMusicSuggested();
    }


    public void destroy() {
        if (mGrabWishSongPresenter != null) {
            mGrabWishSongPresenter.destroy();
        }
    }

    @Override
    public void addGrabWishSongModels(List<GrabWishSongModel> grabWishSongModels) {
        mRefreshLayout.finishLoadMore();
        if (grabWishSongModels != null && grabWishSongModels.size() > 0) {
            mWishSongAdapter.getDataList().addAll(grabWishSongModels);
            mWishSongAdapter.notifyDataSetChanged();
        }

        if (mWishSongAdapter.getDataList() != null && mWishSongAdapter.getDataList().size() > 0) {
            // 没有更多了
        } else {
            // 空页面
        }
    }

    @Override
    public void deleteWishSong(GrabWishSongModel grabWishSongModel) {
        mWishSongAdapter.delete(grabWishSongModel);
    }
}
