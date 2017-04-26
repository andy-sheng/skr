package com.wali.live.watchsdk.ranking.holder;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;
import com.wali.live.watchsdk.lit.recycler.viewmodel.SimpleTextModel;

public class FooterViewHolder extends BaseHolder<SimpleTextModel> {
    public ProgressBar mProgressView;
    public TextView mTextView;

    public FooterViewHolder(View view) {
        super(view);
    }

    @Override
    protected void initView() {
        mProgressView = $(R.id.progress_bar);
        mTextView = $(R.id.loading_more);
    }

    @Override
    protected void bindView() {

    }
}