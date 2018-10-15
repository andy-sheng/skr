package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by lan on 16/4/26.
 *
 * @module 频道
 * @description 频道广告View
 */
public class ChannelVideoBannerView extends ChannelBannerView {
    public ChannelVideoBannerView(Context context) {
        super(context);
    }

    public ChannelVideoBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelVideoBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mViewPagerWithCircleIndicator.setLimitHeight(33);
    }

    @Override
    protected AbsSingleBannerView newSingleBannerView() {
        return new SingleVideoBannerView(getContext());
    }
}
