package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.view.ViewGroup;

import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

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
                lp.height = DisplayUtils.getScreenWidth() * 450 / 1080;
            }else {
                lp.height = DisplayUtils.getScreenWidth() * viewModel.getItemDatas().get(0).getHeight() / viewModel.getItemDatas().get(0).getWidth();
            }
        }
    }

}
