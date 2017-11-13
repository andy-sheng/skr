package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.widget.TextView;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.EmptyViewModel;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 空item，提供文案设置功能
 */
public class EmptyHolder extends BaseHolder<EmptyViewModel> {
    private TextView mEmptyTv;

    public EmptyHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView() {
        mEmptyTv = $(R.id.empty_tv);
    }

    @Override
    protected void bindView() {
        mEmptyTv.setText(mViewModel.getText());
        if (mViewModel.getIconId() != 0) {
            mEmptyTv.setCompoundDrawablesWithIntrinsicBounds(0, mViewModel.getIconId(), 0, 0);
        }
    }

    public TextView getEmptyTv() {
        return mEmptyTv;
    }
}