package com.component.busilib.friends;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.UserInfoManager;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.component.busilib.view.AvatarView;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;

public class FriendRoomHorizontalViewHolder extends RecyclerView.ViewHolder {

    RecyclerOnItemClickListener<RecommendModel> mOnItemClickListener;

    AvatarView mAvatarIv;
    ExTextView mNicknameTv;
    SimpleDraweeView mRecommendTagSdv;

    RecommendModel mFriendRoomModel;
    int position;

    public FriendRoomHorizontalViewHolder(View itemView) {
        super(itemView);

        mAvatarIv = itemView.findViewById(R.id.avatar_iv);
        mNicknameTv = itemView.findViewById(R.id.nickname_tv);
        mRecommendTagSdv = itemView.findViewById(R.id.recommend_tag_sdv);

        itemView.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClicked(v, position, mFriendRoomModel);
                }
            }
        });
    }

    public void setOnItemClickListener(RecyclerOnItemClickListener<RecommendModel> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


    public void bindData(RecommendModel friendRoomModel, int position) {
        this.mFriendRoomModel = friendRoomModel;
        this.position = position;

        if (mFriendRoomModel != null && mFriendRoomModel.getUserInfo() != null && mFriendRoomModel.getRoomInfo() != null) {
            
            mAvatarIv.bindData(mFriendRoomModel.getUserInfo());
            String disName = mFriendRoomModel.getUserInfo().getNickname();
            if (mFriendRoomModel.getUserInfo() != null) {
                disName = UserInfoManager.getInstance().getRemarkName(mFriendRoomModel.getUserInfo().getUserId(), disName);
            }
            mNicknameTv.setText(disName);
            if (!TextUtils.isEmpty(mFriendRoomModel.getRoomInfo().getRoomTagURL())) {
                mRecommendTagSdv.setVisibility(View.VISIBLE);
                FrescoWorker.loadImage(mRecommendTagSdv, ImageFactory.newPathImage(mFriendRoomModel.getRoomInfo().getRoomTagURL())
                        .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .build());
            } else {
                mRecommendTagSdv.setVisibility(View.GONE);
            }
        }
    }
}
