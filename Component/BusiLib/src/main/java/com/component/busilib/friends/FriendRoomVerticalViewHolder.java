package com.component.busilib.friends;

import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.SpanUtils;
import com.common.view.AnimateClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExRelativeLayout;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.R;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;

public class FriendRoomVerticalViewHolder extends RecyclerView.ViewHolder {

    public final static String TAG = "FriendRoomVerticalViewHolder";

    RecyclerOnItemClickListener<RecommendModel> mOnItemClickListener;

    RecommendModel mFriendRoomModel;
    int position;

    ExRelativeLayout mBackground;
    SimpleDraweeView mAvatarIv;
    ExTextView mNameTv;
    SimpleDraweeView mRecommendTagSdv;
    ExTextView mRoomInfoTv;
    ExImageView mEnterRoomIv;

    public FriendRoomVerticalViewHolder(View itemView) {
        super(itemView);


        mBackground = (ExRelativeLayout) itemView.findViewById(R.id.background);
        mAvatarIv = (SimpleDraweeView) itemView.findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) itemView.findViewById(R.id.name_tv);
        mRecommendTagSdv = (SimpleDraweeView) itemView.findViewById(R.id.recommend_tag_sdv);
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
                    AvatarUtils.newParamsBuilder(mFriendRoomModel.getDisplayAvatar())
                            .setCircle(true)
                            .build());
            mNameTv.setText(mFriendRoomModel.getDisplayName());
            if (!TextUtils.isEmpty(mFriendRoomModel.getDisplayURL())) {
                mRecommendTagSdv.setVisibility(View.VISIBLE);
                FrescoWorker.loadImage(mRecommendTagSdv, ImageFactory.newPathImage(mFriendRoomModel.getDisplayURL())
                        .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .build());
            } else {
                mRecommendTagSdv.setVisibility(View.GONE);
            }

            if (mFriendRoomModel.getTagInfo() != null) {
                SpannableStringBuilder stringBuilder = new SpanUtils()
                        .append(mFriendRoomModel.getRoomInfo().getInPlayersNum() + "/" + mFriendRoomModel.getRoomInfo().getTotalPlayersNum()).setBold().setFontSize(24, true)
                        .append("人  " + mFriendRoomModel.getTagInfo().getTagName())
                        .create();

                mRoomInfoTv.setText(stringBuilder);
            } else if (!TextUtils.isEmpty(mFriendRoomModel.getDisplayName())) {
                SpannableStringBuilder stringBuilder = new SpanUtils()
                        .append(mFriendRoomModel.getRoomInfo().getInPlayersNum() + "/" + mFriendRoomModel.getRoomInfo().getTotalPlayersNum()).setBold().setFontSize(24, true)
                        .append("人  " + mFriendRoomModel.getDisplayName())
                        .create();

                mRoomInfoTv.setText(stringBuilder);
            } else {
                MyLog.w(TAG, "服务器数据有问题" + " friendRoomModel=" + friendRoomModel + " position=" + position);
            }
            
        } else {
            MyLog.w(TAG, "bindData" + " friendRoomModel=" + friendRoomModel + " position=" + position);
        }

    }
}
