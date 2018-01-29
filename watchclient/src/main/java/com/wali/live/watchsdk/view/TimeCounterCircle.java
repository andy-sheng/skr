package com.wali.live.watchsdk.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;

public class TimeCounterCircle extends ProgressBar {
    private Paint mPaint;
    private int mRadius = DisplayUtils.dip2px(23.67f);//半径
    private int strokeWidth = DisplayUtils.dip2px(4.67f);//画笔画线的宽度
    private String str;//时间的字符串

    private RectF rect;

    public TimeCounterCircle(Context context) {
        this(context, null);
    }

    public TimeCounterCircle(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeCounterCircle(Context context, AttributeSet attrs, int defStyle) {
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

        mPaint.setColor(getResources().getColor(R.color.color_ff729b));
        mPaint.setStrokeWidth(strokeWidth);//覆盖线的宽度
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        /**
         * oval :指定圆弧的外轮廓矩形区域。计算方式看看就清楚了，核心是x、y相关的坐标
         startAngle: 圆弧起始角度，单位为度。
         sweepAngle: 圆弧扫过的角度，顺时针方向，单位为度,从右中间开始为零度。
         useCenter: 如果为True时，在绘制圆弧时将圆心包括在内，
         */

        float sweepAngle = getProgress() * 1.0f / getMax() * 360;//根据现在值和最大值的百分比计算出弧线现在的度数

        if (rect == null) {
            rect = new RectF();
        }
        rect.left = strokeWidth / 2;
        rect.top = strokeWidth / 2;
        rect.right = getWidth() - strokeWidth / 2;
        rect.bottom = getHeight() - strokeWidth / 2;

        canvas.drawArc(rect, -90, sweepAngle, false, mPaint);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec,
                                          int heightMeasureSpec) {
        //根据半径算出占屏幕的比重，圆环宽，padding相关
        int widthSize = mRadius * 2 + strokeWidth * 3 + getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(widthSize, widthSize);
    }
}