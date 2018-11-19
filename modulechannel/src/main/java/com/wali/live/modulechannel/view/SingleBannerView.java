package com.wali.live.modulechannel.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.utils.U;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.adapter.holder.FixedHolder;

/**
 * Created by lan on 16/4/26.
 *
 * @module 频道
 * @description 频道广告View
 */
public class SingleBannerView extends AbsSingleBannerView {
    BaseImageView mBannerIv;
    TextView mActionBtn;
    TextView mDisplayTv;

    protected String getTAG() {
        return getClass().getSimpleName();
    }

    public SingleBannerView(Context context) {
        super(context);
    }

    public SingleBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SingleBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.channel_single_banner_item;
    }

    @Override
    protected void initContentView() {
        mBannerIv = $(R.id.banner_iv);
        mActionBtn = $(R.id.action_btn);
        mDisplayTv = $(R.id.display_tv);

        mBannerIv.getHierarchy().setPlaceholderImage(R.color.black_trans_10);
        mBannerIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBanner();
            }
        });

        mActionBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAction();
            }
        });
    }

    @Override
    protected void bindBannerView() {
        if (mBanner != null) {
            FrescoWorker.loadImage(mBannerIv,
                    ImageFactory.newHttpImage(mBanner.getBgUrl())
                            .setWidth(ChannelBannerView.BANNER_IMAGE_WIDTH)
                            .setHeight(ChannelBannerView.BANNER_IMAGE_HEIGHT)
                            .setCornerRadius(FixedHolder.IMAGE_CORNER_RADIUS)
                            .build()
            );

            mActionBtn.setVisibility(View.GONE);
            mDisplayTv.setVisibility(View.GONE);
        }
    }

    private void onClickAction() {
        U.getToastUtil().showShort("action click");
    }
}
