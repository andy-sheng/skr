package com.module.playways.grab.room.songmanager;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExLinearLayout;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.inter.IGrabSongManageView;
import com.module.playways.grab.room.songmanager.tags.GrabSongTagsView;
import com.component.busilib.friends.SpecialModel;
import com.module.playways.grab.room.songmanager.tags.GrabTagsAdapter;
import com.module.playways.room.song.fragment.GrabSearchSongFragment;
import com.module.playways.room.song.model.SongModel;
import com.module.rank.R;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

public class GrabSongManageFragment extends BaseFragment implements IGrabSongManageView {
    public final static String TAG = "GrabSongManageFragment";

    GrabRoomData mRoomData;

    ExTextView mSearchSongIv;

    ExTextView mTvSelectedSong;

    ExLinearLayout mFlSongListContainer;

    SmartRefreshLayout mRefreshLayout;

    RecyclerView mRecyclerView;

    ExTextView mTvSelectedTag;

    TextView mTvFinish;

    ManageSongAdapter mManageSongAdapter;

    GrabSongManagePresenter mGrabSongManagePresenter;

    GrabSongTagsView mGrabSongTagsView;

    PopupWindow mPopupWindow;

    RelativeLayout mRlContent;

    int mSpecialModelId;

    @Override
    public int initView() {
        return R.layout.grab_song_manage_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mGrabSongManagePresenter = new GrabSongManagePresenter(this, mRoomData);
        addPresent(mGrabSongManagePresenter);

        mSearchSongIv = mRootView.findViewById(R.id.search_song_iv);
        mTvSelectedSong = (ExTextView) mRootView.findViewById(R.id.tv_selected_song);
        mFlSongListContainer = (ExLinearLayout) mRootView.findViewById(R.id.fl_song_list_container);
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mTvSelectedTag = (ExTextView) mRootView.findViewById(R.id.selected_tag);
        mTvFinish = (TextView) mRootView.findViewById(R.id.tv_finish);
        mRlContent = (RelativeLayout) mRootView.findViewById(R.id.rl_content);

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

        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        defaultItemAnimator.setAddDuration(50);
        defaultItemAnimator.setRemoveDuration(50);
        mRecyclerView.setItemAnimator(defaultItemAnimator);

        initListener();
        mGrabSongManagePresenter.getPlayBookList();

        if (mRoomData.getSpecialModel() != null) {
            setTagTv(mRoomData.getSpecialModel());
        }
    }

    private void setTagTv(SpecialModel specialModel) {
        int color = Color.parseColor("#68ABD3");
        if (!TextUtils.isEmpty(specialModel.getBgColor())) {
            color = Color.parseColor(specialModel.getBgColor());
        }

        mRoomData.setSpecialModel(specialModel);
        mRoomData.setTagId(specialModel.getTagID());

        mTvSelectedTag.setText(specialModel.getTagName());
        mSpecialModelId = specialModel.getTagID();
        Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45))
                .setStrokeColor(Color.parseColor("#202239"))
                .setStrokeWidth(U.getDisplayUtils().dip2px(2))
                .setSolidColor(color)
                .setCornersRadius(U.getDisplayUtils().dip2px(8))
                .build();

        mTvSelectedTag.setBackground(drawable);
    }

    @Override
    public void changeTagSuccess(SpecialModel specialModel) {
        setTagTv(specialModel);
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
    }

    @Override
    public void showNum(int num) {
        mTvSelectedSong.setText(String.format("已点%d首", num));
    }

    @Override
    public void deleteSong(GrabRoomSongModel grabRoomSongModel) {
        mManageSongAdapter.deleteSong(grabRoomSongModel);
    }

    @Override
    public void hasMoreSongList(boolean hasMore) {
        mRefreshLayout.setEnableLoadMore(hasMore);
        mRefreshLayout.finishLoadMore();
    }

    @Override
    public void showTagList(List<SpecialModel> specialModelList) {
        int height = U.getDisplayUtils().dip2px(specialModelList.size() > 4 ? 150 : (55 * (specialModelList.size() - 1)));
        if (mGrabSongTagsView != null) {
            mGrabSongTagsView.setSpecialModelList(specialModelList);
            mPopupWindow.setHeight(height);
        }

        int[] location = new int[2];
        mTvSelectedTag.getLocationOnScreen(location);
//        mPopupWindow.showAtLocation(mTvSelectedTag, Gravity.NO_GRAVITY, location[0], location[1] - height - U.getDisplayUtils().dip2px(5));
        mPopupWindow.showAsDropDown(mTvSelectedTag);
        Drawable drawable = U.getDrawable(R.drawable.zhuanchang_shouqi_up);
        drawable.setBounds(new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
        mTvSelectedTag.setCompoundDrawables(null, null,
                drawable, null);
    }

    @Override
    public void updateSongList(List<GrabRoomSongModel> grabRoomSongModelsList) {
        mManageSongAdapter.setDataList(grabRoomSongModelsList);
    }

    private void initListener() {
        mSearchSongIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder((BaseActivity) getContext(), GrabSearchSongFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {
                                if (requestCode == 0 && resultCode == 0 && obj != null) {
                                    SongModel model = (SongModel) obj;
                                    MyLog.d(TAG, "onFragmentResult" + " model=" + model);
                                    mGrabSongManagePresenter.addSong(model);
                                }
                            }
                        })
                        .build());
            }
        });

        mTvSelectedTag.setOnClickListener(v -> {
            if (mGrabSongTagsView == null) {
                mGrabSongTagsView = new GrabSongTagsView(getContext());

                mGrabSongTagsView.setOnTagClickListener(new GrabTagsAdapter.OnTagClickListener() {
                    @Override
                    public void onClick(SpecialModel specialModel) {
                        mGrabSongManagePresenter.changeMusicTag(specialModel, mRoomData.getGameId());
                    }

                    @Override
                    public void dismissDialog() {
                        if(mPopupWindow != null){
                            mPopupWindow.dismiss();
                        }
                    }
                });
                mPopupWindow = new PopupWindow(mGrabSongTagsView);
                mPopupWindow.setWidth(mTvSelectedTag.getWidth());
                mPopupWindow.setOutsideTouchable(true);
                mPopupWindow.setFocusable(true);

                mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        Drawable drawable = U.getDrawable(R.drawable.zhuanchang_shouqi);
                        drawable.setBounds(new Rect(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
                        mTvSelectedTag.setCompoundDrawables(null, null,
                                drawable, null);
                    }
                });
            }

            mGrabSongTagsView.setCurSpecialModel(mSpecialModelId);

            if (mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            } else {
                mGrabSongManagePresenter.getTagList();
            }
        });

        mTvFinish.setOnClickListener(v -> {
            finish();
        });

        mRootView.setOnClickListener(v -> {
            finish();
        });

        mRlContent.setOnClickListener(v -> {

        });

        mManageSongAdapter.setOnClickDeleteListener(grabRoomSongModel -> {
            mGrabSongManagePresenter.deleteSong(grabRoomSongModel);
        });

        mManageSongAdapter.setGrabRoomData(mRoomData);
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
        return super.onBackPressed();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
