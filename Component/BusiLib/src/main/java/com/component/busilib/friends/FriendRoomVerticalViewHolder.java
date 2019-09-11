package com.component.busilib.friends;

import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.UserInfoManager;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.ex.ExConstraintLayout;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.component.busilib.view.AvatarView;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;

public class FriendRoomVerticalViewHolder extends RecyclerView.ViewHolder {

    public final String TAG = "FriendRoomVerticalViewHolder";

    RecyclerOnItemClickListener<RecommendModel> mOnItemClickListener;

    RecommendModel mFriendRoomModel;
    int position;

    ExConstraintLayout mBackground;
    SimpleDraweeView mRecommendTagSdv;
    SimpleDraweeView mMediaTagSdv;
    AvatarView mAvatarIv;
    ExTextView mNameTv;
    ExTextView mRoomPlayerNumTv;
    ExTextView mRoomInfoTv;

    public FriendRoomVerticalViewHolder(View itemView) {
        super(itemView);

        mBackground = itemView.findViewById(R.id.background);
        mRecommendTagSdv = itemView.findViewById(R.id.recommend_tag_sdv);
        mMediaTagSdv = itemView.findViewById(R.id.media_tag_sdv);
        mAvatarIv = itemView.findViewById(R.id.avatar_iv);
        mNameTv = itemView.findViewById(R.id.name_tv);
        mRoomPlayerNumTv = itemView.findViewById(R.id.room_player_num_tv);
        mRoomInfoTv = itemView.findViewById(R.id.room_info_tv);

        itemView.setOnClickListener(new AnimateClickListener() {
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

        int colorIndex = position % 4;
        if (colorIndex == 1) {
            mBackground.setBackground(FriendRoomVerticalAdapter.mDrawable1);
        } else if (colorIndex == 2) {
            mBackground.setBackground(FriendRoomVerticalAdapter.mDrawable2);
        } else if (colorIndex == 3) {
            mBackground.setBackground(FriendRoomVerticalAdapter.mDrawable3);
        } else {
            mBackground.setBackground(FriendRoomVerticalAdapter.mDrawable4);
        }
        
        mAvatarIv.bindData(friendRoomModel.getUserInfo());

        if (mFriendRoomModel != null && mFriendRoomModel.getUserInfo() != null && mFriendRoomModel.getRoomInfo() != null) {
            if (!TextUtils.isEmpty(mFriendRoomModel.getRoomInfo().getRoomTagURL())) {
                mRecommendTagSdv.setVisibility(View.VISIBLE);
                FrescoWorker.loadImage(mRecommendTagSdv, ImageFactory.newPathImage(mFriendRoomModel.getRoomInfo().getRoomTagURL())
                        .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .build());
            } else {
                mRecommendTagSdv.setVisibility(View.GONE);
            }

            String nickName = UserInfoManager.getInstance().getRemarkName(mFriendRoomModel.getUserInfo().getUserId(), mFriendRoomModel.getUserInfo().getNickname());
            if (!TextUtils.isEmpty(nickName)) {
                mNameTv.setVisibility(View.VISIBLE);
                mNameTv.setText(nickName);
            } else {
                mNameTv.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(mFriendRoomModel.getRoomInfo().getMediaTagURL())) {
                mMediaTagSdv.setVisibility(View.VISIBLE);
                FrescoWorker.loadImage(mMediaTagSdv, ImageFactory.newPathImage(mFriendRoomModel.getRoomInfo().getMediaTagURL())
                        .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .build());
            } else {
                mMediaTagSdv.setVisibility(View.GONE);
            }

            mRoomPlayerNumTv.setText(mFriendRoomModel.getRoomInfo().getInPlayersNum() + "/" + mFriendRoomModel.getRoomInfo().getTotalPlayersNum());

            if (!TextUtils.isEmpty(mFriendRoomModel.getRoomInfo().getRoomName())) {
                mRoomInfoTv.setVisibility(View.VISIBLE);
                mRoomInfoTv.setText(mFriendRoomModel.getRoomInfo().getRoomName());
            } else {
                if (mFriendRoomModel.getTagInfo() != null) {
                    // 只显示专场名称
                    SpannableStringBuilder stringBuilder = new SpanUtils()
                            .append(mFriendRoomModel.getTagInfo().getTagName())
                            .create();
                    mRoomInfoTv.setVisibility(View.VISIBLE);
                    mRoomInfoTv.setText(stringBuilder);
                } else {
                    mRoomInfoTv.setVisibility(View.GONE);
                    MyLog.w(TAG, "服务器数据有问题" + " friendRoomModel=" + friendRoomModel + " position=" + position);
                }
            }
        } else {
            MyLog.w(TAG, "bindData" + " friendRoomModel=" + friendRoomModel + " position=" + position);
        }

    }
}
