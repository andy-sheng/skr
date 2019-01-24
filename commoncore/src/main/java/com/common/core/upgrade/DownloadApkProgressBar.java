package com.common.core.upgrade;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.common.utils.U;

public class DownloadApkProgressBar extends View {

    public DownloadApkProgressBar(Context context) {
        super(context);
        init();
    }

    public DownloadApkProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DownloadApkProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    Paint mPaint;
    int mProgress = 0;
    int mStrokeWitdh = U.getDisplayUtils().dip2px(2);
    int mR = U.getDisplayUtils().dip2px(17);

    private void init() {
        mPaint = new Paint();//这个是画矩形的画笔，方便大家理解这个圆弧
        mPaint.setAntiAlias(true);//取消锯齿
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.parseColor("#0C2275"));
            RectF rectF = new RectF(0, 0, getWidth(), getHeight());
            canvas.drawRoundRect(rectF, mR, mR, mPaint);

            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mStrokeWitdh);
            mPaint.setColor(Color.parseColor("#0C2275"));
            canvas.drawRoundRect(rectF, mR, mR, mPaint);
        }

        {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.parseColor("##E2467A"));
            RectF rectF = new RectF(0, 0, getWidth() * mProgress / 100, getHeight());
            canvas.drawRoundRect(rectF, mR, mR, mPaint);

            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mStrokeWitdh);
            mPaint.setColor(Color.parseColor("#0C2275"));
            canvas.drawRoundRect(rectF, mR, mR, mPaint);
        }

    }

    public void setProgress(int p) {
        mProgress = p;
        invalidate();
    }
}
