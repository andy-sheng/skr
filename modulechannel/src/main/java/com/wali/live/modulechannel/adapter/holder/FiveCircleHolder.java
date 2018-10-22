package com.wali.live.modulechannel.adapter.holder;

import android.view.View;

import com.common.utils.U;
import com.wali.live.modulechannel.R;

import java.util.Arrays;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 带5个圆形图片的item
 */
public class FiveCircleHolder extends RepeatHolder {
    public FiveCircleHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentViewId() {
        mViewSize = 5;
        mParentIds = new int[]{
                R.id.single_circle_1,
                R.id.single_circle_2,
                R.id.single_circle_3,
                R.id.single_circle_4,
                R.id.single_circle_5,
        };
        mIvIds = new int[mViewSize];
        Arrays.fill(mIvIds, R.id.single_iv);
        mTvIds = new int[mViewSize];
        Arrays.fill(mTvIds, R.id.single_tv);
        mBadgeIvIds = new int[mViewSize];
        Arrays.fill(mBadgeIvIds, R.id.badge_iv);
    }

    @Override
    protected boolean isChangeImageSize() {
        return true;
    }

    @Override
    protected int getImageWidth() {
        return U.getDisplayUtils().dip2px(46.67f);
    }

    @Override
    protected int getImageHeight() {
        return U.getDisplayUtils().dip2px(46.67f);
    }

    @Override
    protected boolean isCircle() {
        return true;
    }
}
