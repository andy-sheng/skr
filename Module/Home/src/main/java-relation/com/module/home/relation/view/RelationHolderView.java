package com.module.home.relation.view;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoModel;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.home.R;

public class RelationHolderView extends RecyclerView.ViewHolder {

    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;
    ExTextView mUseridTv;
    ExTextView mFollowTv;


    public RelationHolderView(View itemView) {
        super(itemView);

        mAvatarIv = (SimpleDraweeView) itemView.findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) itemView.findViewById(R.id.name_tv);
        mUseridTv = (ExTextView) itemView.findViewById(R.id.userid_tv);
        mFollowTv = (ExTextView) itemView.findViewById(R.id.follow_tv);
    }

    public void bind(int mode, UserInfoModel userInfoModel) {
        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(userInfoModel.getAvatar())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setBorderColor(Color.parseColor("#FF79A9"))
                        .build());
        mNameTv.setText(userInfoModel.getNickname());
        mUseridTv.setText("ID: " + String.valueOf(userInfoModel.getUserId()));
        if (mode == UserInfoManager.RELATION_FRIENDS) {
            mFollowTv.setText("互相关注");
        } else if (mode == UserInfoManager.RELATION_FANS) {
            mFollowTv.setText("未关注");
        } else if (mode == UserInfoManager.RELATION_FOLLOW) {
            mFollowTv.setText("已关注");
            mFollowTv.setWidth(U.getDisplayUtils().dip2px(86));
        }
    }
}