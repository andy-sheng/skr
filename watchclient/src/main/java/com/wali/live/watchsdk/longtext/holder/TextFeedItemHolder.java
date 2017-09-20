package com.wali.live.watchsdk.longtext.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.longtext.model.interior.item.TextFeedItemModel;

/**
 * Created by lan on 2017/9/20.
 */
public class TextFeedItemHolder extends BaseFeedItemHolder<TextFeedItemModel> {
    private TextView mContentTv;

    public TextFeedItemHolder(View view) {
        super(view);
    }

    @Override
    protected void initView() {
        mContentTv = $(R.id.content_tv);
    }

    @Override
    protected void bindView() {
        if (TextUtils.isEmpty(mViewModel.getContent())) {
            mContentTv.setVisibility(View.GONE);
            return;
        }

        mContentTv.setVisibility(View.VISIBLE);
        mContentTv.setText(mViewModel.getContent());
    }
}
