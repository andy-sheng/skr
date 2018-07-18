package com.wali.live.watchsdk.personalcenter.level.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.wali.live.watchsdk.R;

/**
 * 我的等级页面的 进度条
 * Created by yaojian on 16-3-11.
 */
public class LevelProgressBarTemp extends View {

    private float mPercent = 0;         //百分比
    private Paint mPaint;
    private int mBackgroundColor;
    private int mSolidColor;

    private static final int DEFAULT_BACKGROUND_COLOR = 0x3a3c45;
    private static final int DEFAULT_SOLID_COLOR = 0xffbc21;

    public LevelProgressBarTemp(Context context) {
        this(context, null);
    }

    public LevelProgressBarTemp(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LevelProgressBarTemp(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LevelProgressBarTemp, defStyleAttr, 0);
        try {
            mBackgroundColor = a.getColor(R.styleable.LevelProgressBarTemp_progressBackgroundColor, DEFAULT_BACKGROUND_COLOR);
            mSolidColor = a.getColor(R.styleable.LevelProgressBarTemp_progressSolidColor, DEFAULT_SOLID_COLOR);
        } finally {
            a.recycle();
        }
    }

    /**
     * 设置百分比
     * @param percent
     */
    public void setPercent(float percent){
        if(percent < 0){
            mPercent = 0;
        }else if(percent > 1){
            mPercent = 1;
        }else{
            mPercent = percent;
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


//        float roundWidth = DisplayUtils.px2dip(10);
        /**
         * 画最外层的大圆环
         */
//        int center = getWidth() / 2; //获取圆心的x坐标
//        int radius = (int) (center - roundWidth / 2); //圆环的半径
//        Paint paint = new Paint();
//        paint.setColor(getContext().getResources().getColor(R.color.color_e5e5e5)); //设置圆环的颜色
//        paint.setStyle(Paint.Style.STROKE); //设置空心
//        paint.setStrokeWidth(roundWidth); //设置圆环的宽度
//        paint.setAntiAlias(true);  //消除锯齿
//        canvas.drawCircle(center, center, radius, paint); //画出圆环
//
//        Paint paint1 = new Paint();
//        paint1.setStrokeWidth(roundWidth);
//        paint1.setColor(getResources().getColor(R.color.color_e5aa1e));
//        RectF oval = new RectF(center - radius, center - radius, center + radius, center + radius);
//        paint1.setStyle(Paint.Style.STROKE);
//        paint1.setAntiAlias(true);  //消除锯齿
//        canvas.drawArc(oval, -90, 360 * mPercent, false, paint1);

        /**
         * 画进度条背景
         */
        drawBackground(canvas);
        drawProgressbar(canvas);
    }

    private void drawBackground(Canvas canvas) {
        mPaint.setColor(mBackgroundColor);
        canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
    }

    private void drawProgressbar(Canvas canvas) {
        mPaint.setColor(mSolidColor);
        canvas.drawRect(0, 0, getWidth() * mPercent, getHeight(), mPaint);
    }
}

