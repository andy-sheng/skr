package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

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
    protected int[] mUserNickNameTvIds;
    protected int mBottomContainerId;
    protected View[] mBottomContainers;

    protected TextView[] mTypeTvs;
    protected TextView[] mDisplayTvs;
    protected TextView[] mCountTvs;
    protected TextView[] mUserNickNameTvs;

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
        mBottomContainerId = R.id.bottom_container;
        mUserNickNameTvIds = new int[mViewSize];
        Arrays.fill(mUserNickNameTvIds, R.id.user_name_tv);
    }

    @Override
    protected void initContentView() {
        super.initContentView();

        mTypeTvs = new TextView[mViewSize];
        mDisplayTvs = new TextView[mViewSize];
        mCountTvs = new TextView[mViewSize];
        mBottomContainers = new View[mViewSize];
        mUserNickNameTvs = new TextView[mViewSize];

        for (int i = 0; i < mViewSize; i++) {
            mTypeTvs[i] = $(mParentViews[i], mTypeTvIds[i]);
            mDisplayTvs[i] = $(mParentViews[i], mDisplayTvIds[i]);
            mCountTvs[i] = $(mParentViews[i], mCountTvIds[i]);
            mBottomContainers[i] = $(mParentViews[i], mBottomContainerId);
            mUserNickNameTvs[i] = $(mParentViews[i], mUserNickNameTvIds[i]);
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

            height = (DisplayUtils.getScreenWidth() - DisplayUtils.dip2px(1f) * 2) / 3;
        } else {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mArrayArea.getLayoutParams();
            lp.rightMargin = lp.leftMargin = SIDE_MARGIN;

            height = (DisplayUtils.getScreenWidth() - SIDE_MARGIN * 2 - MIDDLE_MARGIN_THREE * 2) / 3;
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
        if (item.getUser() != null) {
            bindText(mUserNickNameTvs[i], item.getUser().getNickname());
        }
        MyLog.w(TAG, "1=" + item.getLineOneText() + " 2=" + item.getLineTwoText());
    }
}
