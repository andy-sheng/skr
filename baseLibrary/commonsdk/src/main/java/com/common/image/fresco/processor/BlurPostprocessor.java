package com.common.image.fresco.processor;

import android.graphics.Bitmap;

import com.common.utils.U;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.imagepipeline.nativecode.Bitmaps;
import com.facebook.imagepipeline.request.BasePostprocessor;

/**
 * 高斯模糊
 * Created by lan on 15-11-12.
 */

public class BlurPostprocessor extends BasePostprocessor {
    private static int MAX_RADIUS = 20;

    private int mRadius;

    public BlurPostprocessor() {
        this(MAX_RADIUS);
    }

    public BlurPostprocessor(int radius) {
        this.mRadius = radius;
    }

    @Override
    public void process(Bitmap dest, Bitmap source) {
//        if (U.getDeviceUtils().isFlyme()) {
//            Bitmaps.copyBitmap(dest, U.getBlurUtils().fastblur(source, 100 * mRadius / 50));
//        } else {
//            RSUtils.blur(source, mRadius, dest);
//        }
//      U.getBlurUtils()中的 blurByRender 速度更快
        Bitmaps.copyBitmap(dest, U.getBlurUtils().fastblur(source, 100 * mRadius / 50));
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public CacheKey getPostprocessorCacheKey() {
        return new SimpleCacheKey(getName() + "&radius=" + mRadius);
    }
}