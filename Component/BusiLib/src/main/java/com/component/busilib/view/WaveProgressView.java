package com.component.busilib.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import com.component.busilib.R;

public class WaveProgressView extends View {
    /**
     * View的宽高
     */
    private int width;
    private int height;
    /**
     * 图片背景
     */
    private Bitmap backgroundInnerBitmap;   // 底部背景
    private Bitmap backgroundOuterBitmap;   // 顶部背景
    /**
     * 绘制波纹
     */
    private Path mPath;
    private Paint mPathPaint;
    private Path mSecondPath;
    private Paint mSecondPaint;
    /**
     * 波浪参数
     */
    private float mWaveHight = 10f;    // 波峰高度
    private float mWaveHalfWidth = 16f;     // 波峰的宽带
    private String mWaveColor = "#FFB1B1";      // 第一个波峰的颜色
    private String mSecondWaveColor = "#FF8484";  // 第二个波峰的颜色
    private int mWaveSpeed = 30;   // 波峰移动的速度
    private float mWaveDiff = 8f;  // 两个波峰之前的距离
    /**
     * 绘制进度
     */
    private Paint mTextPaint;
    private String currentText = "";
    private String mTextColor = "#FFFFFF";
    private int mTextSize = 41;
    /**
     * 默认进度值
     */
    private int maxProgress = 100;
    private int currentProgress = 0;
    private float CurY;
    /**
     * 动画位移距离
     */
    private float distance = 0;
    private int RefreshGap = 10;
    /**
     * 让波浪动起来
     */
    private static final int INVALIDATE = 0X777;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case INVALIDATE:
                    invalidate();
                    sendEmptyMessageDelayed(INVALIDATE, RefreshGap);
                    break;
            }
        }
    };

    /**
     * 构造函数
     */
    public WaveProgressView(Context context) {
        this(context, null, 0);
    }

    public WaveProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        /**
         * 获得参数，背景
         */
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveProgressView);
        backgroundInnerBitmap = getBitmapFromDrawable(typedArray.getDrawable(R.styleable.WaveProgressView_innerDrawable));
        backgroundOuterBitmap = getBitmapFromDrawable(typedArray.getDrawable(R.styleable.WaveProgressView_outerDrawable));
        typedArray.recycle();
        init();
    }

    /**
     * @param currentProgress 当前进度
     * @param currentText     当前显示的进度文字
     */
    public void setCurrent(int currentProgress, String currentText) {
        this.currentProgress = currentProgress;
        this.currentText = currentText;
    }

    /**
     * @param maxProgress 设置进度条的最大值，默认100
     */
    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    /**
     * @param mTextColor 文字的颜色
     * @param mTextSize  文字的大小
     */
    public void setText(String mTextColor, int mTextSize) {
        this.mTextColor = mTextColor;
        this.mTextSize = mTextSize;
    }

    /**
     * @param mWaveHight 波峰的高度
     * @param mWaveWidth 一个波峰的宽度
     */
    public void setWave(float mWaveHight, float mWaveWidth) {
        this.mWaveHight = mWaveHight;
        this.mWaveHalfWidth = mWaveWidth / 2;
    }

    /**
     * @param mWaveColor 水的颜色
     */
    public void setWaveColor(String mWaveColor) {
        this.mWaveColor = mWaveColor;
    }

    /**
     * 值越大震荡的越慢
     *
     * @param mWaveSpeed
     */
    public void setmWaveSpeed(int mWaveSpeed) {
        this.mWaveSpeed = mWaveSpeed;
    }

    private void init() {
        /**
         * 波浪画笔
         */
        mPath = new Path();
        mPathPaint = new Paint();
        mPathPaint.setAntiAlias(true);
        mPathPaint.setStyle(Paint.Style.FILL);

        mSecondPath = new Path();
        mSecondPaint = new Paint();
        mSecondPaint.setAntiAlias(true);
        mSecondPaint.setStyle(Paint.Style.FILL);
        /**
         * 进度画笔
         */
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        handler.sendEmptyMessageDelayed(INVALIDATE, 100);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /**
         * 获得控件的宽高，默认MeasureSpec.EXACTLY（match_parent,accurate ）
         * 并且布局文件中应该设置 控件的宽高相等
         */
        width = MeasureSpec.getSize(widthMeasureSpec);
        CurY = height = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (backgroundInnerBitmap != null) {
            /**
             * 把最后的bitmap画上去
             */
            canvas.drawBitmap(createImage(), 0, 0, null);
        }
    }

    private Bitmap createImage() {
        mPathPaint.setColor(Color.parseColor(mWaveColor));
        mSecondPaint.setColor(Color.parseColor(mSecondWaveColor));
        mTextPaint.setColor(Color.parseColor(mTextColor));
        mTextPaint.setTextSize(mTextSize);
        mWaveDiff = mWaveHalfWidth * 1.5f;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap finalBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        /**
         * 产生一个同样大小的画布
         */
        Canvas canvas = new Canvas(finalBmp);
        /**
         * 绘制波浪
         */
        float CurMidY = height * (maxProgress - currentProgress) / maxProgress;
        if (Math.abs(CurY - CurMidY) > 0.01f) {
            CurY = CurMidY;
        }
//        if (CurY > CurMidY) {
//            CurY = CurY - (CurY - CurMidY) / 10;
//        }
        mPath.reset();
        mPath.moveTo(0 - distance, CurY);
        mSecondPath.reset();
        mSecondPath.moveTo(0 - distance - mWaveDiff, CurY);

        int waveNum = width / ((int) mWaveHalfWidth * 4) + 1;
        int multiplier = 0;
        for (int i = 0; i < waveNum * 3; i++) {
            mPath.quadTo(mWaveHalfWidth * (multiplier + 1) - distance, CurY - mWaveHight, mWaveHalfWidth * (multiplier + 2) - distance, CurY);
            mPath.quadTo(mWaveHalfWidth * (multiplier + 3) - distance, CurY + mWaveHight, mWaveHalfWidth * (multiplier + 4) - distance, CurY);
            mSecondPath.quadTo(mWaveHalfWidth * (multiplier + 1) - distance - mWaveDiff, CurY - mWaveHight, mWaveHalfWidth * (multiplier + 2) - distance - mWaveDiff, CurY);
            mSecondPath.quadTo(mWaveHalfWidth * (multiplier + 3) - distance - mWaveDiff, CurY + mWaveHight, mWaveHalfWidth * (multiplier + 4) - distance - mWaveDiff, CurY);
            multiplier += 4;
        }


        mPath.lineTo(width, height);
        mPath.lineTo(0, height);
        mPath.close();
        canvas.drawPath(mPath, mPathPaint);

        mSecondPath.lineTo(width, height);
        mSecondPath.lineTo(0, height);
        mSecondPath.close();
        canvas.drawPath(mSecondPath, mSecondPaint);

        distance += mWaveHalfWidth / mWaveSpeed;
        distance = distance % (mWaveHalfWidth * 4);

//        /**
//         * 对图片给进行缩放
//         */
//        int min = Math.min(width, height);
//        backgroundInnerBitmap = Bitmap.createScaledBitmap(backgroundInnerBitmap, min, min, false);
        /**
         * 使用DST_ATOP，取上层非交集部分与下层交集部分 。
         */
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
        /**
         * 绘制底部图片
         */
        canvas.drawBitmap(backgroundInnerBitmap, 0, 0, paint);
        /**
         * 绘制进度文字
         */
        canvas.drawText(currentText, width / 2, height / 2, mTextPaint);
        /**
         * 绘制顶部图片
         */
        paint.setXfermode(null);
        canvas.drawBitmap(backgroundOuterBitmap, 0, 0, paint);
        return finalBmp;
    }

    /**
     * Drawable转Bitmap
     */
    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        try {
            Bitmap bitmap;
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }
}

