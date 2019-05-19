package com.common.view.countdown;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.common.base.R;
import com.common.utils.U;

public class CircleCountDownView extends ProgressBar {

    private Paint mPaint;
    private Paint mBgPaint;
    private float mRadius = U.getDisplayUtils().dip2px(23.33f);//半径
    private float strokeWidth = U.getDisplayUtils().dip2px(3.0f);//画笔画线的宽度
    private int mDegree = 270;// 从那个角度开始转
    private int mBgStrokeColor = U.getColor(R.color.transparent);
    private int mBgColor = U.getColor(R.color.transparent);
    private float mBgStrokeWidth = strokeWidth;//画笔画线的宽度
    int mColor = Color.parseColor("#E9AC1A");

    private RectF rect;

    ValueAnimator mRecordAnimator;

    public CircleCountDownView(Context context) {
        this(context, null);
    }

    public CircleCountDownView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleCountDownView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.CircleCountDownView);
            mRadius = typedArray.getDimension(R.styleable.CircleCountDownView_inner_stroke_raduis, 0);
            strokeWidth = typedArray.getDimension(R.styleable.CircleCountDownView_inner_stroke_width, 0);
            mColor = typedArray.getColor(R.styleable.CircleCountDownView_inner_color, Color.parseColor("#E9AC1A"));
            mDegree = typedArray.getInteger(R.styleable.CircleCountDownView_degree, 270);
            mBgStrokeColor = typedArray.getColor(R.styleable.CircleCountDownView_stroke_bg_color, U.getColor(R.color.transparent));
            mBgColor = typedArray.getColor(R.styleable.CircleCountDownView_bg_color, U.getColor(R.color.transparent));
            mBgStrokeWidth = typedArray.getDimension(R.styleable.CircleCountDownView_stroke_bg_width, strokeWidth);
            typedArray.recycle();
        }

        mBgPaint = new Paint();
        Shader mBgShader = new SweepGradient(getWidth() / 2, getHeight() / 2,
                new int[]{mBgColor,
                        mBgColor,
                        mBgColor,
                        mBgColor
                }, null);
        mBgPaint.setShader(mBgShader);
        mBgPaint.setAntiAlias(true);
        mBgPaint.setStyle(Style.FILL);//实心
        mBgPaint.setStrokeWidth(strokeWidth);//线的宽度

        mPaint = new Paint();
        mPaint.setStyle(Style.STROKE);//空心
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setDither(true);
        mPaint.setColor(getResources().getColor(R.color.white));
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(getResources().getColor(R.color.transparent));
        //画整个背景
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, mBgPaint);

        //画进度条的背景
        Shader mBgStrokeShader = new SweepGradient(getWidth() / 2, getHeight() / 2,
                new int[]{mBgStrokeColor,
                        mBgStrokeColor,
                        mBgStrokeColor,
                        mBgStrokeColor
                }, null);

        mPaint.setShader(mBgStrokeShader);
        mPaint.setStrokeWidth(mBgStrokeWidth);

        if (rect == null) {
            rect = new RectF();
        }
        rect.left = (getWidth() - mRadius * 2) / 2;
        rect.top = (getHeight() - mRadius * 2) / 2;

        rect.right = rect.left + mRadius * 2;
        rect.bottom = rect.top + mRadius * 2;
        canvas.drawArc(rect, 270, 360, false, mPaint);

        //画进度条
        Shader mShader = new SweepGradient(getWidth() / 2, getHeight() / 2,
                new int[]{mColor,
                        mColor,
                        mColor,
                        mColor
                }, null);

        mPaint.setShader(mShader);
        mPaint.setStrokeWidth(strokeWidth);//覆盖线的宽度

        float sweepAngle = (getMax() - getProgress() * 1.0f) / getMax() * 360;//根据现在值和最大值的百分比计算出弧线现在的度数
        canvas.drawArc(rect, mDegree, sweepAngle, false, mPaint);
    }

    public void go(int start, int leave) {
        cancelAnim();
        setMax(360);
        int startD = (360 * start)/(100);

        mRecordAnimator = ValueAnimator.ofInt(startD, 360);
        mRecordAnimator.setDuration(leave > 0 ? leave : 0);
        mRecordAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                setProgress(value);
            }
        });
        mRecordAnimator.start();
    }

    public void cancelAnim() {
        if (mRecordAnimator != null) {
            mRecordAnimator.removeAllUpdateListeners();
            mRecordAnimator.cancel();
            mRecordAnimator = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAnim();
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec,
                                          int heightMeasureSpec) {
        //根据半径算出占屏幕的比重，圆环宽，padding相关
        int widthSize = (int) (mRadius * 2) + (int) (strokeWidth < mBgStrokeWidth ? mBgStrokeWidth : strokeWidth * 2) + getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(widthSize, widthSize);
    }

}
