package com.module.playways.rank.room.scorebar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.common.utils.U;

import java.util.Random;

public class ScorePrograssBar1 extends View {

    public static class Params {
        int viewW, viewH;//
        int speed = 1;// 速度
        int curProgress = 0;
        int progress = 0;
        int paddingLeft = U.getDisplayUtils().dip2px(10), paddingRight = U.getDisplayUtils().dip2px(10);
        int barH = U.getDisplayUtils().dip2px(14);
        int radius = U.getDisplayUtils().dip2px(24);// 光圈半径
        int roundCorner = U.getDisplayUtils().dip2px(15);//进度条圆角

        public int getSpeed() {
            return speed;
        }

        public void setSpeed(int speed) {
            this.speed = speed;
        }

        public int getCurProgress() {
            return curProgress;
        }

        public void setCurProgress(int curProgress) {
            this.curProgress = curProgress;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public int getViewW() {
            return viewW;
        }

        public void setViewW(int viewW) {
            this.viewW = viewW;
        }

        public int getViewH() {
            return viewH;
        }

        public void setViewH(int viewH) {
            this.viewH = viewH;
        }

        public int getPaddingLeft() {
            return paddingLeft;
        }

        public void setPaddingLeft(int paddingLeft) {
            this.paddingLeft = paddingLeft;
        }

        public int getPaddingRight() {
            return paddingRight;
        }

        public void setPaddingRight(int paddingRight) {
            this.paddingRight = paddingRight;
        }

        public int getBarH() {
            return barH;
        }

        public void setBarH(int barH) {
            this.barH = barH;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public int getRoundCorner() {
            return roundCorner;
        }

        public void setRoundCorner(int roundCorner) {
            this.roundCorner = roundCorner;
        }
    }

    public final static String TAG = "SparkPrograssBar";

    Params mParams = new Params();

    private Paint mPaintBg;
    private Paint mPaintProgress;
    private Paint mPaintCircle;

    public ScorePrograssBar1(Context context) {
        super(context);
        init();

    }

    public ScorePrograssBar1(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaintBg = new Paint();
        mPaintBg.setAntiAlias(true);
        mPaintBg.setDither(true);

        mPaintProgress = new Paint();
        mPaintProgress.setAntiAlias(true);
        mPaintProgress.setDither(true);

        mPaintCircle = new Paint();
        mPaintCircle.setAntiAlias(true);
        mPaintCircle.setDither(true);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mParams.setViewW(getMeasuredWidth());
        this.mParams.setViewH(getMeasuredHeight());
    }

    Random mRandom = new Random();

    @Override
    protected void onDraw(Canvas canvas) {
        mPaintBg.setStyle(Paint.Style.FILL);

        LinearGradient linearGradient = new LinearGradient(mParams.getPaddingLeft(), (mParams.getViewH() - mParams.getBarH()) / 2,
                mParams.getViewW() - mParams.getPaddingRight(), (mParams.getViewH() + mParams.getBarH()) / 2,
                new int[]{Color.parseColor("#ff0166a2"), Color.parseColor("#ffff2c9a")},
                null, Shader.TileMode.CLAMP);
        mPaintBg.setShader(linearGradient);
        mPaintBg.setAlpha(100);
        //进度条底部
        canvas.drawRect(mParams.getPaddingLeft(), (mParams.getViewH() - mParams.getBarH()) / 2,
                mParams.getViewW() - mParams.getPaddingRight(), (mParams.getViewH() + mParams.getBarH()) / 2, mPaintBg);


        mPaintProgress.setShader(linearGradient);

        int pw = mParams.getCurProgress() * (mParams.getViewW() - mParams.getPaddingLeft() - mParams.getPaddingRight()) / 100;

        RectF rectF = new RectF(mParams.getPaddingLeft(), (mParams.getViewH() - mParams.getBarH()) / 2,
                mParams.getPaddingLeft() + pw, (mParams.getViewH() + mParams.getBarH()) / 2);
        //进度条下半部分
        canvas.drawRoundRect(rectF,mParams.getRoundCorner(),mParams.getRoundCorner(), mPaintProgress);

        //画光圈
        Shader shader = new RadialGradient(mParams.getPaddingLeft() + pw, mParams.getViewH() / 2, mParams.getRadius(), Color.parseColor("#FD2C9A"), 0x00000000, Shader.TileMode.CLAMP);
        mPaintCircle.setShader(shader);
        mPaintCircle.setAlpha(235);
        canvas.drawCircle(mParams.getPaddingLeft() + pw, mParams.getViewH() / 2, mParams.getRadius(), mPaintCircle);
        tryPostInvalidateDelayed();
    }

    void tryPostInvalidateDelayed() {
        if (mParams.getCurProgress() == mParams.getProgress()) {
            return;
        }
        if (mParams.getCurProgress() < mParams.getProgress()) {
            int np = mParams.getCurProgress() + mParams.getSpeed();
            if (np > mParams.getProgress()) {
                np = mParams.getProgress();
            }
            mParams.setCurProgress(np);
        }
        if (mParams.getCurProgress() > mParams.getProgress()) {
            int np = mParams.getCurProgress() - mParams.getSpeed();
            if (np < mParams.getProgress()) {
                np = mParams.getProgress();
            }
            mParams.setCurProgress(np);
        }
        postInvalidateDelayed(30);
    }

    public void setProgress(int prograss) {
        mParams.setProgress(prograss);
        tryPostInvalidateDelayed();
    }

    public void stopSlidSpak() {

    }
}
