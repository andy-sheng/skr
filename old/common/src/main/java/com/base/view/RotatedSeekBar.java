package com.base.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.base.common.R;
import com.base.log.MyLog;

public class RotatedSeekBar extends View implements View.OnTouchListener {
    private static final String TAG = RotatedSeekBar.class.getSimpleName();

    private static final int RECT_FT = 0;
    private static final int RECT_BK = 1;
    private static final int RECT_BTN = 2;
    private static final int RECT_BTN_PRESS = 3;

    protected boolean isReplay = false;
    protected Paint mPaint;
    protected float mX, mY;                                                                           //绘制中心坐标
    protected float mSeekBarWidth = 693, mSeekBarHeight = 30, mThumbSize = mSeekBarHeight, mThumbSizePress = mThumbSize;            //0度 长边 短边 滑动btn大小
    protected float mSeekBarPaddingLeft, mSeekBarPaddingRight;
    protected Bitmap mBitmapFT, mBitmapBK, mThumb, mThumbPress;                                                    //前景图 背景图
    protected Rect mSrcRectFT = new Rect(),                                                           //前景图 裁剪bitmap区域
            mDstRectFT = new Rect(),                                                                //前景图 绘制bitmap区域
            mSrcRectBK = new Rect(),                                                                //背景图 裁剪bitmap区域
            mDstRectBK = new Rect(),                                                                //背景图 绘制bitmap区域
            mSrcRectBtn = new Rect(),                                                               //背景图 裁剪bitmap区域
            mDstRectBtn = new Rect(),                                                               //背景图 绘制bitmap区域
            mSrcRectBtnPress = new Rect(),
            mDstRectBtnPress = new Rect();

    protected boolean mIsPressed = false;
    protected float mDegree;                                                                          //seekbar 旋转的角度  正数为顺时针
    protected float mPercent, mPercentMax, mPercentMin;                                               //百分比

    protected int mShapeColorFT, mShapeColorBK;                                                     //TODO 后续优化 现在无法解析drawable shape 先work 日后优化跟seekbar一样
    protected float mShapeSize;
    protected float mBkAlpha;

    protected Drawable mThumbPressDrawable;
    protected Drawable mThumbDrawable;

    private OnRotatedSeekBarChangeListener mOnRotatedSeekBarChangeListener;

    public RotatedSeekBar(Context context) {
        this(context, null);
    }

