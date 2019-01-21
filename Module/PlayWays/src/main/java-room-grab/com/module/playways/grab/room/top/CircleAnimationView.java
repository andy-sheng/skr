package com.module.playways.grab.room.top;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.common.utils.U;

public class CircleAnimationView extends View {
    public final static String TAG = "GrabTopRv";

    public CircleAnimationView(Context context) {
        super(context);
        init();
    }

    public CircleAnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleAnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    Paint mPaint;
    int mProgress = 0;
    int mStrokeWitdh = U.getDisplayUtils().dip2px(2);

    void init() {
        mPaint = new Paint();//这个是画矩形的画笔，方便大家理解这个圆弧
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint.setAntiAlias(true);//取消锯齿
//        mPaint.setStyle(Paint.Style.FILL);//设置画圆弧的画笔的属性为描边(空心)，个人喜欢叫它描边，叫空心有点会引起歧义
        mPaint.setStrokeWidth(mStrokeWitdh);
        mPaint.setColor(Color.parseColor("#fee871"));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF oval = new RectF(mStrokeWitdh, mStrokeWitdh,
                getWidth() - mStrokeWitdh, getHeight() - mStrokeWitdh);
        canvas.drawArc(oval, 270, -mProgress * 360 / 100, false, mPaint);
    }

    public void setProgress(int p) {
        mProgress = p;
        invalidate();
    }
}
