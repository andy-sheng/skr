package com.common.banner;

import android.content.Context;
import android.widget.ImageView;

import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.facebook.drawee.view.SimpleDraweeView;
import com.youth.banner.loader.ImageLoader;


public class BannerImageLoader extends ImageLoader {
    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        FrescoWorker.loadImage((SimpleDraweeView) imageView, ImageFactory.newHttpImage((String) path)
                .setWidth(imageView.getMeasuredWidth())
                .setHeight(imageView.getMeasuredHeight())
                .build());
    }

    @Override
    public ImageView createImageView(Context context) {

        return new SimpleDraweeView(context);
    }
}
