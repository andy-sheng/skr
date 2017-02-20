package com.base.image.fresco.processor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.base.log.MyLog;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.request.BasePostprocessor;

/**
 * Created by Star on 16/10/10.
 */
public class ScreenShotprocessor extends BasePostprocessor{
    private final static String TAG = ScreenShotprocessor.class.getSimpleName();
    @Override
    public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {

        int width = sourceBitmap.getWidth();
        int height = sourceBitmap.getHeight();
        MyLog.w(TAG, "source width : " + width + ", source height : " + height);
        Rect dstRect = width < height ? new Rect(0, 0, width, (int) (0.88 * height)) : new Rect(0, 0, width, height);
        CloseableReference<Bitmap> bitmapRef = bitmapFactory.createBitmap(dstRect.width(), dstRect.height());
        try {
            Bitmap destBitmap = bitmapRef.get();
            destBitmap.setHasAlpha(true);
            Canvas canvas = new Canvas(destBitmap);

            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            canvas.drawBitmap(sourceBitmap, dstRect, dstRect, paint);
            return CloseableReference.cloneOrNull(bitmapRef);
        }
        finally {
            CloseableReference.closeSafely(bitmapRef);
        }
    }
}