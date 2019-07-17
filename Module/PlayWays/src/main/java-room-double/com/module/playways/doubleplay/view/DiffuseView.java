package com.module.playways.doubleplay.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.module.playways.R;

import java.util.ArrayList;
import java.util.List;

public class DiffuseView extends View {

    /**
     * 扩散圆圈颜色
     */
    private int mColor = 0;
    /**
     * 圆圈中心颜色
     */
    private int mCoreColor = 0;
    /**
     * 圆圈中心图片
     */
    private Bitmap mNormalBitmap;
    private Bitmap mPressedBitmap;
    /**
     * 中心圆半径
     */
    private float mCoreRadius = 150;
    /**
     * 扩散圆宽度
     */
    private float mDiffuseRaduis = 3;
    /**
     * 最大宽度
     */
    private float mMaxRadius = 255;

    private float mStrokeWidth = 50;
    /**
     * 最大宽度
     */
    private float mSpeed = 1;
    /**
     * 是否正在扩散中
     */
    // 透明度集合
    private List<Integer> mAlphas = new ArrayList<>();
    // 扩散圆半径集合
    private List<Integer> mWidths = new ArrayList<>();
    private Paint mPaint;
    private boolean mPressing = false;
    private long mStopTs = System.currentTimeMillis();

    public DiffuseView(Context context) {
        this(context, null);
    }

    public DiffuseView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public DiffuseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DiffuseView, defStyleAttr, 0);
        mColor = a.getColor(R.styleable.DiffuseView_diffuse_color, mColor);
        mCoreColor = a.getColor(R.styleable.DiffuseView_diffuse_coreColor, mCoreColor);

        mCoreRadius = a.getDimension(R.styleable.DiffuseView_diffuse_coreRadius, mCoreRadius);
        mDiffuseRaduis = a.getDimension(R.styleable.DiffuseView_diffuse_addNewRadius, mDiffuseRaduis);
        mMaxRadius = a.getDimension(R.styleable.DiffuseView_diffuse_maxRadius, mMaxRadius);
        mStrokeWidth = a.getDimension(R.styleable.DiffuseView_diffuse_stroke_width, mStrokeWidth);

        mSpeed = a.getDimension(R.styleable.DiffuseView_diffuse_speed, mSpeed);
        int imageId = a.getResourceId(R.styleable.DiffuseView_diffuse_normalImage, -1);
        if (imageId != -1) {
            mNormalBitmap = BitmapFactory.decodeResource(getResources(), imageId);
            imageId = a.getResourceId(R.styleable.DiffuseView_diffuse_pressedImage, -1);
            if (imageId != -1) {
                mPressedBitmap = BitmapFactory.decodeResource(getResources(), imageId);
            }
        }
        a.recycle();
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        // 绘制扩散圆
        boolean going = System.currentTimeMillis() < mStopTs;
        if (mColor != 0) {
            mPaint.setColor(mColor);
            if (mWidths.isEmpty() && going) {
                mAlphas.add(255);
                mWidths.add(0);
            }
            for (int i = 0; i < mAlphas.size(); i++) {
                // 设置透明度
                int alpha = mAlphas.get(i);
                mPaint.setAlpha(alpha);
                // 绘制扩散圆
                int width = mWidths.get(i);
                float r = mCoreRadius + width;
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, r, mPaint);
                float t = width / ((mMaxRadius - mCoreRadius) * 1.0f);
                if (alpha > 0 && r < mMaxRadius) {
                    alpha = (int) ((1 - t) * 255);
                    if (alpha < 0) {
                        alpha = 0;
                    }
                    mAlphas.set(i, alpha);
                    mWidths.set(i, (int) (width + mSpeed));
                }
            }
            // 判断当扩散圆扩散到指定宽度时添加新扩散圆
            if (mWidths.size() > 0 && going) {
                float mr = mWidths.get(mWidths.size() - 1) + mCoreRadius;
                if (mr > mDiffuseRaduis) {
                    mAlphas.add(255);
                    mWidths.add(0);
                }
            }
            for (int i = mWidths.size() - 1; i >= 0; i--) {
                float r = mCoreRadius + mWidths.get(i);
                int alpha = mAlphas.get(i);
                if (alpha <= 0 || r >= mMaxRadius) {
                    mAlphas.remove(i);
                    mWidths.remove(i);
                }
            }
        }

        if (mCoreRadius != 0) {
            // 绘制中心圆及图片
            mPaint.setAlpha(255);
            mPaint.setColor(mCoreColor);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, mCoreRadius, mPaint);
        }

        if (mPressing) {
            if (mPressedBitmap != null) {
                canvas.drawBitmap(mPressedBitmap, getWidth() / 2 - mPressedBitmap.getWidth() / 2
                        , getHeight() / 2 - mPressedBitmap.getHeight() / 2, mPaint);
            }
        } else {
            if (mNormalBitmap != null) {
                canvas.drawBitmap(mNormalBitmap, getWidth() / 2 - mNormalBitmap.getWidth() / 2
                        , getHeight() / 2 - mNormalBitmap.getHeight() / 2, mPaint);
            }
        }

        if (going || !mWidths.isEmpty()) {
            getHandler().removeCallbacks(invalidRunnable);
            getHandler().postDelayed(invalidRunnable, 16);
        } else {
            setVisibility(GONE);
        }
    }


    Runnable invalidRunnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    /**
     * 开始扩散
     */
    public void start(long delay) {
        mStopTs = System.currentTimeMillis() + delay;
        setVisibility(VISIBLE);
        invalidate();
    }

    /**
     * 停止扩散
     */
    public void stop() {
        mStopTs = 0;
        setVisibility(GONE);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            mWidths.clear();
            mAlphas.clear();
        }
    }
}
