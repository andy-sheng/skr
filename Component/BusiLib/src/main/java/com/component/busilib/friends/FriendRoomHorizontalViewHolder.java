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
    ExImageView mOwnerIv;
    ExTextView mRecommendIv;

    RecommendModel mFriendRoomModel;
    int position;

    public FriendRoomHorizontalViewHolder(View itemView) {
        super(itemView);

        mAvatarIv = (SimpleDraweeView) itemView.findViewById(R.id.avatar_iv);
        mNicknameTv = (ExTextView) itemView.findViewById(R.id.nickname_tv);
        mOwnerIv = (ExImageView) itemView.findViewById(R.id.owner_iv);
        mRecommendIv = (ExTextView) itemView.findViewById(R.id.recommend_iv);


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

        AvatarUtils.loadAvatarByUrl(mAvatarIv,
                AvatarUtils.newParamsBuilder(friendRoomModel.getUserInfo().getAvatar())
                        .setCircle(true)
                        .build());
        mNicknameTv.setText(friendRoomModel.getUserInfo().getNickname());

        if (friendRoomModel.getCategory() == RecommendModel.TYPE_RECOMMEND_ROOM) {
            mRecommendIv.setVisibility(View.VISIBLE);
            mOwnerIv.setVisibility(View.GONE);
        } else {
            if (friendRoomModel.getRoomInfo().isIsOwner()) {
                mRecommendIv.setVisibility(View.GONE);
                mOwnerIv.setVisibility(View.VISIBLE);
            } else {
                mRecommendIv.setVisibility(View.GONE);
                mOwnerIv.setVisibility(View.GONE);
            }
        }
    }
}
