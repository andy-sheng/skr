package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

import java.util.Arrays;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 并列2个方形图片的item
 */
public class TwoCardHolder extends RepeatHolder {
    protected View mArrayArea;
    protected View mMarginArea;

    protected int[] mTypeTvIds;
    protected int[] mDisplayTvIds;
    protected int[] mCountTvIds;
    protected int[] mShadowTvIds;
    protected int mUserNickNameTvIds;

    protected TextView[] mTypeTvs;
    protected TextView[] mDisplayTvs;
    protected TextView[] mCountTvs;
    protected TextView[] mShadowTvs;
    protected TextView[] mUserNickNameTvs;

    protected View[] mBottomContainers;

    protected int mBottomContainerId;
    protected int mDisplayTvEmptyNum;

    public TwoCardHolder(View itemView) {
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
        mUserNickNameTvIds = R.id.user_name_tv;
    }

    @Override
    protected void initContentView() {
        super.initContentView();
        mTypeTvs = new TextView[mViewSize];
        mDisplayTvs = new TextView[mViewSize];
        mCountTvs = new TextView[mViewSize];
        mShadowTvs = new TextView[mViewSize];
        mBottomContainers = new View[mViewSize];
        mUserNickNameTvs = new TextView[mViewSize];

        for (int i = 0; i < mViewSize; i++) {
            mTypeTvs[i] = $(mParentViews[i], mTypeTvIds[i]);
            mDisplayTvs[i] = $(mParentViews[i], mDisplayTvIds[i]);
            mCountTvs[i] = $(mParentViews[i], mCountTvIds[i]);
            mShadowTvs[i] = $(mParentViews[i], mShadowTvIds[i]);
            mBottomContainers[i] = $(mParentViews[i], mBottomContainerId);
            mUserNickNameTvs[i] = $(mParentViews[i], mUserNickNameTvIds);
        }

        mMarginArea = $(R.id.margin_area);
        mArrayArea = $(R.id.array_area);
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
        mDisplayTvEmptyNum = 0;
        // 如果没有标题，而且是组内第一个的话，就显示margin区域
        if (!mViewModel.hasHead() && mPosition != 0) {
            mMarginArea.setVisibility(View.VISIBLE);
        } else {
            mMarginArea.setVisibility(View.GONE);
        }

        int height;
        if (mViewModel.isFullColumn()) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mArrayArea.getLayoutParams();
            lp.rightMargin = lp.leftMargin = 0;

            height = (DisplayUtils.getScreenWidth() - DisplayUtils.dip2px(1f)) / 2;
        } else {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mArrayArea.getLayoutParams();
            lp.rightMargin = lp.leftMargin = SIDE_MARGIN;

            height = (DisplayUtils.getScreenWidth() - SIDE_MARGIN * 2 - MIDDLE_MARGIN_TWO) / 2;
        }
        for (int i = 0; i < mViewSize; i++) {
            mImageViews[i].getLayoutParams().height = height;
        }
    }

    @Override
    protected void bindItemOnLiveModel(ChannelLiveViewModel.BaseItem item, int i) {
        super.bindItemOnLiveModel(item, i);
        bindText(mUserNickNameTvs[i], item.getUser().getNickname());
    }

    @Override
    protected void bindBaseLiveItem(ChannelLiveViewModel.BaseLiveItem item, int i) {
        super.bindBaseLiveItem(item, i);
        mCountTvs[i].setText(item.getCountString());
        mCountTvs[i].setVisibility(View.VISIBLE);
        mShadowTvs[i].setVisibility(View.VISIBLE);
        bindText(mDisplayTvs[i], item.getLocation());
    }
}
