package com.zq.relation.view;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.core.CoreConfiguration;
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
import com.zq.live.proto.Common.ESex;

public class RelationHolderView extends RecyclerView.ViewHolder {
    RelativeLayout mContent;
    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;
    ImageView mSexIv;
    ExTextView mFollowTv;

    int mMode;
    int position;
    UserInfoModel userInfoModel;

    public RelationHolderView(View itemView, int mode, final RecyclerOnItemClickListener recyclerOnItemClickListener) {
        super(itemView);

        this.mMode = mode;
        mContent = (RelativeLayout) itemView.findViewById(R.id.content);
        mAvatarIv = (SimpleDraweeView) itemView.findViewById(R.id.avatar_iv);
        mSexIv = (ImageView) itemView.findViewById(R.id.sex_iv);
        mNameTv = (ExTextView) itemView.findViewById(R.id.name_tv);
        mFollowTv = (ExTextView) itemView.findViewById(R.id.follow_tv);

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
                        .setCircle(true)
                        .build());
        mNameTv.setText(userInfoModel.getNickname());
        mSexIv.setBackgroundResource(userInfoModel.getSex() == ESex.SX_MALE.getValue() ? R.drawable.sex_man_icon : R.drawable.sex_woman_icon);

        if (mMode == UserInfoManager.RELATION_BLACKLIST) {
            mFollowTv.setVisibility(View.VISIBLE);
            mFollowTv.setClickable(true);
            mFollowTv.setText("移出黑名单");
            mFollowTv.setTextColor(Color.parseColor("#3B4E79"));
            mFollowTv.setBackground(ContextCompat.getDrawable(U.app(), R.drawable.yellow_button_icon));
        } else {
            if (userInfoModel.getUserId() == MyUserInfoManager.getInstance().getUid()) {
                mFollowTv.setVisibility(View.GONE);
                return;
            } else {
                if (userInfoModel.isFriend()) {
                    mFollowTv.setVisibility(View.VISIBLE);
                    mFollowTv.setText("已互关");
                    mFollowTv.setClickable(false);
                    mFollowTv.setTextColor(Color.parseColor("#3B4E79"));
                    mFollowTv.setBackground(null);
                } else if (userInfoModel.isFollow()) {
                    mFollowTv.setVisibility(View.VISIBLE);
                    mFollowTv.setText("已关注");
                    mFollowTv.setClickable(false);
                    mFollowTv.setTextColor(Color.parseColor("#CC7F00"));
                    mFollowTv.setBackground(null);
                } else {
                    mFollowTv.setVisibility(View.VISIBLE);
                    mFollowTv.setText("+关注");
                    mFollowTv.setClickable(true);
                    mFollowTv.setTextColor(Color.parseColor("#3B4E79"));
                    mFollowTv.setBackground(ContextCompat.getDrawable(U.app(), R.drawable.yellow_button_icon));
                }
            }


        }
    }
}