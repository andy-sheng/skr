package com.module.playways.grab.songselect.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.grab.songselect.model.FriendRoomModel;
import com.module.rank.R;
import com.zq.live.proto.Common.ESex;

public class FriendRoomViewHolder extends RecyclerView.ViewHolder {

    SimpleDraweeView mAvatarIv;
    ExTextView mNicknameTv;
    ExImageView mOwnerIv;

    FriendRoomModel mFriendRoomModel;

    public FriendRoomViewHolder(View itemView) {
        super(itemView);

        mAvatarIv = (SimpleDraweeView) itemView.findViewById(R.id.avatar_iv);
        mNicknameTv = (ExTextView) itemView.findViewById(R.id.nickname_tv);
        mOwnerIv = (ExImageView) itemView.findViewById(R.id.owner_iv);

    }

    public void bindData(FriendRoomModel friendRoomModel, int position) {
        this.mFriendRoomModel = friendRoomModel;

        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(friendRoomModel.getAvatar())
                        .setBorderColorBySex(MyUserInfoManager.getInstance().getSex() == ESex.SX_MALE.getValue())
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setCircle(true)
                        .build());
        mNicknameTv.setText(friendRoomModel.getName());

    }
}
