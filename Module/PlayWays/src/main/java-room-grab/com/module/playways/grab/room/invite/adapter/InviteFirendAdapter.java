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
import com.common.core.userinfo.model.UserInfoModel;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.recyclerview.DiffAdapter;
import com.dialog.view.StrokeTextView;
import com.module.playways.R;

import java.util.ArrayList;

public class InviteFirendAdapter extends DiffAdapter<UserInfoModel, RecyclerView.ViewHolder> {
    OnInviteClickListener mOnInviteClickListener;
    ArrayList<Integer> mInvitedList;
    boolean mHasSearch;

    private static final int ITEM_TYPE_NORMAL = 1;
    private static final int ITEM_TYPE_SEARCH = 2;

    public InviteFirendAdapter(OnInviteClickListener onInviteClickListener, boolean hasSearch) {
        this.mOnInviteClickListener = onInviteClickListener;
        this.mInvitedList = new ArrayList<>();
        this.mHasSearch = hasSearch;
    }

    public void addInvitedId(int useID) {
        if (mInvitedList != null) {
            mInvitedList.add(useID);
        }
        for (int i = 0; i < mDataList.size(); i++) {
            UserInfoModel userInfoModel = mDataList.get(i);
            if (userInfoModel.getUserId() == useID) {
                MyLog.d(TAG, "update" + " find i=" + i);
                if (mHasSearch) {
                    notifyItemChanged(i + 1);
                } else {
                    notifyItemChanged(i);
                }
                return;
            }
        }
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
                UserInfoModel model = mDataList.get(position - 1);
                ItemHolder itemHolder = (ItemHolder) holder;
                if (mInvitedList != null && mInvitedList.size() > 0 && mInvitedList.contains(model.getUserId())) {
                    itemHolder.bind(model, true);
                } else {
                    itemHolder.bind(model, false);
                }
            }
        } else {
            UserInfoModel model = mDataList.get(position);
            ItemHolder itemHolder = (ItemHolder) holder;
            if (mInvitedList != null && mInvitedList.size() > 0 && mInvitedList.contains(model.getUserId())) {
                itemHolder.bind(model, true);
            } else {
                itemHolder.bind(model, false);
            }
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

        UserInfoModel mGrabFriendModel;

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

        public void bind(UserInfoModel model, boolean hasInvited) {
            this.mGrabFriendModel = model;
            AvatarUtils.loadAvatarByUrl(mIvFriendIcon,
                    AvatarUtils.newParamsBuilder(model.getAvatar())
                            .setCircle(true)
                            .setBorderColorBySex(model.getIsMale())
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .build());

            mIvFriendName.setText(model.getNicknameRemark());

            if (model.getStatus() == UserInfoModel.EF_ONLINE_BUSY) {
                mTvState.setText("忙碌中");
                mTvInvite.setVisibility(View.GONE);
                mTvCircleState.setBackground(mBusyCircleDrawable);
            } else if (model.getStatus() == UserInfoModel.EF_ONLiNE_JOINED) {
                mTvState.setText("已加入游戏");
                mTvInvite.setVisibility(View.GONE);
                mTvCircleState.setBackground(mGameCircleDrawable);
            } else {
                if (model.getStatus() == UserInfoModel.EF_ONLINE) {
                    mTvCircleState.setBackground(mAIDLCircleDrawable);
                    mTvState.setText("在线");
                    mTvInvite.setVisibility(View.VISIBLE);
                } else if (model.getStatus() == UserInfoModel.EF_OFFLINE) {
                    mTvCircleState.setBackground(mOffLineCircleDrawable);
                    mTvState.setText("离线");
                    mTvInvite.setVisibility(View.VISIBLE);
                } else {
                    mTvInvite.setVisibility(View.GONE);
                }
            }

            if (hasInvited) {
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
        void onClick(UserInfoModel model);

        void onClickSearch();
    }
}