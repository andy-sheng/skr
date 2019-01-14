package com.zq.relation.view;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.zq.live.proto.Common.ESex;

public class RelationHolderView extends RecyclerView.ViewHolder {
    RelativeLayout mContent;
    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;
    ExTextView mUseridTv;
    ExTextView mFollowTv;

    int position;
    UserInfoModel userInfoModel;

    public RelationHolderView(View itemView, final RecyclerOnItemClickListener recyclerOnItemClickListener) {
        super(itemView);

        mContent = (RelativeLayout) itemView.findViewById(R.id.content);
        mAvatarIv = (SimpleDraweeView) itemView.findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) itemView.findViewById(R.id.name_tv);
        mUseridTv = (ExTextView) itemView.findViewById(R.id.userid_tv);
        mFollowTv = (ExTextView) itemView.findViewById(R.id.follow_tv);

        mFollowTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recyclerOnItemClickListener != null) {
                    recyclerOnItemClickListener.onItemClicked(mFollowTv, position, userInfoModel);
                }
            }
        });

        mContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recyclerOnItemClickListener != null) {
                    recyclerOnItemClickListener.onItemClicked(mContent, position, userInfoModel);
                }
            }
        });
    }

    public void bind(int mode, int position, UserInfoModel userInfoModel) {
        this.position = position;
        this.userInfoModel = userInfoModel;

        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(userInfoModel.getAvatar())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColorBySex(userInfoModel.getIsMale())
                        .build());
        mNameTv.setText(userInfoModel.getNickname());
        mUseridTv.setText("ID: " + String.valueOf(userInfoModel.getUserId()));
        if (mode == UserInfoManager.RELATION_FRIENDS) {
            mFollowTv.setText("互关");
            mFollowTv.setTextColor(Color.parseColor("#787B8E"));
            mFollowTv.setBackground(ContextCompat.getDrawable(U.app(), R.drawable.followed_bg));
        } else if (mode == UserInfoManager.RELATION_FANS) {
            if (userInfoModel.isFriend()) {
                mFollowTv.setText("互关");
                mFollowTv.setBackground(ContextCompat.getDrawable(U.app(), R.drawable.followed_bg));
            } else {
                mFollowTv.setText("关注");
                mFollowTv.setBackground(ContextCompat.getDrawable(U.app(), R.drawable.unfollow_bg));
            }
        } else if (mode == UserInfoManager.RELATION_FOLLOW) {
            if (userInfoModel.isFriend()) {
                mFollowTv.setText("互关");
            } else {
                mFollowTv.setText("已关注");
            }
            mFollowTv.setTextColor(Color.parseColor("#787B8E"));
            mFollowTv.setWidth(U.getDisplayUtils().dip2px(86));
            mFollowTv.setBackground(ContextCompat.getDrawable(U.app(), R.drawable.followed_bg));
        }
    }
}