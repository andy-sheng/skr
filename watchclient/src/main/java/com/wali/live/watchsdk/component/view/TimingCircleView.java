package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;

public class TimingCircleView extends ProgressBar {
    private int mRadius = DisplayUtils.dip2px(20.33f);  //半径
    private int mStrokeWidth = 6;                       //画笔画线的宽度

    private Paint mPaint;
    private RectF mRect;

    public TimingCircleView(Context context) {
        this(context, null);
        init();
    }

    public TimingCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public TimingCircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Style.STROKE);

        mRect = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //根据半径算出占屏幕的比重，圆环宽，padding相关
        int widthSize = mRadius * 2 + mStrokeWidth * 3 + getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(widthSize, widthSize);

        mRect.left = getPaddingLeft() + mStrokeWidth / 2 + mStrokeWidth;
        mRect.top = getPaddingTop() + mStrokeWidth / 2 + mStrokeWidth;
        mRect.right = getPaddingLeft() + mRadius * 2 + mStrokeWidth / 2 + mStrokeWidth;
        mRect.bottom = getPaddingTop() + mRadius * 2 + mStrokeWidth / 2 + mStrokeWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(getResources().getColor(R.color.transparent));//画布背景色

        mPaint.setColor(getResources().getColor(R.color.color_black_trans_90));
        mPaint.setStrokeWidth(mStrokeWidth);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, mRadius, mPaint);

        mPaint.setColor(getResources().getColor(R.color.color_fff005));
        mPaint.setStrokeWidth(mStrokeWidth);
        //根据现在值和最大值的百分比计算出弧线现在的度数
        float sweepAngle = getProgress() * 1.0f / getMax() * 360;
        canvas.drawArc(mRect, -90, sweepAngle, false, mPaint);

        mPaint.setColor(getResources().getColor(R.color.color_black_trans_50));
        mPaint.setStrokeWidth(2);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, mRadius + 4, mPaint);
    }
}