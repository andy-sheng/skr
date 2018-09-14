package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelSplitViewModel;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 分割线item
 */
public class SplitLineHolder extends BaseHolder<ChannelSplitViewModel> {
    protected View mSplitArea;
    protected RelativeLayout mSplitCard;
    protected TextView mSplitTitle;

    public SplitLineHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView() {
        mSplitArea = $(R.id.split_area);
        mSplitCard = $(R.id.split_card);
        mSplitTitle = $(R.id.split_title);
    }

    @Override
    protected void bindView() {
        if (mViewModel.getColor() == 2) {
            mSplitArea.setBackgroundColor(itemView.getResources().getColor(R.color.color_white));
        } else if (mViewModel.getColor() == 3) {
            mSplitArea.setBackgroundColor(itemView.getResources().getColor(R.color.color_white_trans_20));
        } else {
            mSplitArea.setBackgroundColor(itemView.getResources().getColor(R.color.color_f2f2f2));
        }

        mSplitArea.setVisibility(mViewModel.getHeight() == 3 ? View.GONE : View.VISIBLE);
        mSplitCard.setVisibility(mViewModel.getHeight() == 3 ? View.VISIBLE : View.GONE);

        if (mViewModel.getHeight() == 2) {
            mSplitArea.getLayoutParams().height = DisplayUtils.dip2px(6.67f);
            ((RelativeLayout.LayoutParams)mSplitArea.getLayoutParams()).leftMargin = 0;
            ((RelativeLayout.LayoutParams)mSplitArea.getLayoutParams()).rightMargin = 0;
        } else if (mViewModel.getHeight() == 3) {
            mSplitTitle.setText(mViewModel.getTitle());
            ((RelativeLayout.LayoutParams)mSplitArea.getLayoutParams()).leftMargin = 0;
            ((RelativeLayout.LayoutParams)mSplitArea.getLayoutParams()).rightMargin = 0;
        } else if (mViewModel.getHeight() == 5) {
            mSplitArea.getLayoutParams().height = DisplayUtils.dip2px(0.33f);
            ((RelativeLayout.LayoutParams)mSplitArea.getLayoutParams()).leftMargin = DisplayUtils.dip2px(20);
            ((RelativeLayout.LayoutParams)mSplitArea.getLayoutParams()).rightMargin = DisplayUtils.dip2px(20);
        } else {
            mSplitArea.getLayoutParams().height = DisplayUtils.dip2px(6.67f);
            ((RelativeLayout.LayoutParams)mSplitArea.getLayoutParams()).leftMargin = 0;
            ((RelativeLayout.LayoutParams)mSplitArea.getLayoutParams()).rightMargin = 0;
        }
    }
}
