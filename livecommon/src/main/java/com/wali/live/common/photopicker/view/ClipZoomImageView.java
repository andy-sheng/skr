package com.wali.live.common.photopicker.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;

/**
 * 缩放图片的View
 * Create by zhangyuehuan 2016/6/15
 */
public class ClipZoomImageView extends ImageView implements
        OnScaleGestureListener, OnTouchListener,
        ViewTreeObserver.OnGlobalLayoutListener {
    private static String TAG = ClipZoomImageView.class.getSimpleName();
    public static float SCALE_MAX = 4.0f;
    private static float SCALE_MID = 2.0f;
    /**
     * 初始化时的缩放比例
     */
    private float mInitScale = 1.0f;
    private boolean once = true;

    /**
     * 用于存放矩阵
     */
    private final float[] matrixValues = new float[9];

    /**
     * 缩放的手势检测
     */
    private ScaleGestureDetector mScaleGestureDetector = null;
    private final Matrix mScaleMatrix = new Matrix();
    private Matrix mPreMatrix = new Matrix();

    /**
     * 用于双击
     */
    private GestureDetector mGestureDetector;
    private boolean isAutoScale;

    private int mTouchSlop;

    private float mLastX;
    private float mLastY;

    private boolean isCanDrag;
    private int lastPointerCount;
    /**
     * 水平方向与View的边距
     */
    private int mHorizontalPadding = 0;

    private int mClipHeight;

    public ClipZoomImageView(Context context) {
        this(context, null);
    }

    public ClipZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ClipImageSize);
        mClipHeight = DisplayUtils.dip2px(a.getInt(R.styleable.ClipImageSize_clip_image_height, 0));
        a.recycle();

        setScaleType(ScaleType.MATRIX);
        mGestureDetector = new GestureDetector(context,
                new SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        if (isAutoScale == true)
                            return true;

                        float x = e.getX();
                        float y = e.getY();
                        if (getScale() < SCALE_MID) {
                            ClipZoomImageView.this.postDelayed(
                                    new AutoScaleRunnable(SCALE_MID, x, y), 16);
                            isAutoScale = true;
                        } else {
                            ClipZoomImageView.this.postDelayed(
                                    new AutoScaleRunnable(mInitScale, x, y), 16);
                            isAutoScale = true;
                        }

                        return true;
                    }
                });
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        this.setOnTouchListener(this);
    }

    /**
     * 自动缩放的任务
     */
    private class AutoScaleRunnable implements Runnable {
        static final float BIGGER = 1.07f;
        static final float SMALLER = 0.93f;
        private float mTargetScale;
        private float tmpScale;

        /**
         * 缩放的中点
         */
        private float x;
        private float y;

        /**
         * 传入目标缩放值，根据目标值与当前值，判断应该放大还是缩小
         *
         * @param targetScale
         */
        public AutoScaleRunnable(float targetScale, float x, float y) {
            this.mTargetScale = targetScale;
            this.x = x;
            this.y = y;
            if (getScale() < mTargetScale) {
                tmpScale = BIGGER;
            } else {
                tmpScale = SMALLER;
            }

        }

        @Override
        public void run() {
            mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
            checkBorderAndCenterWhenScale();
            //将矩阵作用到图片上
            setImageMatrix(mScaleMatrix);
            //得到当前图片的缩放值
            float currentScale = getScale();
            if ((tmpScale > 1.0f) && currentScale < mTargetScale
                    || (tmpScale < 1.0f) && currentScale > mTargetScale) {
                //每隔16ms就调用一次run方法
                postDelayed(this, 16);
            } else {
                //保证图片最终的缩放值和目标缩放值一致
                float scale = mTargetScale / currentScale;
                mScaleMatrix.postScale(scale, scale, x, y);
                checkBorderAndCenterWhenScale();
                setImageMatrix(mScaleMatrix);
                //自动缩放结束，置为false
                isAutoScale = false;
            }
        }
    }


    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Log.w(TAG, "onScale");
        float scaleFactor = detector.getScaleFactor();

        if (getDrawable() == null) {
            return true;
        }
        /**
         * 设置缩放比例
         */
        mScaleMatrix.postScale(scaleFactor, scaleFactor,
                detector.getFocusX(), detector.getFocusY());
        checkBorderAndCenterWhenScale();
        setImageMatrix(mScaleMatrix);
        return true;
    }

    /**
     * 根据当前图片的Matrix获得图片的范围
     *
     * @return
     */
    private RectF getMatrixRectF() {
        Matrix matrix = mScaleMatrix;
        RectF rect = new RectF();
        Drawable d = getDrawable();
        if (null != d) {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rect);
        }
        return rect;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        Log.w(TAG, "onScaleBegin");
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        Log.w(TAG, "onScaleEnd");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.w(TAG, "onTouch event.getAction =" + event.getAction());
        if (mGestureDetector.onTouchEvent(event))
            return true;
        mScaleGestureDetector.onTouchEvent(event);

        float x = 0, y = 0;
        float dx = 0, dy = 0;
        // 拿到触摸点的个数
        final int pointerCount = event.getPointerCount();
        // 得到多个触摸点的x与y均值
        for (int i = 0; i < pointerCount; i++) {
            x += event.getX(i);
            y += event.getY(i);
        }
        x = x / pointerCount;
        y = y / pointerCount;

        /**
         * 每当触摸点发生变化时，重置mLasX , mLastY
         */
        if (pointerCount != lastPointerCount) {
            isCanDrag = false;
            mLastX = x;
            mLastY = y;
        }

        lastPointerCount = pointerCount;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPreMatrix = mScaleMatrix;
                break;
            case MotionEvent.ACTION_MOVE:

                dx = x - mLastX;
                dy = y - mLastY;

                if (!isCanDrag) {
                    isCanDrag = isCanDrag(dx, dy);
                }
                Log.w(TAG, "isCanDrag =" + isCanDrag);
                if (isCanDrag) {
                    if (getDrawable() != null) {
                        mScaleMatrix.postTranslate(dx, dy);
                        setImageMatrix(mScaleMatrix);
                    }
                }
                mLastX = x;
                mLastY = y;
                break;

            case MotionEvent.ACTION_UP:
                //如果当前图片大小小于初始化大小
                if (getScale() < mInitScale) {
                    //自动放大至初始化大小
                    post(new AutoScaleRunnable(mInitScale, getWidth() / 2, getHeight() / 2));
                }
                //如果当前图片大小大于最大值
                if (getScale() > SCALE_MAX) {
                    //自动缩小至最大值
                    post(new AutoScaleRunnable(SCALE_MAX, getWidth() / 2, getHeight() / 2));
                }

                Log.w(TAG, "isCandrag =" + isCanDrag);
                if (isCanDrag) {//如果当前可以滑动
                    dx = x - mLastX;
                    dy = y - mLastY;
                    RectF rectF = getMatrixRectF();
                    if (rectF.width() <= getWidth() - mHorizontalPadding * 2) {
                        dx = 0;
                    }
                    // 如果高度小雨屏幕高度，则禁止上下移动
                    if (rectF.height() <= getHeight() - getHVerticalPadding() * 2) {
                        dy = 0;
                    }
                    Log.w(TAG, "dx=" + dx + "dy=" + dy);
                    mScaleMatrix.postTranslate(dx, dy);
                    checkBorderWhenTranslate();
                    setImageMatrix(mScaleMatrix);
                }
                lastPointerCount = 0;
                break;
            case MotionEvent.ACTION_CANCEL:
                lastPointerCount = 0;
                break;
        }

        return true;
    }

    /**
     * 获得当前的缩放比例
     *
     * @return
     */
    public final float getScale() {
        mScaleMatrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        Log.w(TAG, "onGlobalLayout");
        if (once) {
            Drawable d = getDrawable();
            if (d == null)
                return;

            int width = getWidth();
            int height = getHeight();
            // 拿到图片的宽和高
            int drawableW = d.getIntrinsicWidth();
            int drawableH = d.getIntrinsicHeight();
            float scale = 1.0f;

            int frameSize = getWidth() - mHorizontalPadding * 2;

            // 大图
            if (drawableW > frameSize && drawableH < frameSize) {
                scale = 1.0f * frameSize / drawableH;
            } else if (drawableH > frameSize && drawableW < frameSize) {
                scale = 1.0f * frameSize / drawableW;
            } else if (drawableW > frameSize && drawableH > frameSize) {
                float scaleW = frameSize * 1.0f / drawableW;
                float scaleH = frameSize * 1.0f / drawableH;
                scale = Math.max(scaleW, scaleH);
            }

            // 太小的图片放大处理
            if (drawableW < frameSize && drawableH > frameSize) {
                scale = 1.0f * frameSize / drawableW;
            } else if (drawableH < frameSize && drawableW >= frameSize) {
                scale = 1.0f * frameSize / drawableH;
            } else if (drawableW < frameSize && drawableH < frameSize) {
                float scaleW = 1.0f * frameSize / drawableW;
                float scaleH = 1.0f * frameSize / drawableH;
                scale = Math.max(scaleW, scaleH);
            }

            Log.w(TAG, "drawableW = " + drawableW + "drawableH =" + drawableH
                    + " frameSize =" + frameSize + " scale=" + scale);
            mInitScale = scale;
            SCALE_MID = mInitScale * 2;
            SCALE_MAX = mInitScale * 4;
            mScaleMatrix.postTranslate((width - drawableW) / 2,
                    (height - drawableH) / 2);
            mScaleMatrix.postScale(scale, scale, getWidth() / 2,
                    getHeight() / 2);

            // 图片移动至屏幕中间
            checkBorderWhenTranslate();
            setImageMatrix(mScaleMatrix);
            once = false;
        }
    }

    /**
     * 剪切图片，返回剪切后的bitmap对象
     *
     * @return
     */
    public Bitmap clip() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return Bitmap.createBitmap(bitmap, mHorizontalPadding,
                getHVerticalPadding(), getWidth() - 2 * mHorizontalPadding,
                getHeight() - 2 * getHVerticalPadding());
    }

    /**
     * 当缩放时检查边界并且使图片居中
     */
    private void checkBorderAndCenterWhenScale() {
        if (getDrawable() == null) {
            return;
        }
        float deltaX = 0.0f;
        float deltaY = 0.0f;

        int width = getWidth();
        int height = getHeight();

        RectF rectF = getMatrixRectF();

        if (rectF.width() >= width) {

            //左边会出现一个小白边
            if (rectF.left > 0) {
                //我们将图片向左边移动
                deltaX = -rectF.left;
            }

            if (rectF.right < width) {
                //我们将图片向右边移动
                deltaX = width - rectF.right;
            }
        }
        //调整高度
        if (rectF.height() >= height) {
            //如果上面出现小白边，则向上移动
            if (rectF.top > 0) {
                deltaY = -rectF.top;
            }
            //如果下面出现小白边，则向下移动
            if (rectF.bottom < height) {
                deltaY = height - rectF.bottom;
            }
        }
        //水平的居中调整
        if (rectF.width() < width) {
            deltaX = width / 2f - rectF.right + rectF.width() / 2f;
        }

        //竖直方向的居中调整
        if (rectF.height() < height) {
            deltaY = height / 2f - rectF.bottom + rectF.height() / 2f;
        }
        //将平移的偏移量作用到矩阵上
        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 平移边界检查
     */
    private void checkBorderWhenTranslate() {
        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        // 如果宽或高大于屏幕，则控制范围
        if (rect.width() + 0.01 >= width - 2 * mHorizontalPadding) {
            if (rect.left >= mHorizontalPadding) {
                deltaX = -rect.left + mHorizontalPadding;
            }

            if (rect.right <= width - mHorizontalPadding) {
                deltaX = width - mHorizontalPadding - rect.right;
            }
        }

        if (rect.height() + 0.01 >= height - 2 * getHVerticalPadding()) {
            if (rect.top >= getHVerticalPadding()) {
                deltaY = -rect.top + getHVerticalPadding();
            }

            if (rect.bottom <= height - getHVerticalPadding()) {
                deltaY = height - getHVerticalPadding() - rect.bottom;
            }
        }

        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 是否是拖动行为
     *
     * @param dx
     * @param dy
     * @return
     */
    private boolean isCanDrag(float dx, float dy) {
        return Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
    }

    private int getHVerticalPadding() {
        if (mClipHeight > 0) {
            return (getHeight() - mClipHeight) / 2;
        } else {
            return (getHeight() - (getWidth() - 2 * mHorizontalPadding)) / 2;
        }
    }

    public void setClipHeight(int height){
        this.mClipHeight = height;
        invalidate();
    }
}
