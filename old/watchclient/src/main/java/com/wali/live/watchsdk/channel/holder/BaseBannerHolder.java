package com.wali.live.watchsdk.channel.holder;

import android.text.TextUtils;
import android.view.View;

import com.base.log.MyLog;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.helper.HolderHelper;
import com.wali.live.watchsdk.channel.holder.listener.BannerClickListener;
import com.wali.live.watchsdk.channel.view.ChannelBannerView;
import com.wali.live.watchsdk.channel.viewmodel.BaseJumpItem;
import com.wali.live.watchsdk.channel.viewmodel.ChannelBannerViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;
/**
 * Created by fengminchao on 17/9/4
 * UiTemplateLiveOrReplayInfo模板的banner holder
 */

public class BaseBannerHolder extends FixedHolder implements BannerClickListener {

    protected ChannelBannerView mBannerView;

    public BaseBannerHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentView() {
        mBannerView = $(R.id.banner_view);
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel viewModel) {
        mBannerView.setBannerClickListener(this);
        mBannerView.setData(viewModel.getBannerItems());
        mBannerView.startBannerAutoScroll();

        for (BaseJumpItem item : viewModel.getItemDatas()) {
            exposureItem(item);
        }
    }

    @Override
    public void clickBanner(ChannelBannerViewModel.Banner banner) {
        String url = banner.getLinkUrl();
        if (!TextUtils.isEmpty(url)) {
            HolderHelper.sendClickCommand(banner);  // 打点
            if (mJumpListener != null) {
                mJumpListener.jumpScheme(banner.getLinkUrl());
            }
        }
    }
}
