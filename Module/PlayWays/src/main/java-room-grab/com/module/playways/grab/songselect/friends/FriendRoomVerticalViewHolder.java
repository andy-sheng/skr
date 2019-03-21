package com.module.playways.grab.songselect.friends;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.rank.R;
import com.zq.live.proto.Common.ESex;

public class FriendRoomVerticalViewHolder extends RecyclerView.ViewHolder {

    RecyclerOnItemClickListener<FriendRoomModel> mOnItemClickListener;

    FriendRoomModel mFriendRoomModel;
    int position;

    RelativeLayout mBackground;
    SimpleDraweeView mAvatarIv;
    ExTextView mEnterRoom;
    ExTextView mNameTv;
    ExTextView mOwnerTv;
    ExTextView mRoomNumTv;
    ExTextView mTagNameTv;

    public FriendRoomVerticalViewHolder(View itemView) {
        super(itemView);

        mBackground = (RelativeLayout) itemView.findViewById(R.id.background);
        mAvatarIv = (SimpleDraweeView) itemView.findViewById(R.id.avatar_iv);
        mEnterRoom = (ExTextView) itemView.findViewById(R.id.enter_room);
        mNameTv = (ExTextView) itemView.findViewById(R.id.name_tv);
        mOwnerTv = (ExTextView) itemView.findViewById(R.id.owner_tv);
        mRoomNumTv = (ExTextView) itemView.findViewById(R.id.room_num_tv);
        mTagNameTv = (ExTextView) itemView.findViewById(R.id.tag_name_tv);

        mEnterRoom.setOnClickListener(new DebounceViewClickListener() {
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

        if (friendRoomModel.getPlayTag() != null && !TextUtils.isEmpty(friendRoomModel.getPlayTag().getBgColor())) {
            mBackground.setBackground(getShapeDrawable(Color.parseColor(friendRoomModel.getPlayTag().getBgColor())));
        } else {
            mBackground.setBackground(getShapeDrawable(Color.parseColor("#68ABD3")));
        }

        if (friendRoomModel.getPlayTag() != null) {
            mTagNameTv.setText(friendRoomModel.getPlayTag().getTagName());
        }

        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(friendRoomModel.getInfo().getAvatar())
                        .setBorderColorBySex(friendRoomModel.getInfo().getSex() == ESex.SX_MALE.getValue())
                        .setBorderWidth(U.getDisplayUtils().dip2px(2))
                        .setCircle(true)
                        .build());

        mNameTv.setText(friendRoomModel.getInfo().getNickname());
        mRoomNumTv.setText(friendRoomModel.getCurrNum() + "/" + friendRoomModel.getPlaysNum());
        if (friendRoomModel.isIsOwner()) {
            mOwnerTv.setVisibility(View.VISIBLE);
        }
    }

    public Drawable getShapeDrawable(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(U.getDisplayUtils().dip2px(10));
        drawable.setColor(color);
        return drawable;
    }
}
