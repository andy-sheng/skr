package com.wali.live.modulechannel.adapter.holder;

import android.view.View;

import com.common.utils.U;
import com.wali.live.modulechannel.R;

import java.util.Arrays;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 并列3个方形图片的item，比ThreeCardHolder内容更简单
 */
public class ThreePicHolder extends RepeatHolder {
    public ThreePicHolder(View itemView) {
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
    }

    @Override
    protected void initTitleView() {
        super.initTitleView();
        mSplitLine.setVisibility(View.GONE);
    }

    @Override
    protected void initContentView() {
        super.initContentView();
        for (int i = 0; i < mViewSize; i++) {
            mTextViews[i].setSingleLine();
        }
    }

    @Override
    protected boolean isChangeImageSize() {
        return true;
    }

    @Override
    protected int getImageWidth() {
        int width = (U.getDisplayUtils().getScreenWidth() - SIDE_MARGIN * 2 - MIDDLE_MARGIN * 2) / 3;
        return width;
    }

    @Override
    protected int getImageHeight() {
        int height = (U.getDisplayUtils().getScreenWidth() - SIDE_MARGIN * 2 - MIDDLE_MARGIN * 2) / 3;
        return height;
    }

    @Override
    protected boolean isCircle() {
        return false;
    }
}
