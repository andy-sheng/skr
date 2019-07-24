package com.component.relation.view;

import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.component.live.proto.Common.ESex;
import com.component.relation.adapter.RelationAdapter;

public class RelationHolderView extends RecyclerView.ViewHolder {
    ConstraintLayout mContent;
    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;
    ImageView mSexIv;
    ExTextView mFollowTv;
    ExTextView mStatusTv;

    int mMode;
    int position;
    UserInfoModel userInfoModel;

    public RelationHolderView(View itemView, int mode, final RecyclerOnItemClickListener recyclerOnItemClickListener) {
        super(itemView);

        this.mMode = mode;
        mContent = itemView.findViewById(R.id.content);
        mAvatarIv = itemView.findViewById(R.id.avatar_iv);
        mSexIv = itemView.findViewById(R.id.sex_iv);
        mNameTv = itemView.findViewById(R.id.name_tv);
        mFollowTv = itemView.findViewById(R.id.follow_tv);
        mStatusTv = itemView.findViewById(R.id.status_tv);

        mFollowTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (recyclerOnItemClickListener != null) {
                    recyclerOnItemClickListener.onItemClicked(mFollowTv, position, userInfoModel);
                }
            }
        });

        mContent.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (recyclerOnItemClickListener != null) {
                    recyclerOnItemClickListener.onItemClicked(mContent, position, userInfoModel);
                }
            }
        });
    }

    public void bind(int position, UserInfoModel userInfoModel) {
        this.position = position;
        this.userInfoModel = userInfoModel;

        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(userInfoModel.getAvatar())
                        .setBorderColor(Color.WHITE)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2f))
                        .setCircle(true)
                        .build());
        mNameTv.setText(userInfoModel.getNicknameRemark());
        if (userInfoModel.getSex() == ESex.SX_MALE.getValue()) {
            mSexIv.setVisibility(View.VISIBLE);
            mSexIv.setBackgroundResource(R.drawable.sex_man_icon);
        } else if (userInfoModel.getSex() == ESex.SX_FEMALE.getValue()) {
            mSexIv.setVisibility(View.VISIBLE);
            mSexIv.setBackgroundResource(R.drawable.sex_woman_icon);
        } else {
            mSexIv.setVisibility(View.GONE);
        }

        if (mMode == UserInfoManager.RELATION_BLACKLIST) {
            mFollowTv.setVisibility(View.VISIBLE);
            mFollowTv.setClickable(true);
            mFollowTv.setText("移出");
            mFollowTv.setTextColor(Color.parseColor("#3B4E79"));
            mFollowTv.setBackground(RelationAdapter.mFollowDrawable);
        } else {
            if (userInfoModel.getUserId() == MyUserInfoManager.getInstance().getUid()) {
                mFollowTv.setVisibility(View.GONE);
                return;
            } else {
                if (mMode != UserInfoManager.RELATION.FRIENDS.getValue()) {
                    if (userInfoModel.isFriend()) {
                        mFollowTv.setVisibility(View.VISIBLE);
                        mFollowTv.setText("已互关");
                        mFollowTv.setClickable(false);
                        mFollowTv.setTextColor(Color.parseColor("#AD6C00"));
                        mFollowTv.setBackground(RelationAdapter.mFriendDrawable);
                    } else if (userInfoModel.isFollow()) {
                        mFollowTv.setVisibility(View.VISIBLE);
                        mFollowTv.setText("已关注");
                        mFollowTv.setClickable(false);
                        mFollowTv.setTextColor(Color.parseColor("#3B4E79"));
                        mFollowTv.setBackground(RelationAdapter.mFollowDrawable);
                    } else {
                        mFollowTv.setVisibility(View.VISIBLE);
                        mFollowTv.setText("+关注");
                        mFollowTv.setClickable(true);
                        mFollowTv.setTextColor(Color.parseColor("#AD6C00"));
                        mFollowTv.setBackground(RelationAdapter.mUnFollowDrawable);
                    }
                } else {
                    mFollowTv.setVisibility(View.GONE);
                }
            }
        }

        // 只是关心在线和离线
        if (mMode != UserInfoManager.RELATION.FANS.getValue()) {
            if (userInfoModel.getStatus() >= UserInfoModel.EF_ONLINE) {
                mStatusTv.setCompoundDrawablePadding(U.getDisplayUtils().dip2px(3));
                mStatusTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.greendot, 0, 0, 0);
                mStatusTv.setVisibility(View.VISIBLE);
                mStatusTv.setText(userInfoModel.getStatusDesc());
            } else if (userInfoModel.getStatus() == UserInfoModel.EF_OFFLINE) {
                mStatusTv.setCompoundDrawablePadding(U.getDisplayUtils().dip2px(3));
                mStatusTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.graydot, 0, 0, 0);
                mStatusTv.setVisibility(View.VISIBLE);
                mStatusTv.setText(userInfoModel.getStatusDesc());
            } else {
                mStatusTv.setVisibility(View.GONE);
            }
        } else {
            mStatusTv.setVisibility(View.GONE);
        }

    }
}