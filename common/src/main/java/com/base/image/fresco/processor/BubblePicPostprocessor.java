package com.base.image.fresco.processor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.facebook.cache.common.CacheKey;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;

/**
 * Created by chengsimin on 15-11-12.
 */

public class BubblePicPostprocessor extends BasePostprocessor {
    public static final String TAG = "BubblePicPostProcessor";

    public static final int LEFT = 1;
    public static final int RIHGT = 2;

    private int mOrientation = LEFT;//方向
    private int mRoundPx = 0;// 圆角程度
    private float mArrowLengthFactor = 0.1f;// 箭头边长
    private float mArrowHeightFactor = 0.1f;// 箭头高
    private int[] mWidthAndHeight; // view的宽高

    private float mRatio = 2.0f;

    public BubblePicPostprocessor(int orientation, int roundPx, int arrowLengthFactor, int arrowHeightFactor, int arrowTopFactor) {
        this.mOrientation = orientation;
        this.mRoundPx = roundPx;
        this.mArrowLengthFactor = arrowLengthFactor;
        this.mArrowHeightFactor = arrowHeightFactor;
    }

    public BubblePicPostprocessor(int orientation, int roundPx, int[] widthAndHeight) {
        this.mOrientation = orientation;
        this.mRoundPx = roundPx;
        this.mWidthAndHeight = widthAndHeight;
    }

    public void setLongPicRatio(float ratio) {
        mRatio = ratio;
    }

    /**
     * 换了一种处理图片的方式
     * 由于之前的process方法生成的bitmap和原图大小一样，没办法实现加载长图时候只截取上半部分显示的目的
     * 在这里用另外一个process方法，手动生成一张能控制大小的bitmap
     * 能满足长图片上截取前一部分的功能
     * 之前的process方法在下面注释掉了，如后期发现问题再恢复或者调整
     * <p>
     * 在这里面bitmap不要直接用android提供的构造方法，在5.0之前的系统中,fresco的bitmap存在native内存中的，自己new了无法管理，需要统一用fresco提供的方法
     *
     * @param sourceBitmap  源图片
     * @param bitmapFactory 需要用到的构造bitmap的工厂
     * @return
     */
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
            Bitmap destBitmap = bitmapRef.get();

            //根据具体view大小以及实际bitmap大小来动态的计算箭头大小
            mArrowHeightFactor = (float) width / mWidthAndHeight[0];
            mArrowLengthFactor = (float) height / mWidthAndHeight[1];

            int arrowLength = (int) (DisplayUtils.dip2px(32.0f / 3) * mArrowLengthFactor);
            int arrowHeight = (int) (DisplayUtils.dip2px(16.0f / 3) * mArrowHeightFactor);
            int arrowMid = (int) (DisplayUtils.dip2px(33.33f / 2) * mArrowLengthFactor);

            MyLog.d(TAG, "orientation:" + mOrientation + ",roundPx:" + mRoundPx + ",arrowLength:" + arrowLength + ",arrowHeight:" + arrowHeight + ",arrowMid:" + arrowMid);
            MyLog.d(TAG, "input.getWidth:" + width + ",input.getHeight:" + height + ",output.getWidth:" + destBitmap.getWidth() + ",output.getHeight:" + destBitmap.getHeight());
            // fresco默认alpha通道是关的,这里一定要打开，不然会有黑边
            destBitmap.setHasAlpha(true);
            Canvas canvas = new Canvas(destBitmap);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, width, height);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);

            Path path = new Path();

            // 根据当前聊天气泡框的方向来画尖角
            if (mOrientation == RIHGT) {
                Rect rectC = new Rect(0, 0, width - arrowHeight, height);
                RectF rectF = new RectF(rectC);
                canvas.drawRoundRect(rectF, mRoundPx, mRoundPx, paint);

                path.moveTo(width - arrowHeight, arrowMid - arrowLength / 2);
                path.lineTo(width, arrowMid);
                path.lineTo(width - arrowHeight, arrowMid + arrowLength / 2);
                path.lineTo(width - arrowHeight, arrowMid - arrowLength / 2);
            }
            if (mOrientation == LEFT) {
                Rect rectC = new Rect(arrowHeight, 0, width, height);
                RectF rectF = new RectF(rectC);
                canvas.drawRoundRect(rectF, mRoundPx, mRoundPx, paint);

                path.moveTo(arrowHeight, arrowMid - arrowLength / 2);
                path.lineTo(0, arrowMid);
                path.lineTo(arrowHeight, arrowMid + arrowLength / 2);
                path.lineTo(arrowHeight, arrowMid - arrowLength / 2);
            }

            canvas.drawPath(path, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(sourceBitmap, rect, rect, paint);

            return CloseableReference.cloneOrNull(bitmapRef);
        } finally {
            CloseableReference.closeSafely(bitmapRef);
        }
    }

    /**
     * 这段方法是之前气泡尖角框的处理
     * 替换成了上面的处理方法,为了兼容长图片
     *
     * @return
     */
