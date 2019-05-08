package com.component.busilib.friends;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.core.avatar.AvatarUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.zq.live.proto.Common.ESex;

public class FriendRoomHorizontalViewHolder extends RecyclerView.ViewHolder {

    RecyclerOnItemClickListener<RecommendModel> mOnItemClickListener;

    SimpleDraweeView mAvatarIv;
    ExTextView mNicknameTv;
    ExTextView mFriendTv;
    ExTextView mRecommendTv;
    ExTextView mFollowTv;

    RecommendModel mFriendRoomModel;
    int position;

    public FriendRoomHorizontalViewHolder(View itemView) {
        super(itemView);

        mAvatarIv = (SimpleDraweeView) itemView.findViewById(R.id.avatar_iv);
        mNicknameTv = (ExTextView) itemView.findViewById(R.id.nickname_tv);
        mFriendTv = (ExTextView) itemView.findViewById(R.id.friend_tv);
        mRecommendTv = (ExTextView) itemView.findViewById(R.id.recommend_tv);
        mFollowTv = (ExTextView) itemView.findViewById(R.id.follow_tv);

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
            AvatarUtils.loadAvatarByUrl(mAvatarIv,
                    AvatarUtils.newParamsBuilder(mFriendRoomModel.getUserInfo().getAvatar())
                            .setCircle(true)
                            .build());

            if (mFriendRoomModel.getCategory() == RecommendModel.TYPE_RECOMMEND_ROOM) {
                mRecommendTv.setVisibility(View.VISIBLE);
                mFollowTv.setVisibility(View.GONE);
                mFriendTv.setVisibility(View.GONE);
                mNicknameTv.setText(mFriendRoomModel.getRoomInfo().getRoomName());
            } else if (mFriendRoomModel.getCategory() == RecommendModel.TYPE_FOLLOW_ROOM) {
                mRecommendTv.setVisibility(View.GONE);
                mFollowTv.setVisibility(View.VISIBLE);
                mFriendTv.setVisibility(View.GONE);
                mNicknameTv.setText(mFriendRoomModel.getUserInfo().getNickname());
            } else if (mFriendRoomModel.getCategory() == RecommendModel.TYPE_FRIEND_ROOM) {
                mRecommendTv.setVisibility(View.GONE);
                mFollowTv.setVisibility(View.GONE);
                mFriendTv.setVisibility(View.VISIBLE);
                mNicknameTv.setText(mFriendRoomModel.getUserInfo().getNickname());
            } else {
                mRecommendTv.setVisibility(View.GONE);
                mFollowTv.setVisibility(View.GONE);
                mFriendTv.setVisibility(View.GONE);
            }
        }
    }
}
