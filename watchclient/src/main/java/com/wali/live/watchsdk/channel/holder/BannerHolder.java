package com.wali.live.watchsdk.channel.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.helper.HolderHelper;
import com.wali.live.watchsdk.channel.holder.listener.BannerClickListener;
import com.wali.live.watchsdk.channel.view.ChannelBannerView;
import com.wali.live.watchsdk.channel.viewmodel.BaseJumpItem;
import com.wali.live.watchsdk.channel.viewmodel.ChannelBannerViewModel;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 适配ChannelBannerViewModel2的广告位item
 * @notice 与BannerHolder不同，因为开始小米频道和推荐频道有两套代码，这里先做下兼容
 */
public class BannerHolder extends BaseHolder<ChannelBannerViewModel> implements BannerClickListener {
    private static final int MARGIN = DisplayUtils.dip2px(3.33f);
    protected View mSplitArea;
    protected ChannelBannerView mBannerView;

    public BannerHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initView() {
        mSplitArea = $(R.id.split_area);
        mSplitArea.setVisibility(View.GONE);

        mBannerView = $(R.id.banner_view);
    }

    @Override
    protected void bindView() {
        mBannerView.setBannerClickListener(this);
        mBannerView.setData(mViewModel.getItemDatas());
        mBannerView.startBannerAutoScroll();

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mBannerView.getLayoutParams();
        if (mViewModel.isLargeSize()) {
            lp.height = (GlobalData.screenWidth - MARGIN * 2) * 600 / 1080;
        } else {
            lp.height = (GlobalData.screenWidth - MARGIN * 2) * 300 / 1080;
        }

        for (BaseJumpItem item : mViewModel.getItemDatas()) {
            exposureItem(item);
        }
    }

    /**
     * 曝光打点
     */
    protected void exposureItem(BaseJumpItem item) {
        MyLog.d(TAG, "exposureItem=" + item.isExposured());
        if (!item.isExposured()) {
            HolderHelper.sendExposureCommand(item);
            item.setIsExposured(true);
        }
    }

    @Override
    public void clickBanner(ChannelBannerViewModel.Banner banner) {
        String url = banner.getLinkUrl();
        if (!TextUtils.isEmpty(url)) {
            HolderHelper.sendClickCommand(banner);
            mJumpListener.jumpScheme(banner.getLinkUrl());
        } else {
            MyLog.e(TAG, "clickBanner url is empty");
        }
    }
}
