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
        imageView.setAdjustViewBounds(true);
        FrescoWorker.loadImage((SimpleDraweeView) imageView, ImageFactory.newHttpImage((String) path)
                .build());
    }

    @Override
    public ImageView createImageView(Context context) {

        return new SimpleDraweeView(context);
    }
}
