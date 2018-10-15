package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.wali.live.watchsdk.R;

/**
 * Created by lan on 16/4/26.
 *
 * @module 频道
 * @description 频道广告View
 */
public class SingleVideoBannerView extends AbsSingleBannerView {
    BaseImageView mBannerIv;
    TextView mSingleTv;

    public SingleVideoBannerView(Context context) {
        super(context);
    }

    public SingleVideoBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SingleVideoBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.michannel_single_video_banner_item;
    }

    @Override
    protected void initContentView() {
        mBannerIv = $(R.id.banner_iv);
        mSingleTv = $(R.id.single_tv);

        mBannerIv.getHierarchy().setPlaceholderImage(R.color.color_black_trans_10);
        mBannerIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBanner();
            }
        });
    }

    @Override
    protected void bindBannerView() {
        if (mBanner != null) {
            BaseImage baseImage = ImageFactory.newHttpImage(mBanner.getBgUrl()).build();
            baseImage.setWidth(ChannelBannerView.BANNER_IMAGE_WIDTH);
            baseImage.setHeight(ChannelBannerView.BANNER_IMAGE_HEIGHT);
            FrescoWorker.loadImage(mBannerIv, baseImage);

            mSingleTv.setText(mBanner.getDescription());
            mSingleTv.setVisibility(View.VISIBLE);
        } else {
            mSingleTv.setVisibility(View.GONE);
        }
    }
}
