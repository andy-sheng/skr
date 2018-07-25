package com.wali.live.watchsdk.channel.holder;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.base.global.GlobalData;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.holder.listener.BannerClickListener;
import com.wali.live.watchsdk.channel.view.BannerVideoView;
import com.wali.live.watchsdk.channel.view.ChannelVideoBannerView;
import com.wali.live.watchsdk.channel.viewmodel.ChannelBannerViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel.BaseLiveItem;

import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 */
public class VideoBannerHolder extends FixedHolder implements BannerClickListener {
    protected ChannelVideoBannerView mBannerView;
    protected BannerVideoView mVideoView;

    public VideoBannerHolder(View itemView) {
        super(itemView);
        changeImageSize();
    }

    @Override
    protected void initContentView() {
        mBannerView = $(R.id.banner_view);
        mVideoView = $(R.id.video_view);
    }

    protected void changeImageSize() {
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) mVideoView.getLayoutParams();
        mlp.width = getImageWidth();
        mlp.height = getImageHeight();

        mlp = (ViewGroup.MarginLayoutParams) mBannerView.getLayoutParams();
        mlp.width = getImageWidth();
        mlp.height = getImageHeight();
    }

    protected int getImageWidth() {
        return ViewGroup.MarginLayoutParams.MATCH_PARENT;
    }

    protected int getImageHeight() {
        // 比例按设计尺寸
        return (int) (GlobalData.screenWidth * IMAGE_RATIO);
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel viewModel) {
        List<ChannelLiveViewModel.BaseItem> items = viewModel.getItemDatas();
        if (items != null && items.size() > 0) {
            ChannelLiveViewModel.BaseItem item = items.get(0);
            if (item instanceof BaseLiveItem) {
                enterVideoMode((BaseLiveItem) item);
            } else {
                enterBannerMode(viewModel.getBannerItems());
            }
            exposureItem(item);
        }
    }

    private void enterVideoMode(final BaseLiveItem item) {
        mBannerView.setVisibility(View.GONE);

        mVideoView.setVisibility(View.VISIBLE);
        mVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpItem(item);
                mVideoView.enterWatch();
            }
        });
        mVideoView.bindData(item);
    }

    private void enterBannerMode(List<ChannelBannerViewModel.Banner> items) {
        mVideoView.setVisibility(View.GONE);
        mVideoView.removeVideoRunnable();

        mBannerView.setVisibility(View.VISIBLE);
        mBannerView.setBannerClickListener(this);
        mBannerView.setData(items);
        mBannerView.startBannerAutoScroll();
    }

    @Override
    public void clickBanner(ChannelBannerViewModel.Banner banner) {
        String url = banner.getLinkUrl();
        if (!TextUtils.isEmpty(url)) {
            mJumpListener.jumpScheme(url);
        }
    }
}
