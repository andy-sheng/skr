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

public class BaseBannerHolder extends BaseHolder<ChannelLiveViewModel> implements BannerClickListener {

    protected ChannelBannerView mBannerView;

    public BaseBannerHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView() {
        mBannerView = $(R.id.banner_view);
    }

    @Override
    protected void bindView() {
        mBannerView.setBannerClickListener(this);
        mBannerView.setData(mViewModel.getBannerItems());
        mBannerView.startBannerAutoScroll();

        for (BaseJumpItem item : mViewModel.getItemDatas()) {
            exposureItem(item);
        }
    }

    /**
     * 曝光打点
     * @param item
     */
    protected void exposureItem(BaseJumpItem item) {
        MyLog.i(TAG, "exposureItem=" + item.isExposured());
        if (!item.isExposured()) {
            HolderHelper.sendExposureCommand(item);
            item.setIsExposured(true);
        }
    }

    @Override
    public void clickBanner(ChannelBannerViewModel.Banner banner) {
        String url = banner.getLinkUrl();
        if (!TextUtils.isEmpty(url)) {
//            //打点
//            {
//                String key = banner.getEncodeKey();
//                if (!TextUtils.isEmpty(key)) {
//                    StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, key, 1);
//                }
//                WebViewActivity.clickActStatic(url, WebViewActivity.STATIC_CLICK_FROM_BANNER);
//            }
            HolderHelper.sendClickCommand(banner);  // 打点
            if (mJumpListener != null) {
                mJumpListener.jumpScheme(banner.getLinkUrl());
            }
        }
    }
}
