package com.module.playways.grab.room.invite.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.dialog.view.StrokeTextView;
import com.kingja.loadsir.callback.Callback;
import com.kingja.loadsir.core.LoadService;
import com.kingja.loadsir.core.LoadSir;
import com.module.playways.R;
import com.module.playways.grab.room.inter.IGrabInviteView;
import com.module.playways.grab.room.invite.adapter.InviteFirendAdapter;
import com.module.playways.grab.room.invite.fragment.InviteFriendFragment2;
import com.module.playways.grab.room.invite.fragment.InviteSearchFragment;
import com.module.playways.grab.room.invite.presenter.GrabInvitePresenter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.zq.relation.callback.FansEmptyCallback;
import com.zq.relation.callback.FriendsEmptyCallback;

import java.util.List;

public class InviteFriendView extends RelativeLayout implements IGrabInviteView {

    SmartRefreshLayout mRefreshLayout;
    RecyclerView mRecyclerView;
    InviteFirendAdapter mInviteFirendAdapter;

    private int mMode = UserInfoManager.RELATION.FRIENDS.getValue();
    private int mOffset = 0; // 偏移量
    private int DEFAULT_COUNT = 30; // 每次拉去最大值
    private boolean mHasMore = true;
    private int mFrom;

    GrabInvitePresenter mGrabInvitePresenter;
    BaseFragment mBaseFragment;
    int mRoomID;
    LoadService mLoadService;

    public InviteFriendView(BaseFragment fragment, int from, int roomID, int mode) {
        super(fragment.getContext());
        init(fragment, from, roomID, mode);
    }

    private void init(BaseFragment fragment, int from, int roomID, int mode) {
        this.mFrom = from;
        this.mBaseFragment = fragment;
        this.mRoomID = roomID;
        this.mMode = mode;
        inflate(fragment.getContext(), R.layout.invite_view_layout, this);

        mRefreshLayout = (SmartRefreshLayout) findViewById(R.id.refreshLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mInviteFirendAdapter = new InviteFirendAdapter(new InviteFirendAdapter.OnInviteClickListener() {

            @Override
            public void onClick(UserInfoModel model, ExTextView view) {
                if (mFrom == InviteFriendFragment2.FROM_GRAB_ROOM) {
                    mGrabInvitePresenter.inviteGrabFriend(mRoomID, model, view);
                } else if (mFrom == InviteFriendFragment2.FROM_DOUBLE_ROOM) {
                    mGrabInvitePresenter.inviteDoubleFriend(mRoomID, model, view);
                }

            }

            @Override
            public void onClickSearch() {
                Bundle bundle = new Bundle();
                bundle.putSerializable(InviteSearchFragment.INVITE_SEARCH_MODE, mMode);
                bundle.putSerializable(InviteSearchFragment.INVITE_ROOM_ID, mRoomID);
                U.getFragmentUtils().addFragment(FragmentUtils
                        .newAddParamsBuilder((BaseActivity) getContext(), InviteSearchFragment.class)
                        .setUseOldFragmentIfExist(false)
                        .setBundle(bundle)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
            }
        }, true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mInviteFirendAdapter);

        mGrabInvitePresenter = new GrabInvitePresenter(mBaseFragment, this);

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (mMode == UserInfoManager.RELATION.FRIENDS.getValue()) {
                    mGrabInvitePresenter.getFriendList(mOffset, DEFAULT_COUNT);
                } else if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
                    mGrabInvitePresenter.getFansList(mOffset, DEFAULT_COUNT);
                }
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mRefreshLayout.finishRefresh();
            }
        });

        LoadSir mLoadSir = new LoadSir.Builder()
                .addCallback(new FriendsEmptyCallback())
                .addCallback(new FansEmptyCallback())
                .build();
        mLoadService = mLoadSir.register(mRefreshLayout, new Callback.OnReloadListener() {
            @Override
            public void onReload(View v) {
                syncInviteMode();
            }
        });

        syncInviteMode();
    }

    private void syncInviteMode() {
        if (mMode == UserInfoManager.RELATION.FRIENDS.getValue()) {
            mGrabInvitePresenter.getFriendList(0, DEFAULT_COUNT);
        } else if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
            mGrabInvitePresenter.getFansList(0, DEFAULT_COUNT);
        }
    }

    @Override
    public void addInviteModelList(List<UserInfoModel> grabFriendModelList, int oldOffset, int newOffset) {
        if (mMode == UserInfoManager.RELATION.FRIENDS.getValue()) {
            mRefreshLayout.setEnableLoadMore(false);
            mLoadService.showSuccess();
            mInviteFirendAdapter.getDataList().clear();
            mInviteFirendAdapter.getDataList().addAll(grabFriendModelList);
            mInviteFirendAdapter.notifyDataSetChanged();
        } else if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
            this.mOffset = newOffset;
            if (grabFriendModelList != null && grabFriendModelList.size() > 0) {
                mHasMore = true;
                mRefreshLayout.setEnableLoadMore(true);
                mLoadService.showSuccess();
                if (oldOffset == 0) {
                    mInviteFirendAdapter.getDataList().clear();
                }
                mInviteFirendAdapter.getDataList().addAll(grabFriendModelList);
                mInviteFirendAdapter.notifyDataSetChanged();
            } else {
                mHasMore = false;
                mRefreshLayout.setEnableLoadMore(false);
                if (mInviteFirendAdapter.getDataList() == null || mInviteFirendAdapter.getDataList().size() == 0) {
                    // 没有数据
                    if (mMode == UserInfoManager.RELATION.FRIENDS.getValue()) {
                        mLoadService.showCallback(FriendsEmptyCallback.class);
                    } else if (mMode == UserInfoManager.RELATION.FANS.getValue()) {
                        mLoadService.showCallback(FansEmptyCallback.class);
                    }
                } else {
                    // 没有更多了
                }
            }
        }
    }

    @Override
    public void updateInvited(ExTextView view) {
        if (view != null) {
            view.setAlpha(0.5f);
            view.setText("已邀请");
            view.setClickable(false);
            view.setVisibility(VISIBLE);
        }
    }

    @Override
    public void finishRefresh() {
        mRefreshLayout.finishLoadMore();
    }
}