//    @Override
//    public void process(Bitmap output, Bitmap input) {
//        int width = input.getWidth();
//        int height = input.getHeight();
//
//        // 判断当前加载的图片是否为长图，若为长图，则需要截取顶端的一部分显示在view上
//
//        MyLog.e("dada", "width : " + width + " height : " + height);
//
//        boolean isLongPic = isLongPic(width, height);
//        if (isLongPic && mWidthAndHeight[0] >= 0) {
//            height = mWidthAndHeight[1] * width / mWidthAndHeight[0];
//            MyLog.e("dada", "long height : " + height);
//        }
//
//        //根据具体view大小以及实际bitmap大小来动态的计算箭头大小
//
//        mArrowHeightFactor = (float)width / mWidthAndHeight[0];
//        mArrowLengthFactor = (float)height / mWidthAndHeight[1];
//
//        int arrowLength = (int) (DisplayUtils.dip2px(32.0f / 3) * mArrowLengthFactor);
//        int arrowHeight = (int) (DisplayUtils.dip2px(16.0f / 3) * mArrowHeightFactor);
//        int arrowMid = (int) (DisplayUtils.dip2px(33.33f / 2) * mArrowLengthFactor);
//
//        MyLog.d(TAG, "orientation:" + mOrientation + ",roundPx:" + mRoundPx + ",arrowLength:" + arrowLength + ",arrowHeight:" + arrowHeight + ",arrowMid:" + arrowMid);
//        MyLog.d(TAG, "input.getWidth:" + width + ",input.getHeight:" + height + ",output.getWidth:" + output.getWidth() + ",output.getHeight:" + output.getHeight());
//        // fresco默认alpha通道是关的,这里一定要打开，不然会有黑边
//        output.setHasAlpha(true);
//        Canvas canvas = new Canvas(output);
//
//        final int color = 0xff424242;
//        final Paint paint = new Paint();
//        final Rect rect = new Rect(0, 0, width, height);
//
//        paint.setAntiAlias(true);
//        canvas.drawARGB(0, 0, 0, 0);
//        paint.setColor(color);
//
//        Path path = new Path();
//
//        if (mOrientation == RIHGT) {
//            Rect rectC = new Rect(0, 0, width - arrowHeight, height);
//            RectF rectF = new RectF(rectC);
//            canvas.drawRoundRect(rectF, mRoundPx, mRoundPx, paint);
//
//            path.moveTo(width - arrowHeight, arrowMid - arrowLength / 2);
//            path.lineTo(width, arrowMid);
////            path.lineTo(width - 2, arrowMid - 2);
////            path.lineTo(width - 2, arrowMid + 2);
//            path.lineTo(width - arrowHeight, arrowMid + arrowLength / 2);
//            path.lineTo(width - arrowHeight, arrowMid - arrowLength / 2);
//        }
//        if (mOrientation == LEFT) {
//            Rect rectC = new Rect(arrowHeight, 0, width, height);
//            RectF rectF = new RectF(rectC);
//            canvas.drawRoundRect(rectF, mRoundPx, mRoundPx, paint);
//
//            path.moveTo(arrowHeight, arrowMid - arrowLength / 2);
//            path.lineTo(0, arrowMid);
////            path.lineTo(2, arrowMid - 2);
////            path.lineTo(2, arrowMid + 2);
//            path.lineTo(arrowHeight, arrowMid + arrowLength / 2);
//            path.lineTo(arrowHeight, arrowMid - arrowLength / 2);
//        }
//
//        canvas.drawPath(path, paint);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//        canvas.drawBitmap(input, rect, rect, paint);
//    }
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public CacheKey getPostprocessorCacheKey() {
//        return new SimpleCacheKey(getName() + "&radius=" + mRadius);
        return new SimpleCacheKey(getName());
    }

    /**
     * 用来判断当前加载的图片是否是长图片
     * = =不知道依据什么来判断，默认图片高度是宽度的2倍就默认为长图片
     */
    private boolean isLongPic(int width, int height) {
        if (width == 0) {
            return false;
        }
        float ratio = height / width;
        return ratio >= mRatio;
    }
}