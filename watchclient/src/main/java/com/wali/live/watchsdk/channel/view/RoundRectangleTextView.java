package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.Gravity;

import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;

/**
 * Created by ybao on 17/8/28
 * 世界小姐推荐选手的TextView，底部圆角矩形
 */
public class RoundRectangleTextView extends AppCompatTextView {
    private int width;
    private int height;

    private int mRectangleColor = getResources().getColor(R.color.color_fb98aa);
    private Paint mPaint;
    private RectF mRectF;

    public RoundRectangleTextView(Context context) {
        this(context, null);
    }

    public RoundRectangleTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundRectangleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        setPadding(0, 0, 0, DisplayUtils.dip2px(3));
        setTextColor(Color.WHITE);

        mPaint = new Paint();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mRectF = new RectF(DisplayUtils.dip2px(-19.66f), DisplayUtils.dip2px(40),
                DisplayUtils.dip2px(19.66f), DisplayUtils.dip2px(54.66f));
    }

    public void setRectangleColor(int color) {
        mRectangleColor = color;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(width / 2.0f, 0);
        mPaint.setColor(mRectangleColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(mRectF, 100, 100, mPaint);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(mRectF, 100, 100, mPaint);

        canvas.translate(-width / 2.0f, 0);
        super.onDraw(canvas);
    }
}
