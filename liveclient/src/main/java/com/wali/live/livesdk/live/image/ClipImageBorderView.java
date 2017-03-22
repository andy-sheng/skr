package com.wali.live.livesdk.live.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.base.utils.display.DisplayUtils;
import com.wali.live.livesdk.R;

/**
 * 裁剪框
 * Create by zhangyuehuan 2016/6/15
 */
public class ClipImageBorderView extends View {
    /**
     * 水平方向与View的边距
     */
    private int mHorizontalPadding;
    /**
     * 垂直方向与View的边距
     */
    private int mVerticalPadding;
    /**
     * 绘制的矩形的宽度
     */
    private int mWidth;
    /**
     * 边框的颜色，默认为白色
     */
    private int mBorderColor = Color.WHITE;
    /**
     * 边框的宽度单位dp
     */
    private float mBorderWidth = 0.3f;

    private Paint mPaint;

    private int mClipHeight;

    public ClipImageBorderView(Context context) {
        this(context, null);
    }

    public ClipImageBorderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipImageBorderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ClipImageSize);
        mClipHeight = DisplayUtils.dip2px(a.getInt(R.styleable.ClipImageSize_clip_image_height, 0));
        a.recycle();

        mBorderWidth = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, mBorderWidth, getResources()
                        .getDisplayMetrics());
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mClipHeight > 0){
            mVerticalPadding = (getHeight() - mClipHeight)/2;
        } else {
            // 计算矩形区域的宽度
            mWidth = getWidth() - 2 * mHorizontalPadding;
            // 计算距离屏幕垂直边界的边距
            mVerticalPadding = (getHeight() - mWidth) / 2;
        }
        mPaint.setColor(getResources().getColor(R.color.color_black_trans_60));
        mPaint.setStyle(Style.FILL);
        // 绘制左边1
        canvas.drawRect(0, 0, mHorizontalPadding, getHeight(), mPaint);
        // 绘制右边2
        canvas.drawRect(getWidth() - mHorizontalPadding, 0, getWidth(),
                getHeight(), mPaint);
        // 绘制上边3
        canvas.drawRect(mHorizontalPadding, 0, getWidth() - mHorizontalPadding,
                mVerticalPadding, mPaint);
        // 绘制下边4
        canvas.drawRect(mHorizontalPadding, getHeight() - mVerticalPadding,
                getWidth() - mHorizontalPadding, getHeight(), mPaint);
        // 绘制外边颜色
        mPaint.setColor(mBorderColor);
        mPaint.setStrokeWidth(mBorderWidth);
        mPaint.setStyle(Style.STROKE);
        canvas.drawRect(mHorizontalPadding, mVerticalPadding, getWidth()
                - mHorizontalPadding, getHeight() - mVerticalPadding, mPaint);

    }

    public void setClipHeight(int height){
        this.mClipHeight = height;
        invalidate();
    }
}
