package com.module.playways.songmanager.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.inter.IGrabWishManageView;
import com.module.playways.songmanager.adapter.WishSongAdapter;
import com.module.playways.songmanager.model.GrabWishSongModel;
import com.module.playways.songmanager.presenter.GrabWishSongPresenter;
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

    LoadService mLoadService;
    long mOffset = 0;   //此处是时间戳，int64

    public GrabSongWishView(Context context, GrabRoomData grabRoomData) {
        super(context);
        mGrabRoomData = grabRoomData;
        mGrabWishSongPresenter = new GrabWishSongPresenter(this, mGrabRoomData);
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
                mGrabWishSongPresenter.getListMusicSuggested(mOffset);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mGrabWishSongPresenter.getListMusicSuggested(0);
            }
        });

        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new GrabWishEmptyCallback())
                .build();
        mLoadService = mLoadSir.register(mRefreshLayout, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                mGrabWishSongPresenter.getListMusicSuggested(0);
            }
        });
        mGrabWishSongPresenter.getListMusicSuggested(0);
    }

    public void destroy() {
        if (mGrabWishSongPresenter != null) {
            mGrabWishSongPresenter.destroy();
        }
    }

    public void tryLoad() {
//        if (mWishSongAdapter.getDataList().isEmpty()) {
        mGrabWishSongPresenter.getListMusicSuggested(0);
//        }
    }

    @Override
    public void addGrabWishSongModels(boolean clear, long newOffset, List<GrabWishSongModel> grabWishSongModels) {
        if (clear) {
            mWishSongAdapter.getDataList().clear();
        }
        mOffset = newOffset;
        mRefreshLayout.finishLoadMore();
        if (grabWishSongModels != null && grabWishSongModels.size() > 0) {
            mWishSongAdapter.getDataList().addAll(grabWishSongModels);
            mWishSongAdapter.notifyDataSetChanged();
        }
        if (mWishSongAdapter.getDataList() != null && mWishSongAdapter.getDataList().size() > 0) {
            // 没有更多了
            mLoadService.showSuccess();
        } else {
            // 空页面
            mLoadService.showCallback(GrabWishEmptyCallback.class);
        }
    }

    @Override
    public void deleteWishSong(GrabWishSongModel grabWishSongModel) {
        mWishSongAdapter.delete(grabWishSongModel);
        if (mWishSongAdapter.getDataList() != null && mWishSongAdapter.getDataList().size() > 0) {
            // 没有更多了
            mLoadService.showSuccess();
        } else {
            // 空页面
            mLoadService.showCallback(GrabWishEmptyCallback.class);
        }
    }

}
