package com.zq.level.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.common.utils.U;
import com.component.busilib.R;

/**
 * 新段位下面的星星进度条
 */
public class LevelStarProgressBar extends View {


    public final static String TAG = "LevelStarProgressBar";

    Drawable mBgDrawable = U.app().getResources().getDrawable(R.drawable.level_jindutiaoguan);
    int w1, h1;

    Drawable mPrDrawable = U.app().getResources().getDrawable(R.drawable.level_jindutiao);
    int w2, h2;

    Drawable mLightDrawable = U.app().getResources().getDrawable(R.drawable.level_jindutiaofaguang);
    int w3, h3;

    Drawable mStarDrawable = U.app().getResources().getDrawable(R.drawable.level_jindutiaoxing);
    int w4, h4;

    int tx, ty;
    int tStar;

    float sx = 0; // progress=0的位置
    float px = 0; // 角度产生的 x 轴，下面边的偏移量
    float extendX = 0;// 如果到100% ，斜边矩形是会往外延伸的
    int mCurProgress = 0;// 当前进度

    double arc = Math.PI; // 角度

    public LevelStarProgressBar(Context context) {
        super(context);
        init();
    }

    public LevelStarProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LevelStarProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // TODO: 2019/3/12 目前是因为h4最大，作为了其它的基准
        w1 = mBgDrawable.getIntrinsicWidth();
        h1 = mBgDrawable.getIntrinsicHeight();

        w2 = mPrDrawable.getIntrinsicWidth();
        h2 = mPrDrawable.getIntrinsicHeight();

        w3 = mLightDrawable.getIntrinsicWidth();
        h3 = mLightDrawable.getIntrinsicHeight();

        w4 = mStarDrawable.getIntrinsicWidth();
        h4 = mStarDrawable.getIntrinsicHeight();

        ty = (h1 - h2) / 2;
        tx = (w1 - w2) / 2;
        tStar = (h1 - h4) / 2;

        sx = w4 + tStar * 2;
        px = (float) (Math.tan(arc) * h1);

        float bb = (h1 - U.getDisplayUtils().dip2px(25)) / 2.0f;
        extendX = (float) (Math.tan(arc) * bb);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 底部背景
        mBgDrawable.setBounds(0, (h3 - h1) / 2, w1, h1 + (h3 - h1) / 2);
        mBgDrawable.draw(canvas);
        canvas.save();

        // 进度条
        float temp = mCurProgress * (w1 + extendX - sx) / 100.0f;
        Path path = new Path();
        path.lineTo(temp + sx, 0);
        path.lineTo(temp + sx - px, h1 + (h3 - h1) / 2);
        path.lineTo(0, h1 + (h3 - h1) / 2);
        path.close();
        canvas.clipPath(path);
        mPrDrawable.setBounds(tx, ty + (h3 - h1) / 2, w2 + tx, h2 + ty + (h3 - h1) / 2);
        mPrDrawable.draw(canvas);
        canvas.restore();

        // 光圈用图替换，发光点
        float cx = temp + sx - px / 2;
        mLightDrawable.setBounds((int) (cx - w3 / 2), 0, w3 + (int) (cx - w3 / 2), h3);
        mLightDrawable.draw(canvas);

        // 星星
        mStarDrawable.setBounds(tStar, (h3 - h4) / 2, tStar + w4, h4 + (h3 - h4) / 2);
        mStarDrawable.draw(canvas);
    }

    public void setCurProgress(int curProgress) {
        mCurProgress = curProgress;
        invalidate();
    }
}
