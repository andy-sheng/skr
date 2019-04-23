package com.module.playways.grab.room.invite;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.recyclerview.DiffAdapter;
import com.module.playways.grab.room.model.GrabFriendModel;
import com.module.playways.R;

public class InviteFirendAdapter extends DiffAdapter<GrabFriendModel, RecyclerView.ViewHolder> {
    OnInviteClickListener mOnInviteClickListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_friend_item_layout, parent, false);
        ItemHolder viewHolder = new ItemHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GrabFriendModel model = mDataList.get(position);

        ItemHolder reportItemHolder = (ItemHolder) holder;
        reportItemHolder.bind(model);
    }

    public void setOnInviteClickListener(OnInviteClickListener onInviteClickListener) {
        mOnInviteClickListener = onInviteClickListener;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        BaseImageView mIvFriendIcon;
        ExTextView mIvFriendName;
        ExTextView mTvState;
        ExTextView mTvInvite;
        ExTextView mTvCircleState;

        GrabFriendModel mGrabFriendModel;

        Drawable mBusyCircleDrawable;
        Drawable mAIDLCircleDrawable;
        Drawable mGameCircleDrawable;
        Drawable mCanInviteBtnDrawable;
        Drawable mCannotInviteDrawable;

        public ItemHolder(View itemView) {
            super(itemView);
            mIvFriendIcon = (BaseImageView) itemView.findViewById(R.id.iv_friend_icon);
            mIvFriendName = (ExTextView) itemView.findViewById(R.id.iv_friend_name);
            mTvState = (ExTextView) itemView.findViewById(R.id.tv_state);
            mTvInvite = (ExTextView) itemView.findViewById(R.id.tv_invite);
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

            mGameCircleDrawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45))
                    .setSolidColor(Color.parseColor("#FF8C9A"))
                    .setCornersRadius(U.getDisplayUtils().dip2px(4))
                    .build();

            mCanInviteBtnDrawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45))
                    .setStrokeColor(Color.parseColor("#202239"))
                    .setStrokeWidth(U.getDisplayUtils().dip2px(2))
                    .setSolidColor(Color.parseColor("#484B63"))
                    .setCornersRadius(U.getDisplayUtils().dip2px(8))
                    .build();

            mCannotInviteDrawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45))
                    .setStrokeColor(Color.parseColor("#202239"))
                    .setStrokeWidth(U.getDisplayUtils().dip2px(2))
                    .setSolidColor(Color.parseColor("#D36060"))
                    .setCornersRadius(U.getDisplayUtils().dip2px(8))
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
                mTvState.setText("可邀请");
                mTvInvite.setVisibility(View.VISIBLE);
                mTvCircleState.setBackground(mAIDLCircleDrawable);
            } else if (model.getStatus() == 3) {
                mTvState.setText("已加入游戏");
                mTvInvite.setVisibility(View.GONE);
                mTvCircleState.setBackground(mGameCircleDrawable);
            }

            if (model.isInvited()) {
                mTvInvite.setBackground(mCanInviteBtnDrawable);
                mTvInvite.setTextColor(U.getColor(R.color.white_trans_70));
                mTvInvite.setEnabled(false);
                mTvInvite.setText("已邀请");
            } else {
                mTvInvite.setBackground(mCannotInviteDrawable);
                mTvInvite.setTextColor(U.getColor(R.color.white));
                mTvInvite.setEnabled(true);
                mTvInvite.setText("邀请");
            }
        }
    }

    interface OnInviteClickListener {
        void onClick(GrabFriendModel model);
    }
}