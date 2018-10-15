package com.base.image.fresco.processor;

/**
 * Created by zhaomin on 16-9-13.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;

import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.request.BasePostprocessor;

/**
 * 动态界面预览长图存在问题
 * 图片太长了显示不了
 * 在这里加一个processor来截取部分长图
 */
public class PicFeedsPostprocessor extends BasePostprocessor {


    // 宽高比超过3则认为是长图或者宽图
    private static final float NORMAL_MAX_RATIO = 3.0f;
    private static final int MIN_WIDTH_HEIGHT = 214;
    private int[] mWidthAndHeight;

    public PicFeedsPostprocessor(int[] widthAndHeight) {
        mWidthAndHeight = widthAndHeight;
    }

    @Override
    public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {
        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();

        // 判断当前加载的图片是否为长图，若为长图，则需要截取顶端的一部分显示在view上
        boolean isLongPic = isLongPic(width, height);
        if (isLongPic && mWidthAndHeight[0] >= 0) {
            height = mWidthAndHeight[1] * width / mWidthAndHeight[0];
        }

        // 通过fresco提供的方法来创建bitmap
        CloseableReference<Bitmap> bitmapRef = bitmapFactory.createBitmap(width, height);
        try {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            Rect rect = new Rect(0, 0, width, height);

            Bitmap destBitmap = bitmapRef.get();
            destBitmap.setHasAlpha(true);

            Canvas canvas = new Canvas(destBitmap);
            canvas.drawBitmap(sourceBitmap, rect, rect, paint);
            return CloseableReference.cloneOrNull(bitmapRef);
        } finally {
            CloseableReference.closeSafely(bitmapRef);
        }

    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Nullable
    @Override
    public CacheKey getPostprocessorCacheKey() {
        return new SimpleCacheKey(getName());
    }


    /**
     * 用来判断当前加载的图片是否是长图片
     * = =不知道依据什么来判断，默认图片高度是宽度的三倍就默认为长图片
     *
     * @param width
     * @param height
     * @return
     */
    private boolean isLongPic(int width, int height) {
        if (width == 0 || height == 0) {
            return false;
        }
        float ratio = ((float) height) / width;
        return ratio >= NORMAL_MAX_RATIO;
    }

}
