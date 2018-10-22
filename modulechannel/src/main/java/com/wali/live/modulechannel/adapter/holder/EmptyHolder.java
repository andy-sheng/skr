package com.wali.live.modulechannel.adapter.holder;

import android.view.View;
import android.widget.TextView;

import com.common.utils.U;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.EmptyViewModel;


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

        mEmptyTv.setTextColor(U.app().getResources().getColor(R.color.color_949494_trans_80));
        itemView.setBackgroundColor(U.app().getResources().getColor(R.color.transparent));
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