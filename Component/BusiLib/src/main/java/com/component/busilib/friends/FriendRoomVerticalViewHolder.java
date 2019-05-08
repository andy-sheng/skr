package com.component.busilib.friends;

import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.log.MyLog;
import com.common.utils.SpanUtils;
import com.common.view.AnimateClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.facebook.drawee.view.SimpleDraweeView;

public class FriendRoomVerticalViewHolder extends RecyclerView.ViewHolder {

    public final static String TAG = "FriendRoomVerticalViewHolder";

    RecyclerOnItemClickListener<RecommendModel> mOnItemClickListener;

    RecommendModel mFriendRoomModel;
    int position;

    RelativeLayout mBackground;
    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;
    ExTextView mFriendTv;
    ExTextView mRecommendTv;
    ExTextView mFollowTv;
    ExTextView mRoomInfoTv;
    ExImageView mEnterRoomIv;

    public FriendRoomVerticalViewHolder(View itemView) {
        super(itemView);


        mBackground = (RelativeLayout) itemView.findViewById(R.id.background);
        mAvatarIv = (SimpleDraweeView) itemView.findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) itemView.findViewById(R.id.name_tv);
        mFriendTv = (ExTextView) itemView.findViewById(R.id.friend_tv);
        mRecommendTv = (ExTextView) itemView.findViewById(R.id.recommend_tv);
        mFollowTv = (ExTextView) itemView.findViewById(R.id.follow_tv);
        mRoomInfoTv = (ExTextView) itemView.findViewById(R.id.room_info_tv);
        mEnterRoomIv = (ExImageView) itemView.findViewById(R.id.enter_room_iv);

        mEnterRoomIv.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClicked(view, position, mFriendRoomModel);
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
                mNameTv.setText(mFriendRoomModel.getRoomInfo().getRoomName());
            } else if (mFriendRoomModel.getCategory() == RecommendModel.TYPE_FOLLOW_ROOM) {
                mRecommendTv.setVisibility(View.GONE);
                mFollowTv.setVisibility(View.VISIBLE);
                mFriendTv.setVisibility(View.GONE);
                mNameTv.setText(mFriendRoomModel.getUserInfo().getNickname());
            } else if (mFriendRoomModel.getCategory() == RecommendModel.TYPE_FRIEND_ROOM) {
                mRecommendTv.setVisibility(View.GONE);
                mFollowTv.setVisibility(View.GONE);
                mFriendTv.setVisibility(View.VISIBLE);
                mNameTv.setText(mFriendRoomModel.getUserInfo().getNickname());
            } else {
                mRecommendTv.setVisibility(View.GONE);
                mFollowTv.setVisibility(View.GONE);
                mFriendTv.setVisibility(View.GONE);
            }

            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(mFriendRoomModel.getRoomInfo().getInPlayersNum() + "/" + mFriendRoomModel.getRoomInfo().getTotalPlayersNum()).setBold().setFontSize(24, true)
                    .append("人  " + mFriendRoomModel.getTagInfo().getTagName())
                    .create();

            mRoomInfoTv.setText(stringBuilder);
        } else {
            MyLog.w(TAG, "bindData" + " friendRoomModel=" + friendRoomModel + " position=" + position);
        }

    }
}
