package com.wali.live.watchsdk.longtext.holder;

import android.view.View;
import android.widget.TextView;

import com.base.utils.date.DateTimeUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.longtext.model.interior.item.ViewerFeedItemModel;

/**
 * Created by lan on 2017/9/20.
 */
public class ViewerFeedItemHolder extends BaseFeedItemHolder<ViewerFeedItemModel> {
    public TextView mViewerTv;
    public TextView mLikeTv;
    public TextView mTimeTv;

    public ViewerFeedItemHolder(View view) {
        super(view);
    }

    @Override
    protected void initView() {
        mViewerTv = $(R.id.viewer_tv);
        mLikeTv = $(R.id.like_tv);
        mTimeTv = $(R.id.time_tv);
    }

    @Override
    protected void bindView() {
        if (mViewModel.getViewerCount() > 0) {
            mViewerTv.setVisibility(View.VISIBLE);
            mViewerTv.setText(itemView.getResources().getString(R.string.read_count, mViewModel.getViewerCount()));
        } else {
            mViewerTv.setVisibility(View.GONE);
        }

        if (mViewModel.getLikeCount() > 0) {
            mLikeTv.setVisibility(View.VISIBLE);
            mLikeTv.setText(itemView.getResources().getString(R.string.like_count_text, mViewModel.getLikeCount()));
        } else {
            mLikeTv.setVisibility(View.GONE);
        }

        if (mViewModel.getCreateTime() <= 0) {
            mTimeTv.setVisibility(View.GONE);
        } else {
            mTimeTv.setVisibility(View.VISIBLE);
            String time = DateTimeUtils.formatFeedsJournalCreateData(mViewModel.getCreateTime(), System.currentTimeMillis());
            mTimeTv.setText(time);
        }
    }
}
