package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.wali.live.watchsdk.channel.holder.listener.BannerClickListener;
import com.wali.live.watchsdk.channel.viewmodel.ChannelBannerViewModel;

import butterknife.ButterKnife;

/**
 * Created by lan on 16/4/26.
 *
 * @module 频道
 * @description 频道广告View
 */
public abstract class AbsSingleBannerView extends RelativeLayout {
    protected final String TAG = getTAG();

    protected ChannelBannerViewModel.Banner mBanner;
    protected BannerClickListener mBannerClickListener;

    protected <V extends View> V $(int resId) {
        return (V) findViewById(resId);
    }

    protected String getTAG() {
        return getClass().getSimpleName();
    }

    public AbsSingleBannerView(Context context) {
        super(context);
        init();
    }

    public AbsSingleBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AbsSingleBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), getLayoutId(), this);
        ButterKnife.bind(this);

        initContentView();
    }

    protected abstract int getLayoutId();

    protected abstract void initContentView();

    public void setBanner(ChannelBannerViewModel.Banner banner) {
        mBanner = banner;
        bindBannerView();
    }

    protected abstract void bindBannerView();

    public void setBannerClickListener(BannerClickListener bannerClickListener) {
        mBannerClickListener = bannerClickListener;
    }

    protected void onClickBanner() {
        if (mBanner != null) {
            if (mBannerClickListener != null) {
                mBannerClickListener.clickBanner(mBanner);
            } else {
                defaultClickBanner();
            }
        }
    }

    private void defaultClickBanner() {
        MyLog.d(TAG, "defaultClickBanner");
        String url = mBanner.getLinkUrl();
        if (!TextUtils.isEmpty(url)) {
//            //打点
//            {
//                String key = mBanner.getEncodeKey();
//                if (!TextUtils.isEmpty(key)) {
//                    StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, key, 1);
//                }
//                WebViewActivity.clickActStatic(url, WebViewActivity.STATIC_CLICK_FROM_BANNER);
//            }
//            EventBus.getDefault().post(new EventClass.OnClickBannerEvent(mBanner.toBannerItem()));
        }
    }
}
