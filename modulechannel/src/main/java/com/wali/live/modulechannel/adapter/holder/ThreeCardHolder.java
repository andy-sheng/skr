package com.wali.live.modulechannel.adapter.holder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.utils.U;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;

import java.util.Arrays;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 并列3个方形图片的item
 */
public class ThreeCardHolder extends RepeatHolder {
    protected View mMarginArea;
    protected View mArrayArea;

    protected int[] mTypeTvIds;
    protected int[] mDisplayTvIds;
    protected int[] mCountTvIds;
    protected int[] mShadowTvIds;

    protected TextView[] mTypeTvs;
    protected TextView[] mDisplayTvs;
    protected TextView[] mCountTvs;
    protected TextView[] mShadowTvs;

    public ThreeCardHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentViewId() {
        mViewSize = 3;
        mParentIds = new int[]{
                R.id.single_card_1,
                R.id.single_card_2,
                R.id.single_card_3,
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
        mArrayArea = $(R.id.array_area);
    }

    @Override
    protected void initTitleView() {
        super.initTitleView();
        mSplitLine.setVisibility(View.GONE);
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

        int height;
        if (mViewModel.isFullColumn()) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mArrayArea.getLayoutParams();
            lp.rightMargin = lp.leftMargin = 0;

            height = (U.getDisplayUtils().getScreenWidth() - U.getDisplayUtils().dip2px(1f) * 2) / 3;
        } else {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mArrayArea.getLayoutParams();
            lp.rightMargin = lp.leftMargin = SIDE_MARGIN;

            height = (U.getDisplayUtils().getScreenWidth() - SIDE_MARGIN * 2 - MIDDLE_MARGIN_THREE * 2) / 3;
        }
        for (int i = 0; i < mViewSize; i++) {
            mImageViews[i].getLayoutParams().height = height;
        }
    }

    @Override
    protected void bindItemOnLiveModel(ChannelLiveViewModel.BaseItem item, int i) {
        super.bindItemOnLiveModel(item, i);
        //默认都是一行
//        if (viewModel.isThreeCardTwoLine()) {
//            mTextViews[i].setSingleLine(false);
//            mTextViews[i].setLines(2);
//        } else {
//            mTextViews[i].setSingleLine();
//        }

        bindText(mTypeTvs[i], item.getUpRightText());
        bindText(mDisplayTvs[i], item.getDisplayText());
    }

    @Override
    protected void resetItem(int i) {
        mCountTvs[i].setVisibility(View.GONE);
        mMarkTvs[i].setVisibility(View.GONE);
        mShadowTvs[i].setVisibility(View.GONE);
    }

    @Override
    protected void bindBaseLiveItem(ChannelLiveViewModel.BaseLiveItem item, int i) {
        mCountTvs[i].setText(String.valueOf(item.getViewerCnt()));
        mCountTvs[i].setVisibility(View.VISIBLE);
        mShadowTvs[i].setVisibility(View.VISIBLE);
    }

    @Override
    protected void bindVideoItem(ChannelLiveViewModel.VideoItem item, int i) {
        mCountTvs[i].setText(String.valueOf(item.getViewCount()));
        mCountTvs[i].setVisibility(View.VISIBLE);
        mShadowTvs[i].setVisibility(View.VISIBLE);
    }
}
