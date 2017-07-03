package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelViewModel;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 显示head的抽象基类
 */
public abstract class HeadHolder extends BaseHolder<ChannelViewModel> {

    protected View mHeadArea;
    protected TextView mHeadTv;
    protected TextView mSubHeadTv;
    protected View mSplitLine;
    protected View mSplitArea;

    public HeadHolder(View itemView) {
        super(itemView);
    }

    protected boolean needTitleView() {
        return true;
    }

    protected void initView() {
        if (needTitleView()) {
            initTitleView();
        }
        initContentView();
    }

    protected void initTitleView() {
        mHeadArea = $(R.id.title_area);
        if (mHeadArea != null) {
            mHeadArea.getLayoutParams().height = DisplayUtils.dip2px(43.33f);
        }
        mHeadTv = $(R.id.head_tv);
        mSubHeadTv = $(R.id.sub_head_tv);
        mSplitLine = $(R.id.split_line);
        if (mSplitLine != null) {
            mSplitLine.setVisibility(View.GONE);
        }
        mSplitArea = $(R.id.split_area);
        if (mSplitArea != null) {
            mSplitArea.setVisibility(View.GONE);
        }
    }

    protected abstract void initContentView();

    @Override
    protected void bindView() {
        if (needTitleView()) {
            bindTitleView();
        }
    }

    private void bindTitleView() {
        // 如果有标题，显示标题
        if (mHeadArea == null) {
            return;
        }
        if (mViewModel.hasHead()) {
            mHeadArea.setVisibility(View.VISIBLE);
            mSplitArea.setVisibility(mPosition == 0 ? View.GONE : View.VISIBLE);
            mHeadTv.setText(mViewModel.getHead());

            if (mViewModel.hasSubHead()) {
                mSubHeadTv.setVisibility(View.VISIBLE);
                mSubHeadTv.setText(mViewModel.getSubHead());
            } else {
                mSubHeadTv.setVisibility(View.GONE);
            }
            
            itemView.setOnClickListener(null);
            mHeadTv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            // 调整位置
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHeadTv.getLayoutParams();
            if (mViewModel.hasSubHead()) {
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                lp.addRule(RelativeLayout.CENTER_VERTICAL, 0);
                lp.leftMargin = 0;

                lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                lp.topMargin = DisplayUtils.dip2px(15f);

                mHeadArea.getLayoutParams().height = DisplayUtils.dip2px(66.67f);
            } else {
                lp.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
                lp.topMargin = 0;

                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                lp.leftMargin = DisplayUtils.dip2px(10f);

                mHeadArea.getLayoutParams().height = DisplayUtils.dip2px(43.33f);
            }
        } else {
            mHeadArea.setVisibility(View.GONE);
            mSplitArea.setVisibility(View.GONE);
        }
    }
}