    public RotatedSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotatedSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.RotatedSeekBar);
        Drawable drawable = typedArray.getDrawable(R.styleable.RotatedSeekBar_appProgressDrawableFT);
        mBitmapFT = drawable2Bitmap(drawable);
        drawable = typedArray.getDrawable(R.styleable.RotatedSeekBar_appProgressDrawableBK);
        mBitmapBK = drawable2Bitmap(drawable);

        mThumbDrawable = typedArray.getDrawable(R.styleable.RotatedSeekBar_appThumbDrawable);
        mThumb = drawable2Bitmap(mThumbDrawable);
        mThumbPressDrawable = typedArray.getDrawable(R.styleable.RotatedSeekBar_appThumbDrawablePress);
        mThumbPress = drawable2Bitmap(mThumbPressDrawable);

        drawable = null;
        mSeekBarPaddingLeft = typedArray.getDimension(R.styleable.RotatedSeekBar_appSeekBarLeftPadding, 0);
        mSeekBarPaddingRight = typedArray.getDimension(R.styleable.RotatedSeekBar_appSeekBarRightPadding, 0);
        mThumbSize = typedArray.getDimension(R.styleable.RotatedSeekBar_appThumbSize, 0);
        mThumbSizePress = typedArray.getDimension(R.styleable.RotatedSeekBar_appThumbSizePress, mThumbSize);
        mDegree = typedArray.getFloat(R.styleable.RotatedSeekBar_appRotatedAngle, 0);

        mPercentMin = typedArray.getFloat(R.styleable.RotatedSeekBar_appPercenteMin, 0);
        mPercentMax = typedArray.getFloat(R.styleable.RotatedSeekBar_appPercenteMax, 1);
        mPercent = typedArray.getFloat(R.styleable.RotatedSeekBar_appPercente, mPercentMin);

        mShapeColorFT = typedArray.getColor(R.styleable.RotatedSeekBar_appShapeColorFT, 0x000000);
        mShapeColorBK = typedArray.getColor(R.styleable.RotatedSeekBar_appShapeColorBK, 0x000000);
        mShapeSize = typedArray.getDimension(R.styleable.RotatedSeekBar_appShapeSize, -1f);
        mBkAlpha = typedArray.getFloat(R.styleable.RotatedSeekBar_appShapeBkAlpha, 255);
        typedArray.recycle();
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        MyLog.v(TAG, "onAttachedToWindow");
        init();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MyLog.v(TAG, "onDetachedFromWindow");
        release();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mX = measureSize(widthMeasureSpec, mSeekBarWidth) / 2;
        mY = measureSize(heightMeasureSpec, mSeekBarHeight) / 2;
        mSeekBarWidth = 2 * Math.max(mX, mY) - mSeekBarPaddingLeft - mSeekBarPaddingRight;
        if (mBitmapFT != null) {
            mSeekBarHeight = mSeekBarWidth * mBitmapFT.getHeight() / mBitmapFT.getWidth();
            if (mSeekBarHeight > 2 * Math.min(mX, mY)) {
                mSeekBarHeight = 2 * Math.min(mX, mY);
            }
            if (mThumbSize == 0) {
                mThumbSize = mSeekBarHeight;
                mThumbSizePress = mThumbSize;
            }
        }
        Log.v(TAG, " x=" + mX + " y=" + mY + " w=" + mSeekBarWidth + " h=" + mSeekBarHeight);
        setDstRect(mDstRectFT, mX, mY, mPercent, RECT_FT);
        setDstRect(mDstRectBK, mX, mY, mPercent, RECT_BK);
        float btnX = mX + cosd(mDegree) * (mPercent - 0.5f) * mSeekBarWidth;
        float btnY = mY + sind(mDegree) * (mPercent - 0.5f) * mSeekBarWidth;
        setDstRect(mDstRectBtn, btnX, btnY, mPercent, RECT_BTN);
        setDstRect(mDstRectBtnPress, btnX, btnY, mPercent, RECT_BTN_PRESS);
    }

    private void init() {
        MyLog.d(TAG, "init hash code=" + this.hashCode());
        mPaint = new Paint();
        setOnTouchListener(this);
        //mBitmapFT = BitmapFactory.decodeResource(getResources(), R.drawable.live_anchor_list_beauty_progress);
        //mBitmapBK = BitmapFactory.decodeResource(getResources(), R.drawable.live_anchor_list_beauty_progress_bg);
        setAllRect();
        if (mThumb == null) {
            mThumb = drawable2Bitmap(mThumbDrawable);
        }
        if (mThumbPress == null) {
            mThumbPress = drawable2Bitmap(mThumbPressDrawable);
        }
    }

    private void release() {
        MyLog.d(TAG, "release hash code=" + this.hashCode());
//        if (mBitmapFT != null && !mBitmapFT.isRecycled()) {
//            //mBitmapFT.recycle();
//        }
//        mBitmapFT = null;
//        if (mBitmapBK != null && !mBitmapBK.isRecycled()) {
//            //mBitmapBK.recycle();
//        }
//        mBitmapBK = null;
    }

    public void setOnRotatedSeekBarChangeListener(OnRotatedSeekBarChangeListener listener) {
        mOnRotatedSeekBarChangeListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isReplay) {
            canvas.save();
            canvas.rotate(mDegree, mX, mY);

            //TODO mEnabled=false 暫時背景色沒有顏色 所以mEnabled = false状态先隐藏前景色 paint该透明度 后续优化

            if (!isEnabled()) {
                mPaint.setAlpha(128);
            } else {
                mPaint.setAlpha(255);
            }

            if (mBitmapBK != null) {
                canvas.drawBitmap(mBitmapBK, mSrcRectBK, mDstRectBK, mPaint);
            } else if (mShapeSize >= 0) {
                mPaint.setColor(mShapeColorBK);
                if (!isEnabled()) {
                    mPaint.setAlpha(128);
                } else {
                    mPaint.setAlpha(255);
                }
                mPaint.setAlpha((int) mBkAlpha);
                mPaint.setStrokeWidth(mShapeSize);
                canvas.drawLine(mDstRectFT.left, mY, mDstRectBK.right, mY, mPaint);
            }

            if (mBitmapFT != null && isEnabled()) {
                canvas.drawBitmap(mBitmapFT, mSrcRectFT, mDstRectFT, mPaint);
            } else if (mShapeSize >= 0 && isEnabled()) {
                mPaint.setColor(mShapeColorFT);
                mPaint.setStrokeWidth(mShapeSize);
                canvas.drawLine(mDstRectFT.left, mY, mDstRectFT.right, mY, mPaint);
            }

            canvas.restore();
            if (mThumb != null) {
                if (mThumbPress != null && mIsPressed) {
                    canvas.drawBitmap(mThumbPress, mSrcRectBtnPress, mDstRectBtnPress, mPaint);
                } else {
                    canvas.drawBitmap(mThumb, mSrcRectBtn, mDstRectBtn, mPaint);
                }
            }
        }
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        mIsPressed = pressed;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setPercent(event.getX(), event.getY());
                if (mOnRotatedSeekBarChangeListener != null) {
                    mOnRotatedSeekBarChangeListener.onStartTrackingTouch(this);
                }
                mIsPressed = true;
                break;
            case MotionEvent.ACTION_MOVE:
                setPercent(event.getX(), event.getY());
                if (mOnRotatedSeekBarChangeListener != null) {
                    mOnRotatedSeekBarChangeListener.onProgressChanged(this, mPercent, true);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setPercent(event.getX(), event.getY());
                if (mOnRotatedSeekBarChangeListener != null) {
                    mOnRotatedSeekBarChangeListener.onProgressChanged(this, mPercent, true);
                    mOnRotatedSeekBarChangeListener.onStopTrackingTouch(this);
                }
                mIsPressed = false;
                break;
        }
        invalidate();
        return true;
    }

    private void setPercent(float x, float y) {
        float dx = x - mX;
        float dy = y - mY;
        float r = (float) Math.sqrt(dx * dx + dy * dy);
        float si = atan2d(dy, dx) - mDegree;
        float result = 0.5f + r * cosd(si) / mSeekBarWidth;
        Log.v(TAG, " result=" + result + " cosd(si)=" + cosd(si));
        if (result < mPercentMin) {
            result = mPercentMin;
        }
        if (result > mPercentMax) {
            result = mPercentMax;
        }
        mPercent = result;
        setAllRect();
    }

    public void setPercent(float p) {
        Log.v(TAG, "setPercent");
        if (p < mPercentMin) {
            p = mPercentMin;
        }
        if (p > mPercentMax) {
            p = mPercentMax;
        }
        mPercent = p;
        if (mOnRotatedSeekBarChangeListener != null) {
            mOnRotatedSeekBarChangeListener.onProgressChanged(this, mPercent, false);
        }
        setAllRect();
        invalidate();
    }

    public float getPercent() {
        return mPercent;
    }

    public float getMaxPercent() {
        return mPercentMax;
    }

    public float getMinPercent() {
        return mPercentMin;
    }

    private void setAllRect() {
        setDstRect(mDstRectFT, mX, mY, mPercent, RECT_FT);
        setSrcRect(mSrcRectFT, mBitmapFT, mPercent, RECT_FT);
        setDstRect(mDstRectBK, mX, mY, mPercent, RECT_BK);
        setSrcRect(mSrcRectBK, mBitmapBK, mPercent, RECT_BK);
        float btnX = mX + cosd(mDegree) * (mPercent - 0.5f) * mSeekBarWidth;
        float btnY = mY + sind(mDegree) * (mPercent - 0.5f) * mSeekBarWidth;
        setSrcRect(mSrcRectBtn, mThumb, mPercent, RECT_BTN);
        setDstRect(mDstRectBtn, btnX, btnY, mPercent, RECT_BTN);
        setSrcRect(mSrcRectBtnPress, mThumbPress, mPercent, RECT_BTN_PRESS);
        setDstRect(mDstRectBtnPress, btnX, btnY, mPercent, RECT_BTN_PRESS);
    }

    private void setDstRect(Rect rect, float x, float y, float percent, int rectType) {
        if (percent < 0) {
            percent = 0;
        }
        if (percent > 1) {
            percent = 1;
        }
        if (rect == null) {
            return;
        }

        switch (rectType) {
            case RECT_FT:
                rect.left = (int) (x - mSeekBarWidth / 2);
                rect.top = (int) (y - mSeekBarHeight / 2);
                rect.right = (int) (x + mSeekBarWidth * (percent - 0.5f));
                rect.bottom = (int) (y + mSeekBarHeight / 2);
                break;
            case RECT_BK:
                rect.left = (int) (x + mSeekBarWidth * (percent - 0.5f));
                rect.top = (int) (y - mSeekBarHeight / 2);
                rect.right = (int) (x + mSeekBarWidth / 2);
                rect.bottom = (int) (y + mSeekBarHeight / 2);
                break;
            case RECT_BTN:
                rect.left = (int) (x - mThumbSize / 2);
                rect.top = (int) (y - mThumbSize / 2);
                rect.right = (int) (x + mThumbSize / 2);
                rect.bottom = (int) (y + mThumbSize / 2);
                break;
            case RECT_BTN_PRESS:
                rect.left = (int) (x - mThumbSizePress / 2);
                rect.top = (int) (y - mThumbSizePress / 2);
                rect.right = (int) (x + mThumbSizePress / 2);
                rect.bottom = (int) (y + mThumbSizePress / 2);
                break;
        }
    }

    private void setSrcRect(Rect rect, Bitmap bitmap, float percent, int rectType) {
        if (percent < 0) {
            percent = 0;
        }
        if (percent > 1) {
            percent = 1;
        }
        if (rect == null || bitmap == null) {
            return;
        }
        switch (rectType) {
            case RECT_FT:
                rect.left = 0;
                rect.top = 0;
                rect.right = (int) (bitmap.getWidth() * percent);
                rect.bottom = bitmap.getHeight();
                break;
            case RECT_BK:
                rect.left = (int) (bitmap.getWidth() * percent);
                rect.top = 0;
                rect.right = bitmap.getWidth();
                rect.bottom = bitmap.getHeight();
                break;
            case RECT_BTN:
                rect.left = 0;
                rect.top = 0;
                rect.right = bitmap.getWidth();
                rect.bottom = bitmap.getHeight();
                break;
            case RECT_BTN_PRESS:
                rect.left = 0;
                rect.top = 0;
                rect.right = bitmap.getWidth();
                rect.bottom = bitmap.getHeight();
                break;
        }
    }

    private float measureSize(int size, float defaultSize) {
        int specMode = MeasureSpec.getMode(size);
        int specSize = MeasureSpec.getSize(size);
        if (specMode == MeasureSpec.AT_MOST || specMode == MeasureSpec.EXACTLY) {
            return specSize;
        }
        return defaultSize;
    }

    private float sind(double degree) {
        return (float) Math.sin(Math.toRadians(degree));
    }

    private float cosd(double degree) {
        return (float) Math.cos(Math.toRadians(degree));
    }

    private float atan2d(double dy, double dx) {
        return (float) Math.toDegrees(Math.atan2(dy, dx));
    }

    private Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable == null) {

        } else if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
        return null;
    }


    public interface OnRotatedSeekBarChangeListener {
        void onProgressChanged(RotatedSeekBar rotatedSeekBar, float percent, boolean fromUser);

        void onStartTrackingTouch(RotatedSeekBar rotatedSeekBar);

        void onStopTrackingTouch(RotatedSeekBar rotatedSeekBar);
    }
}
