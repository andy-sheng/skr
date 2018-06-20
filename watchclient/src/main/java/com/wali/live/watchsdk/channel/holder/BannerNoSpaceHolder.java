package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.view.ViewGroup;

import com.base.utils.display.DisplayUtils;

/**
 * Created by fengminchao on 17/9/4
 */

public class BannerNoSpaceHolder extends BaseBannerHolder{

    public BannerNoSpaceHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void bindView() {
        super.bindView();
        ViewGroup.LayoutParams lp = mBannerView.getLayoutParams();
        if (mViewModel.getItemDatas() != null && mViewModel.getItemDatas().size() > 0) {
            if (mViewModel.getItemDatas().get(0).getWidth() == 0){
                lp.height = DisplayUtils.getScreenWidth() * 450 / 1080;
            }else {
                lp.height = DisplayUtils.getScreenWidth() * mViewModel.getItemDatas().get(0).getHeight() / mViewModel.getItemDatas().get(0).getWidth();
            }
        }
    }

}
