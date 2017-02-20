package com.wali.live.channel.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.IFrescoCallBack;
import com.base.image.fresco.image.ImageFactory;
import com.facebook.drawee.drawable.ScalingUtils.ScaleType;
import com.wali.live.channel.viewmodel.ChannelLiveViewModel;
import com.wali.live.channel.viewmodel.ChannelViewModel;
import com.mi.liveassistant.R;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 固定样式的item，提供ShowViewModel，UserViewModel，LiveViewModel的抽象绑定方法
 */
public abstract class FixedHolder extends BaseHolder<ChannelViewModel> {
    public FixedHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void bindView() {
        if (mViewModel instanceof ChannelLiveViewModel) {
            bindLiveModel((ChannelLiveViewModel) mViewModel.get());
        }
    }

    protected void bindLiveModel(ChannelLiveViewModel viewModel) {
    }

    protected void bindText(TextView tv, String... texts) {
        if (tv == null) {
            return;
        }
        for (String text : texts) {
            if (!TextUtils.isEmpty(text)) {
                tv.setVisibility(View.VISIBLE);
                tv.setText(text);
                return;
            }
        }
        tv.setVisibility(View.GONE);
    }

    protected void bindImage(BaseImageView iv, String url, boolean isCircle, int width, int height, ScaleType scaleType) {
        bindImageWithCallback(iv, url, isCircle, width, height, scaleType, null);
    }

    protected void bindImageWithCallback(BaseImageView iv, String url, boolean isCircle, int width, int height, ScaleType scaleType, IFrescoCallBack callBack) {
        if (iv == null) {
            return;
        }
        FrescoWorker.loadImage(iv,
                ImageFactory.newHttpImage(url)
                        .setIsCircle(isCircle)
                        .setWidth(width).setHeight(height)
                        .setScaleType(scaleType)
                        .setLoadingDrawable(GlobalData.app().getResources().getDrawable(R.color.color_f2f2f2))
                        .setFailureDrawable(GlobalData.app().getResources().getDrawable(R.color.color_f2f2f2))
                        .setCallBack(callBack)
                        .build());
    }

    protected void bindImageWithBorder(BaseImageView iv, String url, boolean isCircle, int width, int height, ScaleType scaleType) {
        if (iv == null) {
            return;
        }
        FrescoWorker.loadImage(iv,
                ImageFactory.newHttpImage(url)
                        .setIsCircle(isCircle)
                        .setWidth(width).setHeight(height)
                        .setScaleType(scaleType)
                        .setLoadingDrawable(GlobalData.app().getResources().getDrawable(R.color.color_f2f2f2))
                        .setFailureDrawable(GlobalData.app().getResources().getDrawable(R.color.color_f2f2f2))
                        .setCornerRadius(5)
                        .setBorderWidth(1)
                        .setBorderColor(GlobalData.app().getResources().getColor(R.color.color_e5e5e5))
                        .build());
    }
}
