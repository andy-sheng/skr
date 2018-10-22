package com.wali.live.modulechannel.adapter.holder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;

import java.util.Arrays;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 与样式15不同的是，观众文案内嵌
 */
public class ThreeInnerCardHolder extends RepeatHolder {
    protected View mMarginArea;
    protected View mArrayArea;

    protected int[] mTypeTvIds;
    protected int[] mDisplayTvIds;
    protected int[] mCountTvIds;
    protected int[] mShadowTvIds;
    protected int[] mUserNickNameTvIds;
    protected int[] mMiddleTvIds;

    protected TextView[] mTypeTvs;
    protected TextView[] mDisplayTvs;
    protected TextView[] mCountTvs;
    protected TextView[] mShadowTvs;
    protected TextView[] mUserNickNameTvs;
    protected TextView[] mMiddleTvs;

    public ThreeInnerCardHolder(View itemView) {
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
        mUserNickNameTvIds = new int[mViewSize];
        Arrays.fill(mUserNickNameTvIds, R.id.user_name_tv);
        mMiddleTvIds = new int[mViewSize];
        Arrays.fill(mMiddleTvIds, R.id.middle_text);
        mLeftLabelTvId = R.id.left_label;
        mMarkTvId = R.id.mark_tv;
        mLeftLabelIvId = R.id.anchor_activity_icon;
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
        mUserNickNameTvs = new TextView[mViewSize];
        mMiddleTvs = new TextView[mViewSize];
        mLeftLabelTvs = new TextView[mViewSize];
        mLeftLabelIvs = new BaseImageView[mViewSize];

        for (int i = 0; i < mViewSize; i++) {
            mTypeTvs[i] = $(mParentViews[i], mTypeTvIds[i]);
            mDisplayTvs[i] = $(mParentViews[i], mDisplayTvIds[i]);
            mCountTvs[i] = $(mParentViews[i], mCountTvIds[i]);
            mMarkTvs[i] = $(mParentViews[i], mMarkTvId);
            mShadowTvs[i] = $(mParentViews[i], mShadowTvIds[i]);
            mBottomContainers[i] = $(mParentViews[i], mBottomContainerId);
            mUserNickNameTvs[i] = $(mParentViews[i], mUserNickNameTvIds[i]);
            mMiddleTvs[i] = $(mParentViews[i], mMiddleTvIds[i]);
            mLeftLabelTvs[i] = $(mParentViews[i], mLeftLabelTvId);
            mLeftLabelIvs[i] = $(mParentViews[i], mLeftLabelIvId);
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
        bindText(mTypeTvs[i], item.getUpRightText());
        bindText(mDisplayTvs[i], item.getDisplayText());
        bindText(mUserNickNameTvs[i], item.getUserNickName());
        bindText(mMiddleTvs[i], item.getMiddleInfo() != null ? item.getMiddleInfo().getText1() : "");
        bindLeftLabel(item, i);
        if (mLeftLabelTvs[i].getVisibility() != View.VISIBLE) {
            bindLeftWidgetInfo(item, i);
        }
    }

    @Override
    protected void resetItem(int i) {
        mCountTvs[i].setVisibility(View.GONE);
        mMarkTvs[i].setVisibility(View.GONE);
        mShadowTvs[i].setVisibility(View.GONE);
        mLeftLabelTvs[i].setVisibility(View.GONE);
        mBottomContainers[i].setVisibility(View.GONE);
        mLeftLabelIvs[i].setVisibility(View.GONE);
    }

    @Override
    protected void bindBaseLiveItem(ChannelLiveViewModel.BaseLiveItem item, int i) {
/*        mCountTvs[i].setText(item.getCountString());
        mCountTvs[i].setVisibility(View.VISIBLE);*/
        mShadowTvs[i].setVisibility(View.VISIBLE);
    }

    @Override
    protected void bindVideoItem(ChannelLiveViewModel.VideoItem item, int i) {
/*        mCountTvs[i].setText(item.getCountString());
        mCountTvs[i].setVisibility(View.VISIBLE);*/
        mShadowTvs[i].setVisibility(View.VISIBLE);
    }
}
