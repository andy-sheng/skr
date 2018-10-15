package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

import java.util.Arrays;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 并列2个方形图片的item，比TwoCardHolder更宽
 */
public class TwoWideCardHolder extends RepeatHolder {
    protected View mMarginArea;

    protected int[] mTypeTvIds;
    protected int[] mDisplayTvIds;
    protected int[] mCountTvIds;
    protected int[] mShadowTvIds;

    protected TextView[] mTypeTvs;
    protected TextView[] mDisplayTvs;
    protected TextView[] mCountTvs;
    protected TextView[] mShadowTvs;

    public TwoWideCardHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentViewId() {
        mViewSize = 2;
        mParentIds = new int[]{
                R.id.single_card_1,
                R.id.single_card_2,
        };
        mIvIds = new int[mViewSize];
        Arrays.fill(mIvIds, R.id.cover_iv);
        mTvIds = new int[mViewSize];
        Arrays.fill(mTvIds, R.id.name_tv);

        mTypeTvIds = new int[mViewSize];
        Arrays.fill(mTypeTvIds, R.id.type_tv);
        mDisplayTvIds = new int[mViewSize];
        Arrays.fill(mDisplayTvIds, R.id.display_tv);
        mCountTvIds = new int[mViewSize];
        Arrays.fill(mCountTvIds, R.id.count_tv);
        mShadowTvIds = new int[mViewSize];
        Arrays.fill(mShadowTvIds, R.id.shadow_tv);
        mBottomContainerId = R.id.bottom_container;
        mMarkTvId = R.id.mark_tv;
    }

    @Override
    protected void initTitleView() {
        super.initTitleView();
        mSplitLine.setVisibility(View.GONE);
    }

    @Override
    protected void initContentView() {
        super.initContentView();

        mTypeTvs = new TextView[mViewSize];
        mDisplayTvs = new TextView[mViewSize];
        mCountTvs = new TextView[mViewSize];
        mMarkTvs = new TextView[mViewSize];
        mShadowTvs = new TextView[mViewSize];
        mBottomContainers = new View[mViewSize];
        for (int i = 0; i < mViewSize; i++) {
            mTypeTvs[i] = $(mParentViews[i], mTypeTvIds[i]);
            mDisplayTvs[i] = $(mParentViews[i], mDisplayTvIds[i]);
            mCountTvs[i] = $(mParentViews[i], mCountTvIds[i]);
            mMarkTvs[i] = $(mParentViews[i], mMarkTvId);
            mShadowTvs[i] = $(mParentViews[i], mShadowTvIds[i]);
            mBottomContainers[i] = $(mParentViews[i], mBottomContainerId);
        }

        mMarginArea = $(R.id.margin_area);
        int width = (GlobalData.screenWidth - MIDDLE_MARGIN_TWO - SIDE_MARGIN) / 2;
        for (int i = 0; i < mViewSize; i++) {
            // 比例按设计尺寸
            mImageViews[i].getLayoutParams().height = (int) (width * IMAGE_RATIO);
        }
    }

    @Override
    protected boolean isChangeImageSize() {
        return false;
    }

    @Override
    protected boolean isCircle() {
        return false;
    }

    @Override
    protected ScalingUtils.ScaleType getScaleType() {
        return ScalingUtils.ScaleType.CENTER_CROP;
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel viewModel) {
        super.bindLiveModel(viewModel);

        // 如果没有标题，而且是组内第一个的话，就显示margin区域
        if (!mViewModel.hasHead() && viewModel.isFirst() && mPosition != 0) {
            mMarginArea.setVisibility(View.VISIBLE);
        } else {
            mMarginArea.setVisibility(View.GONE);
        }
    }

    protected void bindItemOnLiveModel(ChannelLiveViewModel.BaseItem item, int i) {
        super.bindItemOnLiveModel(item, i);
        bindText(mTypeTvs[i], item.getUpRightText());
        bindText(mDisplayTvs[i], item.getDisplayText());
        bindMarkTv(item, i);
    }

    @Override
    protected void resetItem(int i) {
        mCountTvs[i].setVisibility(View.GONE);
        mMarkTvs[i].setVisibility(View.GONE);
        mShadowTvs[i].setVisibility(View.GONE);
    }

    @Override
    protected void bindBaseLiveItem(ChannelLiveViewModel.BaseLiveItem item, int i) {
        mCountTvs[i].setText(item.getCountString());
        mCountTvs[i].setVisibility(View.VISIBLE);
        mShadowTvs[i].setVisibility(View.VISIBLE);
    }

    @Override
    protected void bindVideoItem(ChannelLiveViewModel.VideoItem item, int i) {
        mCountTvs[i].setText(item.getCountString());
        mCountTvs[i].setVisibility(View.VISIBLE);
        mShadowTvs[i].setVisibility(View.VISIBLE);
    }
}
