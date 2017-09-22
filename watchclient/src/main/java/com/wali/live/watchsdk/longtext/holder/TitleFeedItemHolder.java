package com.wali.live.watchsdk.longtext.holder;

import android.view.View;
import android.widget.TextView;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.longtext.model.interior.item.TitleFeedItemModel;

/**
 * Created by lan on 2017/9/20.
 */
public class TitleFeedItemHolder extends BaseFeedItemHolder<TitleFeedItemModel> {
    public TextView mTitleTv;

    public TitleFeedItemHolder(View view) {
        super(view);
    }

    @Override
    protected void initView() {
        mTitleTv = $(R.id.title_tv);
    }

    @Override
    protected void bindView() {
        bindText(mTitleTv, mViewModel.getTitle());
    }
}
