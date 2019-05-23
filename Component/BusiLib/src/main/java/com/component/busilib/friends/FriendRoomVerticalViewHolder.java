package com.component.busilib.friends;

import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
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
    ExTextView mRoomPlayerNumTv;
    SimpleDraweeView mIv1;
    SimpleDraweeView mIv2;
    SimpleDraweeView mIv3;
    SimpleDraweeView mIv4;
    SimpleDraweeView mIv5;
    SimpleDraweeView mIv6;

    SimpleDraweeView[] mSimpleDraweeViewList;

//    ExImageView mEnterRoomIv;

    public FriendRoomVerticalViewHolder(View itemView) {
        super(itemView);

        mBackground = (ExRelativeLayout) itemView.findViewById(R.id.background);
        mAvatarIv = (SimpleDraweeView) itemView.findViewById(R.id.avatar_iv);
        mNameTv = (ExTextView) itemView.findViewById(R.id.name_tv);
        mRecommendTagSdv = (SimpleDraweeView) itemView.findViewById(R.id.recommend_tag_sdv);
        mRoomInfoTv = (ExTextView) itemView.findViewById(R.id.room_info_tv);
        mRoomPlayerNumTv = (ExTextView) itemView.findViewById(R.id.room_player_num_tv);
        mIv1 = (SimpleDraweeView) itemView.findViewById(R.id.iv_1);
        mIv2 = (SimpleDraweeView) itemView.findViewById(R.id.iv_2);
        mIv3 = (SimpleDraweeView) itemView.findViewById(R.id.iv_3);
        mIv4 = (SimpleDraweeView) itemView.findViewById(R.id.iv_4);
        mIv5 = (SimpleDraweeView) itemView.findViewById(R.id.iv_5);
        mIv6 = (SimpleDraweeView) itemView.findViewById(R.id.iv_6);
        mSimpleDraweeViewList = new SimpleDraweeView[]{mIv1, mIv2, mIv3, mIv4, mIv5, mIv6};

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

    private void resetPlayerIcon() {
        for (SimpleDraweeView simpleDraweeView : mSimpleDraweeViewList) {
            simpleDraweeView.setVisibility(View.GONE);
        }
    }

    void setPlayerUserList(RecommendModel friendRoomModel) {
        if (friendRoomModel.getPlayUsers() == null || friendRoomModel.getPlayUsers().size() == 0) {
            return;
        }

        for (int i = 0; i < friendRoomModel.getPlayUsers().size() && i < mSimpleDraweeViewList.length; i++) {
            mSimpleDraweeViewList[i].setVisibility(View.VISIBLE);
            AvatarUtils.loadAvatarByUrl(mSimpleDraweeViewList[i],
                    AvatarUtils.newParamsBuilder(friendRoomModel.getPlayUsers().get(i).getUserInfo().getAvatar())
                            .setCircle(true)
                            .setGray(false)
                            .setBorderWidth(U.getDisplayUtils().dip2px(1))
                            .setBorderColor(U.getColor(R.color.white))
                            .build());
        }
    }

    public void bindData(RecommendModel friendRoomModel, int position) {
        this.mFriendRoomModel = friendRoomModel;
        this.position = position;
        resetPlayerIcon();
        setPlayerUserList(friendRoomModel);

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

            mRoomPlayerNumTv.setText(mFriendRoomModel.getRoomInfo().getInPlayersNum() + "/" + mFriendRoomModel.getRoomInfo().getTotalPlayersNum());

            if (mFriendRoomModel.getTagInfo() != null) {
                SpannableStringBuilder stringBuilder = new SpanUtils()
                        .append(mFriendRoomModel.getTagInfo().getTagName())
                        .create();

                mRoomInfoTv.setText(stringBuilder);
            } else if (!TextUtils.isEmpty(mFriendRoomModel.getDisplayName())) {
                SpannableStringBuilder stringBuilder = new SpanUtils()
                        .append(mFriendRoomModel.getDisplayName())
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
