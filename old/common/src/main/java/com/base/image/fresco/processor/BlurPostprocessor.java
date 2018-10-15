package com.base.image.fresco.processor;

import android.graphics.Bitmap;

import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.image.BoxBlur;
import com.base.utils.image.RSUtils;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.imagepipeline.nativecode.Bitmaps;
import com.facebook.imagepipeline.request.BasePostprocessor;

/**
 * Created by lan on 15-11-12.
 */

public class BlurPostprocessor extends BasePostprocessor {
    private static final String TAG = BlurPostprocessor.class.getSimpleName();

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
        if (CommonUtils.isMeizu()) {
            Bitmaps.copyBitmap(dest, BoxBlur.fastblur(source, 100 * mRadius / 50));
        } else {
            MyLog.d(TAG, "time1=" + System.currentTimeMillis());
            RSUtils.blur(source, mRadius, dest);
            MyLog.d(TAG, "time2=" + System.currentTimeMillis());
        }
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