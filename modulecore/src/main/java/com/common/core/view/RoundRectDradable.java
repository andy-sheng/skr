package com.common.core.view;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.RoundRectShape;
import com.common.utils.U;

/**
 * Created by yurui on 3/22/16.
 */
public class RoundRectDradable extends Drawable {
    private Paint mPaint = new Paint();
    private RoundRectShape mShape;
    private float[] mOuter;
    private float mRadius;

    public RoundRectDradable(int color) {
        this(color, U.getDisplayUtils().dip2px(6));
    }

    public RoundRectDradable(int color, float radius) {
        mPaint.setColor(color);
        mPaint.setAntiAlias(true);
        mRadius = radius;
    }

    public void setRadius(float radius) {
        this.mRadius = radius;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        refreshShape();
        mShape.resize(bounds.right - bounds.left, bounds.bottom - bounds.top);
    }

    private void refreshShape() {
        mOuter = new float[]{mRadius, mRadius //TopLeft
                , mRadius, mRadius            //TopRight
                , mRadius, mRadius            //BottomLeft
                , mRadius, mRadius};          //BottomLeft
        mShape = new RoundRectShape(mOuter, null, null);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    @Override
    public void draw(Canvas canvas) {
        mShape.draw(canvas, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return mPaint.getAlpha();
    }
}