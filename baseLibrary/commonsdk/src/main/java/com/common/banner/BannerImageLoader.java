package com.common.banner;

import android.content.Context;
import android.widget.ImageView;

import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.image.model.oss.OssImgFactory;
import com.common.utils.ImageUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.youth.banner.loader.ImageLoader;


public class BannerImageLoader extends ImageLoader {
    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        FrescoWorker.loadImage((SimpleDraweeView) imageView, ImageFactory.newHttpImage((String) path)
                .setScaleType(ScalingUtils.ScaleType.FIT_XY)
                .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_320.getW()).build())
                .build());
    }

    @Override
    public ImageView createImageView(Context context) {
        SimpleDraweeView simpleDraweeView = new SimpleDraweeView(context);
        simpleDraweeView.setScaleType(ImageView.ScaleType.FIT_XY);
        return simpleDraweeView;
    }
}
