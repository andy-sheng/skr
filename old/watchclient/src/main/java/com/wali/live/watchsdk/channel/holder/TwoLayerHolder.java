package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.view.ViewGroup;

import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.watchsdk.R;

import java.util.Arrays;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 两层结构的item，上层显示大图，下层显示两个图片
 */
public class TwoLayerHolder extends RepeatHolder {
    public TwoLayerHolder(View itemView) {
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
    protected void initContentView() {
        super.initContentView();
        if (mTextViews[mViewSize - 1] != null) {
            mTextViews[mViewSize - 1].setSingleLine();
        }
    }

    @Override
    protected void initTitleView() {
        super.initTitleView();
        mSplitLine.setVisibility(View.GONE);
    }

    @Override
    protected void changeImageSize() {
        super.changeImageSize();
        // 特殊处理
        ViewGroup.MarginLayoutParams mlp;
        mlp = (ViewGroup.MarginLayoutParams) mImageViews[2].getLayoutParams();
        mlp.width = DisplayUtils.getScreenWidth() - SIDE_MARGIN * 2;
        mlp.height = getImageHeight();
    }

    @Override
    protected boolean isChangeImageSize() {
        return true;
    }

    @Override
    protected int getImageWidth() {
        return (DisplayUtils.getScreenWidth() - SIDE_MARGIN * 2 - MIDDLE_MARGIN_TWO) / 2;
    }

    @Override
    protected int getImageHeight() {
        return DisplayUtils.dip2px(99f);
    }

    @Override
    protected boolean isCircle() {
        return false;
    }

    @Override
    protected ScalingUtils.ScaleType getScaleType() {
        return ScalingUtils.ScaleType.CENTER_CROP;
    }
}
