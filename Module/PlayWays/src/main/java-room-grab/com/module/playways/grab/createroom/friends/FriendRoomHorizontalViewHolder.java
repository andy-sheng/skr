package com.module.playways.grab.createroom.friends;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.core.avatar.AvatarUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.rank.R;
import com.zq.live.proto.Common.ESex;

public class FriendRoomHorizontalViewHolder extends RecyclerView.ViewHolder {

    RecyclerOnItemClickListener<FriendRoomModel> mOnItemClickListener;

    SimpleDraweeView mAvatarIv;
    ExTextView mNicknameTv;
    ExImageView mOwnerIv;

    FriendRoomModel mFriendRoomModel;
    int position;

    public FriendRoomHorizontalViewHolder(View itemView) {
        super(itemView);

        mAvatarIv = (SimpleDraweeView) itemView.findViewById(R.id.avatar_iv);
        mNicknameTv = (ExTextView) itemView.findViewById(R.id.nickname_tv);
        mOwnerIv = (ExImageView) itemView.findViewById(R.id.owner_iv);

        itemView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClicked(v, position, mFriendRoomModel);
                }
            }
        });
    }

    public void setOnItemClickListener(RecyclerOnItemClickListener<FriendRoomModel> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


    public void bindData(FriendRoomModel friendRoomModel, int position) {
        this.mFriendRoomModel = friendRoomModel;
        this.position = position;

        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(friendRoomModel.getUserInfo().getAvatar())
                        .setBorderColorBySex(friendRoomModel.getUserInfo().getSex() == ESex.SX_MALE.getValue())
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setCircle(true)
                        .build());
        mNicknameTv.setText(friendRoomModel.getUserInfo().getNickname());

    }
}
