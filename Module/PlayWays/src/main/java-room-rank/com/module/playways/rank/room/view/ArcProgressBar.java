package com.module.playways.rank.room.view;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.common.utils.U;
import com.module.rank.R;

/**
 * Created by youzehong on 16/4/19.
 */
public class ArcProgressBar extends View {
    private Paint mDottedLinePaint;

    /**
     * 虚线默认颜色
     */
    private int mDottedDefaultColor = Color.parseColor("#E08B10");
    /**
     * 虚线变动颜色
     */
    private int mDottedRunColor = Color.parseColor("#767995");
    /**
     * 线条数
     */
    private int mDottedLineCount = 80;
    /**
     * 线条高度
     */
    private int mDottedLineHeight = U.getDisplayUtils().dip2px(2);
    /**
     * 线条宽度
     */
    private int mDottedLineWidth = U.getDisplayUtils().dip2px(7) - mDottedLineHeight;
    /**
     * 进度条最大值
     */
    private int mProgressMax = 100;

    int mWidth = -1;// view的宽度
    int mHeight = -1;// view的高度
    private int mProgress;
    private float mExternalDottedLineRadius;
    private float mInsideDottedLineRadius;
    private float mArcCenterX;
    private float mArcRadius; // 圆弧半径
    private boolean isRestart = false;

    private int mRealProgress;

    AnimatorSet mAnimatorSet;

    public ArcProgressBar(Context context) {
        this(context, null, 0);
    }

    public ArcProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.arcProgress);
        mDottedLineCount = typedArray.getInt(R.styleable.arcProgress_linesnum, 80);
        typedArray.recycle();

        // 内测虚线的画笔
        mDottedLinePaint = new Paint();
        mDottedLinePaint.setAntiAlias(true);
        mDottedLinePaint.setStrokeWidth(mDottedLineHeight);
        mDottedLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mDottedLinePaint.setColor(mDottedDefaultColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mArcCenterX = w / 2.f;
        mArcRadius = w / 2.f - mDottedLineHeight;

        // 内部虚线的外部半径
        mExternalDottedLineRadius = mArcRadius;
        // 内部虚线的内部半径
        mInsideDottedLineRadius = mExternalDottedLineRadius - mDottedLineWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mWidth < 0) {
            mWidth = getWidth();
        }
        if (mHeight < 0) {
            mHeight = getHeight();
        }
        drawDottedLineArc(canvas);
        drawRunDottedLineArc(canvas);
        if (isRestart) {
            drawDottedLineArc(canvas);
        }
    }


    public void restart() {
        isRestart = true;
        this.mRealProgress = 0;
        invalidate();
    }

    /**
     * 设置最大进度
     *
     * @param max
     */
    public void setMaxProgress(int max) {
        this.mProgressMax = max;
    }

    /**
     * 设置当前进度
     *
     * @param progress
     */
    public void setProgress(int progress) {
        this.mRealProgress = progress;
        isRestart = false;
        this.mProgress = ((mDottedLineCount) * progress) / mProgressMax;
        postInvalidate();
    }

    /**
     * @param progress 进度条，从什么地方开始播放
     * @param duration 剩下歌曲的时长
     */
    public void startCountDown(int progress, long duration) {
        if (mAnimatorSet != null) {
            mAnimatorSet.removeAllListeners();
            mAnimatorSet.cancel();
        }
        setProgress(progress);

        ValueAnimator creditValueAnimator = ValueAnimator.ofInt(progress + 1, 100);
        creditValueAnimator.setInterpolator(new LinearInterpolator());
        creditValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = (int) animation.getAnimatedValue();
                setProgress(progress);
            }
        });


        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.setDuration(duration).play(creditValueAnimator);
        mAnimatorSet.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimatorSet != null) {
            mAnimatorSet.removeAllListeners();
            mAnimatorSet.cancel();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            if (mAnimatorSet != null) {
                mAnimatorSet.removeAllListeners();
                mAnimatorSet.cancel();
            }
        }
    }

    private void drawRunDottedLineArc(Canvas canvas) {
        mDottedLinePaint.setColor(mDottedRunColor);
        float evenryDegrees = (float) (2.0f * Math.PI / mDottedLineCount);

        float startDegress = (float) (90 * Math.PI / 180);

        for (int i = 0; i < mProgress; i++) {
            float degrees = i * evenryDegrees + startDegress;

            float startX = mArcCenterX + (float) Math.sin(degrees) * mInsideDottedLineRadius;
            float startY = mArcCenterX - (float) Math.cos(degrees) * mInsideDottedLineRadius;

            float stopX = mArcCenterX + (float) Math.sin(degrees) * mExternalDottedLineRadius;
            float stopY = mArcCenterX - (float) Math.cos(degrees) * mExternalDottedLineRadius;

            canvas.drawLine(startX, startY, stopX, stopY, mDottedLinePaint);
        }
    }

    private void drawDottedLineArc(Canvas canvas) {
        mDottedLinePaint.setColor(mDottedDefaultColor);
        // 360 * Math.PI / 180
        float evenryDegrees = (float) (2.0f * Math.PI / mDottedLineCount);

        float startDegress = (float) (180 * Math.PI / 180);
        float endDegress = (float) (180 * Math.PI / 180);

        for (int i = 0; i < mDottedLineCount; i++) {
            float degrees = i * evenryDegrees;
            // 过滤底部90度的弧长
            if (degrees > startDegress && degrees < endDegress) {
                continue;
            }

            float startX = mArcCenterX + (float) Math.sin(degrees) * mInsideDottedLineRadius;
            float startY = mArcCenterX - (float) Math.cos(degrees) * mInsideDottedLineRadius;

            float stopX = mArcCenterX + (float) Math.sin(degrees) * mExternalDottedLineRadius;
            float stopY = mArcCenterX - (float) Math.cos(degrees) * mExternalDottedLineRadius;


            canvas.drawLine(startX, startY, stopX, stopY, mDottedLinePaint);
        }
    }

}
