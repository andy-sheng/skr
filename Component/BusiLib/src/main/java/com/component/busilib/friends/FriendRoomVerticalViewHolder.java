package com.component.busilib.friends;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
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
    ExImageView mOwnerIv;
    ExTextView mNameTv;
    ExTextView mFriendTv;
    ExTextView mRecommendTv;
    ExTextView mRoomSongTv;
    ExTextView mRoomInfoTv;
    ExRelativeLayout mEnterRoomArea;

    public FriendRoomVerticalViewHolder(View itemView) {
        super(itemView);


        mBackground = (RelativeLayout) itemView.findViewById(R.id.background);
        mAvatarIv = (SimpleDraweeView) itemView.findViewById(R.id.avatar_iv);
        mOwnerIv = (ExImageView) itemView.findViewById(R.id.owner_iv);
        mNameTv = (ExTextView) itemView.findViewById(R.id.name_tv);
        mFriendTv = (ExTextView) itemView.findViewById(R.id.friend_tv);
        mRecommendTv = (ExTextView) itemView.findViewById(R.id.recommend_tv);
        mRoomSongTv = (ExTextView) itemView.findViewById(R.id.room_song_tv);
        mRoomInfoTv = (ExTextView) itemView.findViewById(R.id.room_info_tv);
        mEnterRoomArea = (ExRelativeLayout) itemView.findViewById(R.id.enter_room_area);

        mEnterRoomArea.setOnClickListener(new AnimateClickListener() {
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
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2))
                            .setCircle(true)
                            .build());

            mNameTv.setText(mFriendRoomModel.getUserInfo().getNickname());
            if (friendRoomModel.getCategory() == RecommendModel.TYPE_FRIEND_ROOM) {
                mFriendTv.setVisibility(View.VISIBLE);
                mRecommendTv.setVisibility(View.GONE);
            } else {
                mFriendTv.setVisibility(View.GONE);
                mRecommendTv.setVisibility(View.VISIBLE);
            }
            mRoomSongTv.setText(mFriendRoomModel.getRoomInfo().getCurrentRoundSeq() + " / " + mFriendRoomModel.getRoomInfo().getTotalGameRoundSeq());
            mRoomInfoTv.setText(mFriendRoomModel.getTagInfo().getTagName() + " / " + mFriendRoomModel.getRoomInfo().getInPlayersNum());
        } else {
            MyLog.w(TAG, "bindData" + " friendRoomModel=" + friendRoomModel + " position=" + position);
        }

    }
}
