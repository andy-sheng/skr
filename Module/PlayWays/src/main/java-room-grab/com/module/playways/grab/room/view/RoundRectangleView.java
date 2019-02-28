package com.module.playways.grab.room.view;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import com.common.log.MyLog;
import com.common.utils.U;

/**
 * 从坐上角开始
 */
public class RoundRectangleView extends View {
    public final static String TAG = "RoundRectangleView";
    private int max = 100;
    private int min = 0;

    //右上角开始,满足 0 < startAngle < 90
    private int startAngle = 0;

    //右下角结束,满足 270 < startAngle < 360
    private int endAngle = 360;

    //毫秒
    private long mDuration = 3000;

    private int currentProgress = 3000;

//    RectF progressArea;

    Paint mPaint;

    int mRadio;

    int mLineWidth;

    RectF circleCenterA = new RectF();

    RectF circleCenterB = new RectF();

    int totalLenght = 0;

    AnimatorSet mAnimatorSet;

    int mProgressWidth = U.getDisplayUtils().dip2px(3);

    public RoundRectangleView(Context context) {
        super(context);
        init();
    }

    public RoundRectangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundRectangleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private int getMeasuredWidthR(){
        return getMeasuredWidth() - mProgressWidth;
    }

    private int getMeasuredHeightR(){
        return getMeasuredHeight() - mProgressWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        MyLog.d(TAG, "width " + getMeasuredWidth() + " height " + getMeasuredHeight());
        mRadio = getMeasuredHeightR() / 2;

        mLineWidth = getMeasuredWidthR() - getMeasuredHeight();

        totalLenght =  (int) (2 * Math.PI * mRadio) + mLineWidth * 2;

        circleCenterA.set(
                mProgressWidth / 2, mProgressWidth / 2,
                getMeasuredHeightR(),
                getMeasuredHeightR()
        );

        circleCenterB.set(
                getMeasuredWidthR() - getMeasuredHeightR(), mProgressWidth / 2,
                getMeasuredWidthR(),
                getMeasuredHeightR()
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(!hasData){
            return;
        }
        //分五个区域画，从最后一个部分开始画
        int a = endAngle - 270;
        double arc = 2 * Math.PI * mRadio * a / 360;

        int angle = percentageToAngle(currentProgress, a);

        canvas.drawArc(circleCenterB, 0,
                angle, false, mPaint);

        int lastedProgress = currentProgress - (int) arc;

        if(lastedProgress <= 0){
            return;
        }

        int bottomLineLengh = progressToLine(lastedProgress, mLineWidth);

        canvas.drawLine(getMeasuredWidth() - getMeasuredHeight() / 2, getMeasuredHeight() - mProgressWidth, mRadio + (mLineWidth - bottomLineLengh), getMeasuredHeight() - mProgressWidth, mPaint);

        lastedProgress = lastedProgress - bottomLineLengh;
        if(lastedProgress <= 0){
            return;
        }

        arc = 2 * Math.PI * mRadio * 180 / 360;
        angle = percentageToAngle(lastedProgress, 180);
        canvas.drawArc(circleCenterA, 0 + 90,
                angle, false, mPaint);

        lastedProgress = lastedProgress - (int) arc;
        if(lastedProgress <= 0){
            return;
        }

        int topLineLengh = progressToLine(lastedProgress, mLineWidth);
        canvas.drawLine(getMeasuredHeight() / 2, mProgressWidth / 2, getMeasuredHeight() / 2 + topLineLengh, mProgressWidth / 2, mPaint);

        lastedProgress = lastedProgress - mLineWidth;
        if(lastedProgress <= 0){
            return;
        }

        a = 90 - startAngle;
        angle = percentageToAngle(lastedProgress, a);
        canvas.drawArc(circleCenterB, 0 - 90,
                angle, false, mPaint);

    }

    public void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mProgressWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.parseColor("#FFFFFF"));
//        mPaint.setShader(generateOutSweepGradient());
    }

    private SweepGradient generateOutSweepGradient() {
        SweepGradient sweepGradient = new SweepGradient(mRadio, mRadio,
                new int[]{0xFFFFED61, 0xFFFFED61},
                new float[]{0, 0}
        );

        Matrix matrix = new Matrix();
//        matrix.setRotate(mStartAngle - 10, mCenterX, mCenterY);
        sweepGradient.setLocalMatrix(matrix);

        return sweepGradient;
    }

    boolean hasData = false;

    public void startCountDown(long duration) {
        if (duration <= 0) {
            return;
        }

        hasData = true;

        mDuration = duration;

        ValueAnimator creditValueAnimator = ValueAnimator.ofInt(totalLenght, 0);
        creditValueAnimator.setDuration(mDuration);
        creditValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentProgress = (int) animation.getAnimatedValue();
//                MyLog.d(TAG, "currentProgress " + currentProgress);
                postInvalidate();
            }
        });

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet
                .playTogether(creditValueAnimator);
        mAnimatorSet.start();
    }

    public void stopCountDown(){
        hasData = false;
        if(mAnimatorSet != null){
            mAnimatorSet.cancel();
        }

        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if(mAnimatorSet != null){
            mAnimatorSet.cancel();
        }
    }

    /**
     * 百分比变成角度
     */
    public int percentageToAngle(int current, int need) {
        double arc = 2 * Math.PI * mRadio * need / 360;
        if(current >= arc){
            return need;
        } else {
            double realAngle = (double)current / (2 * Math.PI * mRadio / 360);
            return (int) realAngle;
        }
    }

    /**
     * 百分比变成角度
     */
    public int progressToLine(int current, int need) {
        if(current >= need){
            return need;
        }else {
            return current;
        }
    }
}
