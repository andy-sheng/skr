package com.wali.live.channel.holder;

import android.view.View;
import android.widget.LinearLayout;

import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.mi.liveassistant.R;
import com.wali.live.channel.viewmodel.ChannelLiveViewModel;

import java.util.Arrays;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 并列2个方形图片的item
 */
public class TwoCardHolder extends RepeatHolder {
    protected View mArrayArea;

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
        Arrays.fill(mIvIds, R.id.avatar_iv);
        mTvIds = new int[mViewSize];
        Arrays.fill(mTvIds, R.id.name_tv);
    }

    @Override
    protected void initContentView() {
        super.initContentView();
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
}
