package com.wali.live.watchsdk.fastsend.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;

public class CircleProgressBar extends ProgressBar {

    private Paint mPaint;
    private int mRadius = DisplayUtils.dip2px(18f);//半径
    private int strokeWidth = 6;//画笔画线的宽度
    private String str;//时间的字符串

    private RectF rect;

    public CircleProgressBar(Context context) {
        this(context, null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mPaint == null) {
            mPaint = new Paint();
        }
        canvas.drawColor(getResources().getColor(R.color.transparent));//画布背景色

        mPaint.setColor(getResources().getColor(R.color.transparent));
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Style.STROKE);//空心
        mPaint.setStrokeWidth(strokeWidth);//线的宽度

        canvas.drawCircle(getWidth() / 2, getHeight() / 2, mRadius, mPaint);

        mPaint.setColor(getResources().getColor(R.color.white));
        mPaint.setStrokeWidth(strokeWidth);//覆盖线的宽度
        mPaint.setAntiAlias(true);

        float sweepAngle =(getMax() - getProgress() * 1.0f) / getMax() * 360;//根据现在值和最大值的百分比计算出弧线现在的度数

        if (rect == null) {
            rect = new RectF();
        }
        rect.left = (getWidth() - mRadius * 2)/2;
        rect.top = (getHeight() - mRadius * 2)/2;

        rect.right = rect.left + mRadius * 2;
        rect.bottom =rect.top + mRadius * 2;

        canvas.drawArc(rect, 270,sweepAngle, false, mPaint);

    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec,
                                          int heightMeasureSpec) {
        //根据半径算出占屏幕的比重，圆环宽，padding相关
        int widthSize = mRadius * 2 + strokeWidth*2 + getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(widthSize, widthSize);
    }

}