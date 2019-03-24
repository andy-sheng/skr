package com.module.playways.grab.room.invite;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.core.kouling.SkrKouLingUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.inter.IGrabInviteView;

import com.module.playways.grab.room.model.GrabFriendModel;

import com.module.rank.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.List;

public class InviteFriendFragment extends BaseFragment implements IGrabInviteView {
    public final static String TAG = "InviteFriendFragment";

    GrabRoomData mRoomData;
    SmartRefreshLayout mRefreshLayout;
    RecyclerView mRecyclerView;
    ExTextView mTvShare;
    ImageView mIvClose;
    TextView mTvWeixinShare;
    TextView mTvQqShare;

    View mEmptyView;
    DialogPlus mShareDialog;

    InviteFirendAdapter mInviteFirendAdapter;

    GrabInvitePresenter mGrabInvitePresenter;

    @Override
    public int initView() {
        return R.layout.invite_friend_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mGrabInvitePresenter = new GrabInvitePresenter(this, mRoomData);
        addPresent(mGrabInvitePresenter);

        mEmptyView = (View)mRootView.findViewById(R.id.empty_view);
        mRefreshLayout = (SmartRefreshLayout) mRootView.findViewById(R.id.refreshLayout);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        mTvShare = (ExTextView) mRootView.findViewById(R.id.tv_share);
        mIvClose = (ImageView) mRootView.findViewById(R.id.iv_close);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mInviteFirendAdapter = new InviteFirendAdapter();
        mRecyclerView.setAdapter(mInviteFirendAdapter);
        mInviteFirendAdapter.setOnInviteClickListener(new InviteFirendAdapter.OnInviteClickListener() {
            @Override
            public void onClick(GrabFriendModel model) {
                mGrabInvitePresenter.inviteFriend(mRoomData.getGameId(), model);
            }
        });

        mRefreshLayout.setEnableRefresh(false);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.setEnableOverScrollDrag(false);
        mRefreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                mGrabInvitePresenter.getFriendList();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }
        });

        initListener();
        mGrabInvitePresenter.getFriendList();
    }

    private void initListener() {
        mIvClose.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                finish();
            }
        });

        mTvShare.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                showShareDialog();
            }
        });

        mEmptyView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                finish();
            }
        });
    }

    private void showShareDialog() {
        if (mShareDialog == null) {
            mShareDialog = DialogPlus.newDialog(getContext())
                    .setContentHolder(new ViewHolder(R.layout.invite_friend_panel))
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_50)
                    .setExpanded(false)
                    .setGravity(Gravity.BOTTOM)
                    .create();

            mTvWeixinShare = (TextView) mShareDialog.findViewById(R.id.tv_weixin_share);
            mTvQqShare = (TextView) mShareDialog.findViewById(R.id.tv_qq_share);
            mTvWeixinShare.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    String kouling = SkrKouLingUtils.genJoinGameKouling((int) MyUserInfoManager.getInstance().getUid(), mRoomData.getGameId());
                    new ShareAction(getActivity()).withText(kouling)
                            .setPlatform(SHARE_MEDIA.WEIXIN)
                            .share();
                }
            });

            mTvQqShare.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    String kouling = SkrKouLingUtils.genJoinGameKouling((int) MyUserInfoManager.getInstance().getUid(), mRoomData.getGameId());
                    new ShareAction(getActivity()).withText(kouling)
                            .setPlatform(SHARE_MEDIA.QQ)
                            .share();
                }
            });
        }

        if (!mShareDialog.isShowing()) {
            mShareDialog.show();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mShareDialog != null) {
            mShareDialog.dismiss();
        }
    }

    @Override
    public void updateFriendList(List<GrabFriendModel> grabFriendModelList) {
        mInviteFirendAdapter.setDataList(grabFriendModelList);
    }

    @Override
    public void hasMore(boolean hasMore) {
        mRefreshLayout.setEnableLoadMore(true);
    }

    @Override
    public void finishRefresh() {
        mRefreshLayout.finishRefresh();
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mRoomData = (GrabRoomData) data;
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
