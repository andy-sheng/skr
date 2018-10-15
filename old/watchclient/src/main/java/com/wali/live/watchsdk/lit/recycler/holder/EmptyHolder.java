package com.wali.live.watchsdk.lit.recycler.holder;

import android.view.View;
import android.widget.TextView;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.lit.recycler.viewmodel.EmptyModel;

/**
 * Created by lan on 16/6/28.
 */
public class EmptyHolder extends BaseHolder<EmptyModel> {
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
}