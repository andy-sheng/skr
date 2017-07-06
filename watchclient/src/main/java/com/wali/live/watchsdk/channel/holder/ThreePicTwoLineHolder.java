package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.widget.TextView;

import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelTwoTextViewModel;

import java.util.Arrays;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 */
public class ThreePicTwoLineHolder extends RepeatHolder {
    protected int[] mDisplayTvIds;
    protected TextView[] mDisplayTvs;

    public ThreePicTwoLineHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentViewId() {
        mViewSize = 3;
        mParentIds = new int[]{
                R.id.single_pic_1,
                R.id.single_pic_2,
                R.id.single_pic_3,
        };
        mIvIds = new int[mViewSize];
        Arrays.fill(mIvIds, R.id.single_iv);
        mTvIds = new int[mViewSize];
        Arrays.fill(mTvIds, R.id.single_tv);
        mDisplayTvIds = new int[mViewSize];
        Arrays.fill(mDisplayTvIds, R.id.display_tv);
    }

    @Override
    protected void initTitleView() {
        super.initTitleView();
        mSplitLine.setVisibility(View.GONE);
    }

    @Override
    protected void initContentView() {
        super.initContentView();
        mDisplayTvs = new TextView[mViewSize];
        for (int i = 0; i < mViewSize; i++) {
            mDisplayTvs[i] = $(mParentViews[i], mDisplayTvIds[i]);
            mTextViews[i].setSingleLine();
        }
    }

    @Override
    protected boolean isChangeImageSize() {
        return true;
    }

    @Override
    protected int getImageWidth() {
        int width = (DisplayUtils.getScreenWidth() - SIDE_MARGIN * 2 - MIDDLE_MARGIN * 2) / 3;
        return width;
    }

    @Override
    protected int getImageHeight() {
        int height = (DisplayUtils.getScreenWidth() - SIDE_MARGIN * 2 - MIDDLE_MARGIN * 2) / 3;
        return height;
    }

    @Override
    protected boolean isCircle() {
        return false;
    }

    @Override
    protected void bindItemOnTwoLineModel(ChannelTwoTextViewModel.TwoLineItem item, int i) {
        bindText(mDisplayTvs[i], item.getDesc());
    }
}
