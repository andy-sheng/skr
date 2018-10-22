package com.wali.live.modulechannel.adapter.holder;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;

import java.util.Arrays;

public class TwoLongCoverHolder extends RepeatHolder {
    private int[] mLeftLabelIds;
    private int[] mNameIds;
    private int[] mLikeCountIds;
    private int[] mLeftLabelIvIds;

    private TextView[] mLeftLabelTvs;
    private TextView[] mNameTvs;
    private TextView[] mLikeCountTvs;

    public TwoLongCoverHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentViewId() {
        mParentIds = new int[]{
                R.id.single_card_1,
                R.id.single_card_2
        };
        mViewSize = mParentIds.length;
        mIvIds = new int[mViewSize];
        Arrays.fill(mIvIds, R.id.cover_img);
        mTvIds = new int[mViewSize];
        Arrays.fill(mTvIds, R.id.single_tv);
        mLeftLabelIds = new int[mViewSize];
        Arrays.fill(mLeftLabelIds, R.id.left_label_tv);
        mNameIds = new int[mViewSize];
        Arrays.fill(mNameIds, R.id.name_tv);
        mLikeCountIds = new int[mViewSize];
        Arrays.fill(mLikeCountIds, R.id.like_count);
        mLeftLabelIvIds = new int[mViewSize];
        Arrays.fill(mLeftLabelIvIds, R.id.left_label_Iv);
    }

    @Override
    protected void initContentView() {
        super.initContentView();
        mLeftLabelTvs = new TextView[mViewSize];
        mNameTvs = new TextView[mViewSize];
        mLikeCountTvs = new TextView[mViewSize];
        mLeftLabelIvs = new BaseImageView[mViewSize];
        for (int i = 0; i < mViewSize; i++) {
            mLeftLabelTvs[i] = $(mParentViews[i], mLeftLabelIds[i]);
            mNameTvs[i] = $(mParentViews[i], mNameIds[i]);
            mLikeCountTvs[i] = $(mParentViews[i], mLikeCountIds[i]);
            mLeftLabelIvs[i] = $(mParentViews[i], mLeftLabelIvIds[i]);
        }
    }

    @Override
    protected void bindVideoItem(ChannelLiveViewModel.VideoItem item, int index) {
        super.bindVideoItem(item, index);
        mNameTvs[index].setText(item.getUser().getUserNickname());
        mLikeCountTvs[index].setText(String.valueOf(item.getLikeCount()));
        Drawable drawable = itemView.getContext().getResources()
                .getDrawable(item.isLiked() ? R.drawable.home_video_icon_praised : R.drawable.feeds_video_icon_like);
        drawable.setBounds(0, 0, 30, 30);
        mLikeCountTvs[index].setCompoundDrawables(drawable, null, null, null);
    }

    @Override
    protected void bindItemOnLiveModel(ChannelLiveViewModel.BaseItem item, int i) {
        super.bindItemOnLiveModel(item, i);
        if (item.getTopLeft() != null && !TextUtils.isEmpty(item.getTopLeft().getIconUrl())) {
            mLeftLabelIvs[i].setVisibility(View.VISIBLE);
            bindImage(mLeftLabelIvs[i], item.getTopLeft().getIconUrl(), false, 270, 120,
                    ScalingUtils.ScaleType.FIT_START);
        } else {
            mLeftLabelIvs[i].setVisibility(View.GONE);
        }
        if (item.getTopLeft() != null && !TextUtils.isEmpty(item.getTopLeft().getText())) {
            mLeftLabelTvs[i].setText(item.getTopLeft().getText());
            mLeftLabelTvs[i].setVisibility(View.VISIBLE);
        } else {
            mLeftLabelTvs[i].setVisibility(View.GONE);
        }
    }

    @Override
    protected boolean isChangeImageSize() {
        return true;
    }

    @Override
    protected int getImageWidth() {
        return (U.getDisplayUtils().getScreenWidth() - U.getDisplayUtils().dip2px(10)) / 2;
    }

    @Override
    protected int getImageHeight() {
        int width = (U.getDisplayUtils().getScreenWidth() - U.getDisplayUtils().dip2px(10)) / 2;
        return width * 4 / 3;
    }

    @Override
    protected ScalingUtils.ScaleType getScaleType() {
        return ScalingUtils.ScaleType.CENTER_CROP;
    }

    @Override
    protected boolean isCircle() {
        return false;
    }

}
