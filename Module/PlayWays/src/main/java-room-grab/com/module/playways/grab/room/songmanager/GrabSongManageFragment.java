package com.module.playways.grab.room.songmanager;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExFrameLayout;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.inter.IGrabSongManageView;
import com.module.playways.grab.room.songmanager.tags.GrabSongTagsView;
import com.module.playways.grab.songselect.model.SpecialModel;
import com.module.rank.R;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

public class GrabSongManageFragment extends BaseFragment implements IGrabSongManageView {
    public final static String TAG = "GrabSongManageFragment";

    GrabRoomData mRoomData;

    ExImageView mIvAddSong;

    ExTextView mTvSelectedSong;

    ExFrameLayout mFlSongListContainer;

    SmartRefreshLayout mRefreshLayout;

    RecyclerView mRecyclerView;

    ExTextView mTvSelectedTag;

    TextView mTvFinish;

    ManageSongAdapter mManageSongAdapter;

    GrabSongManagePresenter mGrabSongManagePresenter;

    GrabSongTagsView mGrabSongTagsView;

    PopupWindow mPopupWindow;

    int mSpecialModelId;

    @Override
    public int initView() {
        return R.layout.grab_song_manage_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mGrabSongManagePresenter = new GrabSongManagePresenter(this, mRoomData);
        addPresent(mGrabSongManagePresenter);

        mIvAddSong = (ExImageView) mRootView.findViewById(R.id.iv_add_song);
        mTvSelectedSong = (ExTextView) mRootView.findViewById(R.id.tv_selected_song);
        mFlSongListContainer = (ExFrameLayout) mRootView.findViewById(R.id.fl_song_list_container);
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mTvSelectedTag = (ExTextView) mRootView.findViewById(R.id.tv_selected_tag);
        mTvFinish = (TextView) mRootView.findViewById(R.id.tv_finish);

        mSpecialModelId = mRoomData.getTagId();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mManageSongAdapter = new ManageSongAdapter();
        mRecyclerView.setAdapter(mManageSongAdapter);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                mGrabSongManagePresenter.getPlayBookList();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }
        });

        initListener();
    }

    @Override
    public void changeTagSuccess(SpecialModel specialModel) {
        mTvSelectedTag.setText(specialModel.getTagName());
        mSpecialModelId = specialModel.getTagID();
        Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45))
                .setStrokeColor(Color.parseColor("#202239"))
                .setStrokeWidth(U.getDisplayUtils().dip2px(2))
                .setSolidColor(Color.parseColor("#9B6C43"))
                .setCornersRadius(U.getDisplayUtils().dip2px(8))
                .build();

        mTvSelectedTag.setBackground(drawable);
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
    }

    @Override
    public void hasMoreSongList(boolean hasMore) {
        mRefreshLayout.setEnableLoadMore(hasMore);
    }

    @Override
    public void showTagList(List<SpecialModel> specialModelList) {
        if (mGrabSongTagsView == null) {
            mGrabSongTagsView.setSpecialModelList(specialModelList);
            mPopupWindow.setHeight(U.getDisplayUtils().dip2px(136));
            if (!mPopupWindow.isShowing()) {
                mPopupWindow.showAtLocation(mRootView, Gravity.TOP, 0, 0);
            }
        }
    }

    @Override
    public void updateSongList(List<GrabRoomSongModel> grabRoomSongModelsList) {
        mManageSongAdapter.setDataList(grabRoomSongModelsList);
    }

    private void initListener() {
        mIvAddSong.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {

            }
        });

        mTvSelectedTag.setOnClickListener(v -> {
            if (mGrabSongTagsView == null) {
                mGrabSongTagsView = new GrabSongTagsView(getContext());
                mGrabSongTagsView.setOnTagClickListener(specialModel -> {
                    mGrabSongManagePresenter.changeMusicTag(specialModel, mRoomData.getGameId());
                });

                mPopupWindow = new PopupWindow(mGrabSongTagsView);
                mPopupWindow.setWidth(mTvSelectedTag.getMeasuredWidth());
            }

            mGrabSongTagsView.setCurSpecialModel(mRoomData.getTagId());
            mGrabSongManagePresenter.getTagList();
        });

        mTvFinish.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        mManageSongAdapter.setOnClickDeleteListener(grabRoomSongModel -> {
            mGrabSongManagePresenter.deleteSong(grabRoomSongModel.getPlaybookItemID(), mRoomData.getRealRoundSeq());
        });
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mRoomData = (GrabRoomData) data;
        }
    }

    @Override
    protected boolean onBackPressed() {
        if (getActivity() != null) {
            getActivity().finish();
        }
        return super.onBackPressed();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
