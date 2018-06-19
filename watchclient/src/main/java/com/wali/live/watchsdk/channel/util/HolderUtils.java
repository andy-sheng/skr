package com.wali.live.watchsdk.channel.util;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.IFrescoCallBack;
import com.base.image.fresco.image.ImageFactory;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.watchsdk.R;

/**
 * Created by liuting on 18-6-15.
 */

public class HolderUtils {
    private static int mImageCornerRadius = 8;
    private static int mImageBorderWidth = 0;

    public static void bindImage(BaseImageView iv, String url, boolean isCircle, int width, int height, ScalingUtils.ScaleType scaleType) {
        bindImageWithCallback(iv, url, isCircle, width, height, scaleType, null);
    }

    public static void bindImageWithCallback(BaseImageView iv, String url, boolean isCircle, int width, int height, ScalingUtils.ScaleType scaleType, IFrescoCallBack callBack) {
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

    public static void bindImageWithBorder(BaseImageView iv, String url, boolean isCircle, int width, int height, ScalingUtils.ScaleType scaleType) {
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
                        .setCornerRadius(mImageCornerRadius)
                        .setBorderWidth(mImageBorderWidth)
                        .setBorderColor(GlobalData.app().getResources().getColor(R.color.color_e5e5e5))
                        .build());
    }

    public static void bindImageWithCorner(BaseImageView iv, String url, boolean isCircle, int width, int height, ScalingUtils.ScaleType scaleType, IFrescoCallBack callBack) {
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
                        .setCornerRadius(8)
                        .setCallBack(callBack)
                        .build());
    }
}
