package com.component.busilib.friends;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.UserInfoManager;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.utils.VideoUtils;
import com.common.view.AnimateClickListener;
import com.common.view.ex.ExConstraintLayout;
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

    SimpleDraweeView mIv1;
    SimpleDraweeView mIv2;
    SimpleDraweeView mIv3;
    ImageView mMoreUserIv;
    ExTextView mRoomPlayerNumTv;
    SimpleDraweeView mRecommendTagSdv;
    ExTextView mRoomInfoTv;
    ExTextView mTipsTv;
    SimpleDraweeView mMediaTagSdv;

    SimpleDraweeView[] mSimpleDraweeViewList;

    public FriendRoomVerticalViewHolder(View itemView) {
        super(itemView);

        mIv1 = itemView.findViewById(R.id.iv_1);
        mIv2 = itemView.findViewById(R.id.iv_2);
        mIv3 = itemView.findViewById(R.id.iv_3);
        mMoreUserIv = itemView.findViewById(R.id.more_user_iv);
        mRoomPlayerNumTv = itemView.findViewById(R.id.room_player_num_tv);
        mRecommendTagSdv = itemView.findViewById(R.id.recommend_tag_sdv);
        mRoomInfoTv = itemView.findViewById(R.id.room_info_tv);
        mTipsTv = itemView.findViewById(R.id.tips_tv);
        mMediaTagSdv = itemView.findViewById(R.id.media_tag_sdv);

        mSimpleDraweeViewList = new SimpleDraweeView[]{mIv1, mIv2, mIv3};

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
                    AvatarUtils.newParamsBuilder(friendRoomModel.getPlayUsers().get(i).getAvatar())
                            .setCircle(true)
                            .setGray(false)
                            .setBorderWidth(U.getDisplayUtils().dip2px(1.5f))
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
            if (!TextUtils.isEmpty(mFriendRoomModel.getDisplayURL())) {
                mRecommendTagSdv.setVisibility(View.VISIBLE);
                FrescoWorker.loadImage(mRecommendTagSdv, ImageFactory.newPathImage(mFriendRoomModel.getDisplayURL())
                        .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .build());
            } else {
                mRecommendTagSdv.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(mFriendRoomModel.getMediaTagURL())) {
                mMediaTagSdv.setVisibility(View.VISIBLE);
                FrescoWorker.loadImage(mMediaTagSdv, ImageFactory.newPathImage(mFriendRoomModel.getMediaTagURL())
                        .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .build());
            } else {
                mMediaTagSdv.setVisibility(View.GONE);
            }

            if (mFriendRoomModel.getRoomInfo().getInPlayersNum() > 3) {
                mMoreUserIv.setVisibility(View.VISIBLE);
            } else {
                mMoreUserIv.setVisibility(View.GONE);
            }

            mRoomPlayerNumTv.setText(mFriendRoomModel.getRoomInfo().getInPlayersNum() + "/" + mFriendRoomModel.getRoomInfo().getTotalPlayersNum());

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

            if (!TextUtils.isEmpty(mFriendRoomModel.getDisplayDesc()) && !TextUtils.isEmpty(mFriendRoomModel.getUserInfo().getNickname())) {
                String remark = UserInfoManager.getInstance().getRemarkName(mFriendRoomModel.getUserInfo().getUserId(), mFriendRoomModel.getUserInfo().getNickname());
                if (!TextUtils.isEmpty(remark) && remark.length() > 7) {
                    remark = remark.substring(0, 7) + "...";
                }
                SpannableStringBuilder stringBuilder = new SpanUtils()
                        .append(remark).setForegroundColor(Color.parseColor("#7088FF"))
                        .append(mFriendRoomModel.getDisplayDesc()).setForegroundColor(U.getColor(R.color.textColorPrimary))
                        .create();
                mTipsTv.setVisibility(View.VISIBLE);
                mTipsTv.setText(stringBuilder);
            } else {
                mTipsTv.setVisibility(View.GONE);
                MyLog.w(TAG, "bindData" + " 服务器bug 啥都没有让我显示什么");
            }
        } else {
            MyLog.w(TAG, "bindData" + " friendRoomModel=" + friendRoomModel + " position=" + position);
        }

    }
}
