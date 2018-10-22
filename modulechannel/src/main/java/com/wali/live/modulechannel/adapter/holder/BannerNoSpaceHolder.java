package com.wali.live.modulechannel.adapter.holder;

import android.view.View;
import android.view.ViewGroup;

import com.common.utils.U;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;

/**
 * Created by fengminchao on 17/9/4
 */

public class BannerNoSpaceHolder extends BaseBannerHolder{

    public BannerNoSpaceHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel viewModel) {
        super.bindLiveModel(viewModel);
        ViewGroup.LayoutParams lp = mBannerView.getLayoutParams();
        if (viewModel.getItemDatas() != null && viewModel.getItemDatas().size() > 0) {
            if (viewModel.getItemDatas().get(0).getWidth() == 0){
                lp.height = U.getDisplayUtils().getScreenWidth() * 450 / 1080;
            }else {
                lp.height = U.getDisplayUtils().getScreenWidth() * viewModel.getItemDatas().get(0).getHeight() / viewModel.getItemDatas().get(0).getWidth();
            }
        }
    }

}
