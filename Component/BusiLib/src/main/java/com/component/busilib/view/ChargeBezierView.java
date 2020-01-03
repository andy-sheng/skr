package com.component.busilib.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class ChargeBezierView extends View {

    private Paint mExternalPaint;
    private Paint mInnerPaint;
    private Paint mLovePaint;

    private int mWidth;
    private int mHeight;
    // 充电进度值百分制
    private int mProgress;

    //水波纹于进度条的高度比
    private float rippleScale;

    private Random mRandom;

    private float mCircleX;
    private float mCircleY;
    private float mDefCircleRadius;

    private boolean isFinished = false;

    //水波纹高度坐标
    private float x;
    private float y;

    private void init() {
        mExternalPaint = getPaint(Color.parseColor("#554F94CD"));
        mInnerPaint = getPaint(Color.parseColor("#66B8FF"));
        mLovePaint = getPaint(Color.RED);
        mLovePaint.setStyle(Paint.Style.STROKE);
        mRandom = new Random();
    }

    private Paint getPaint(int color) {
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(18f);
        paint.setTextSize(60f);
        paint.setColor(color);
        return paint;
    }

    public ChargeBezierView(Context context) {
        this(context, null);
    }

    public ChargeBezierView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChargeBezierView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        mCircleX = mWidth / 2;
        mCircleY = mHeight / 2;

        mDefCircleRadius = mWidth / 4;
//        mRect = new RectF(mCircleX - mDefCircleRadius, mCircleY - mDefCircleRadius,
//                mCircleX + mDefCircleRadius, mCircleY + mDefCircleRadius);
//
//        mDiagonal = (float) Math.sqrt(Math.pow(mCircleX, 2) + Math.pow(mCircleY, 2));

        rippleScale = 2 * mDefCircleRadius / 100;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isFinished) {

        } else {
            drawLove(canvas);
            mLovePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            drawExternalRipple(canvas);
            mLovePaint.setXfermode(null);
        }
    }

    // 爱心底部的点(2 * mDefCircleRadius, 0)
    // 爱心左边的点(1.094 * mDefCircleRadius, 0.906 * mDefCircleRadius)
    // 爱心右边的点(2.906 * mDefCircleRadius, 0.906 * mDefCircleRadius)
    // 爱心顶部的点(2 * mDefCircleRadius, 1。813 * mDefCircleRadius)
    private void drawLove(Canvas canvas) {
        float radius = mDefCircleRadius;
        PointF bottom = new PointF(2f * radius, mCircleY + radius);
        PointF left = new PointF(1.094f * radius, -0.906f * radius + mCircleY + radius);
        PointF leftControl = new PointF(1.094f * radius, -1.813f * radius + mCircleY + radius);
        PointF right = new PointF(2.906f * radius, -0.906f * radius + mCircleY + radius);
        PointF rightControl = new PointF(2.906f * radius, -1.813f * radius + mCircleY + radius);
        PointF top = new PointF(2f * radius, -1.813f * radius + mCircleY + radius);

        Path lovePath = new Path();
        lovePath.moveTo(bottom.x, bottom.y);
        lovePath.lineTo(left.x, left.y);
        // 画两端圆弧
        RectF leftRecF = new RectF(0.906f * radius, -2f * radius + mCircleY + radius, 2.187f * radius, -0.719f * radius + mCircleY + radius);
        lovePath.arcTo(leftRecF, 135, 180);
        RectF rightRecF = new RectF(1.813f * radius, -2f * radius + mCircleY + radius, 3.094f * radius, -0.719f * radius + mCircleY + radius);
        lovePath.arcTo(rightRecF, 225, 180);
        lovePath.moveTo(right.x, right.y);
        lovePath.lineTo(bottom.x, bottom.y);
        lovePath.close();
        canvas.drawPath(lovePath, mLovePaint);
    }


    private PointF pExt0;
    private PointF pExt1;
    private PointF pExt2;
    private PointF pExt3;

    private PointF pIn0;
    private PointF pIn1;
    private PointF pIn2;
    private PointF pIn3;


    // 绘制海浪的波纹效果，分内部和外部两条
    private void drawExternalRipple(Canvas canvas) {

        // 计算进度的 x , y 位置
        y = mCircleY - mDefCircleRadius + (100 - mProgress) * rippleScale;
        x = caculateX(y);

        float rippleY = y;
        float rippleX = mCircleX;

        //内部
        pIn0 = new PointF(rippleX - mDefCircleRadius, rippleY);
        pIn1 = new PointF(rippleX - mRandom.nextInt((int) mDefCircleRadius), rippleY - mRandom.nextInt((int) (mDefCircleRadius / 4)));
        pIn2 = new PointF(rippleX + mRandom.nextInt((int) mDefCircleRadius), rippleY + mRandom.nextInt((int) (mDefCircleRadius / 4)));
        pIn3 = new PointF(rippleX + mDefCircleRadius, rippleY);
        Path inPath = new Path();
        inPath.moveTo(pIn0.x, pIn0.y);
        inPath.cubicTo(pIn1.x, pIn1.y, pIn2.x, pIn2.y, pIn3.x, pIn3.y);
        inPath.lineTo(mCircleX + mDefCircleRadius, mCircleY + mDefCircleRadius);
        inPath.lineTo(mCircleX - mDefCircleRadius, mCircleY + mDefCircleRadius);
        inPath.close();
        canvas.drawPath(inPath, mInnerPaint);

        // 外部
        pExt0 = new PointF(rippleX - mDefCircleRadius, rippleY);
        pExt1 = new PointF(rippleX - mRandom.nextInt((int) mDefCircleRadius), rippleY + mRandom.nextInt((int) (mDefCircleRadius / 3)));
        pExt2 = new PointF(rippleX + mRandom.nextInt((int) mDefCircleRadius), rippleY + mRandom.nextInt((int) (mDefCircleRadius / 3)));
        pExt3 = new PointF(rippleX + mDefCircleRadius, rippleY);
        Path extPath = new Path();
        extPath.moveTo(pExt0.x, pExt0.y);
        extPath.cubicTo(pExt1.x, pExt1.y, pExt2.x, pExt2.y, pExt3.x, pExt3.y);
        extPath.lineTo(mCircleX + mDefCircleRadius, mCircleY + mDefCircleRadius);
        extPath.lineTo(mCircleX - mDefCircleRadius, mCircleY + mDefCircleRadius);
        extPath.close();
        canvas.drawPath(extPath, mExternalPaint);

    }

    public void setProgress(int progress) {
        this.mProgress = progress;
        if (mProgress <= 100) {
            isFinished = false;
        } else {
            isFinished = true;
        }
        invalidate();
    }

    // 圆的方程式 a2 = b2 + c2
    private float caculateX(float y) {
        x = (float) Math.sqrt(Math.pow(mDefCircleRadius, 2) - y * y);
        return x;
    }
}