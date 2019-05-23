package com.module.playways.grab.room.invite.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.recyclerview.DiffAdapter;
import com.dialog.view.StrokeTextView;
import com.module.playways.grab.room.invite.model.GrabFriendModel;
import com.module.playways.R;

public class InviteFirendAdapter extends DiffAdapter<GrabFriendModel, RecyclerView.ViewHolder> {
    OnInviteClickListener mOnInviteClickListener;
    boolean mHasSearch = false;

    private static final int ITEM_TYPE_NORMAL = 1;
    private static final int ITEM_TYPE_SEARCH = 2;

    public InviteFirendAdapter(OnInviteClickListener onInviteClickListener, boolean hasSearch) {
        this.mOnInviteClickListener = onInviteClickListener;
        this.mHasSearch = hasSearch;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_SEARCH) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_friend_item_layout, parent, false);
            SearchItmeHolder viewHolder = new SearchItmeHolder(view);
            return viewHolder;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_friend_item_layout, parent, false);
            ItemHolder viewHolder = new ItemHolder(view);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (mHasSearch) {
            if (position == 0) {

            } else {
                GrabFriendModel model = mDataList.get(position - 1);
                ItemHolder reportItemHolder = (ItemHolder) holder;
                reportItemHolder.bind(model);
            }
        } else {
            GrabFriendModel model = mDataList.get(position);
            ItemHolder reportItemHolder = (ItemHolder) holder;
            reportItemHolder.bind(model);
        }
    }

    @Override
    public int getItemCount() {
        if (mHasSearch) {
            return mDataList.size() + 1;
        }
        return mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mHasSearch && position == 0) {
            return ITEM_TYPE_SEARCH;
        }
        return ITEM_TYPE_NORMAL;
    }

    private class SearchItmeHolder extends RecyclerView.ViewHolder {

        RelativeLayout mContent;

        public SearchItmeHolder(View itemView) {
            super(itemView);
            mContent = (RelativeLayout) itemView.findViewById(R.id.content);

            mContent.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    if (mOnInviteClickListener != null) {
                        mOnInviteClickListener.onClickSearch();
                    }
                }
            });
        }
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        BaseImageView mIvFriendIcon;
        ExTextView mIvFriendName;
        ExTextView mTvState;
        StrokeTextView mTvInvite;
        ExTextView mTvCircleState;

        GrabFriendModel mGrabFriendModel;

        Drawable mBusyCircleDrawable;
        Drawable mAIDLCircleDrawable;
        Drawable mOffLineCircleDrawable;
        Drawable mGameCircleDrawable;

        public ItemHolder(View itemView) {
            super(itemView);
            mIvFriendIcon = (BaseImageView) itemView.findViewById(R.id.iv_friend_icon);
            mIvFriendName = (ExTextView) itemView.findViewById(R.id.iv_friend_name);
            mTvState = (ExTextView) itemView.findViewById(R.id.tv_state);
            mTvInvite = (StrokeTextView) itemView.findViewById(R.id.tv_invite);
            mTvCircleState = (ExTextView) itemView.findViewById(R.id.tv_circle_state);

            mTvInvite.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    if (mOnInviteClickListener != null) {
                        mOnInviteClickListener.onClick(mGrabFriendModel);
                    }
                }
            });

            mBusyCircleDrawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45))
                    .setSolidColor(Color.parseColor("#FFC300"))
                    .setCornersRadius(U.getDisplayUtils().dip2px(4))
                    .build();

            mAIDLCircleDrawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45))
                    .setSolidColor(Color.parseColor("#7ED321"))
                    .setCornersRadius(U.getDisplayUtils().dip2px(4))
                    .build();

            mOffLineCircleDrawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45))
                    .setSolidColor(Color.parseColor("#8EA0A9"))
                    .setCornersRadius(U.getDisplayUtils().dip2px(4))
                    .build();

            mGameCircleDrawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45))
                    .setSolidColor(Color.parseColor("#FF8C9A"))
                    .setCornersRadius(U.getDisplayUtils().dip2px(4))
                    .build();

        }

        public void bind(GrabFriendModel model) {
            this.mGrabFriendModel = model;
            AvatarUtils.loadAvatarByUrl(mIvFriendIcon,
                    AvatarUtils.newParamsBuilder(model.getAvatar())
                            .setCircle(true)
                            .setBorderColorBySex(model.getIsMale())
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .build());

            mIvFriendName.setText(model.getNickName());

            if (model.getStatus() == 2) {
                mTvState.setText("忙碌中");
                mTvInvite.setVisibility(View.GONE);
                mTvCircleState.setBackground(mBusyCircleDrawable);
            } else if (model.getStatus() == 1) {
                if (model.isIsOnline()) {
                    mTvCircleState.setBackground(mAIDLCircleDrawable);
                    mTvState.setText("在线");
                } else {
                    mTvCircleState.setBackground(mOffLineCircleDrawable);
                    mTvState.setText("离线");
                }

                mTvInvite.setVisibility(View.VISIBLE);

            } else if (model.getStatus() == 3) {
                mTvState.setText("已加入游戏");
                mTvInvite.setVisibility(View.GONE);
                mTvCircleState.setBackground(mGameCircleDrawable);
            }

            if (model.isInvited()) {
                mTvInvite.setAlpha(0.5f);
                mTvInvite.setEnabled(false);
                mTvInvite.setText("已邀请");
            } else {
                mTvInvite.setAlpha(1f);
                mTvInvite.setEnabled(true);
                mTvInvite.setText("邀请");
            }
        }
    }

    public interface OnInviteClickListener {
        void onClick(GrabFriendModel model);

        void onClickSearch();
    }
}