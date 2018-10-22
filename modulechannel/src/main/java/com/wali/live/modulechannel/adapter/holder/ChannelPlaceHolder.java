package com.wali.live.modulechannel.adapter.holder;

import android.view.View;
import android.view.ViewGroup;

import com.wali.live.modulechannel.model.viewmodel.ChannelPlaceHolderModel;

/**
 * Created by vera on 2018/6/27.
 */

public class ChannelPlaceHolder extends BaseHolder<ChannelPlaceHolderModel> {
    public ChannelPlaceHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void bindView() {
        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        layoutParams.height = mViewModel.getViewHeight();
        itemView.setLayoutParams(layoutParams);
    }
}
