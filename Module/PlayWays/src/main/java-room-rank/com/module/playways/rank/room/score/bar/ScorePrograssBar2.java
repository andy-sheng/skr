package com.module.playways.rank.room.score.bar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.common.utils.U;
import com.module.rank.R;

public class ScorePrograssBar2 extends View {

    public final static String TAG = "ScorePrograssBar2";

    Drawable mBgDrawable = U.app().getResources().getDrawable(R.drawable.xuecao);
    int w1, h1;

    Drawable mPrDrawable = U.app().getResources().getDrawable(R.drawable.xueye);
    int w2, h2;

    float tx, ty;

    float sx = 0; // progress=0的位置
    float px = 0; // 角度产生的 x 轴，下面边的偏移量
    float extendX = 0;// 如果到100% ，斜边矩形是会往外延伸的
    //    int speed = 1;// 速度
    int curProgress = 0;// 当前进度
    int oldProgress = 0;// 之前的进度
    int progress = 0;// 目标进度

    Paint mPaintCircle;

    Paint mPaintProgressBar;

    float prgress2 = 100;

    public ScorePrograssBar2(Context context) {
        super(context);
        init();
    }

    public ScorePrograssBar2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScorePrograssBar2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        w1 = mBgDrawable.getIntrinsicWidth();
        h1 = mBgDrawable.getIntrinsicHeight();

        w2 = mPrDrawable.getIntrinsicWidth();
        h2 = mPrDrawable.getIntrinsicHeight();

        ty = (h1 - h2) / 2.0f + U.getDisplayUtils().dip2px(3);
        tx = (w1 - w2) / 2.0f + U.getDisplayUtils().dip2px(3);

        sx = w1 * 25 / 100.0f;
        px = (float) (Math.tan(Math.PI / 4) * h1);

        float bb = (h1 - U.getDisplayUtils().dip2px(25)) / 2.0f;
        extendX = (float) (Math.tan(Math.PI / 4) * bb);

        mPaintCircle = new Paint();
        mPaintCircle.setAntiAlias(true);
        mPaintCircle.setDither(true);

        mPaintProgressBar = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintProgressBar.setDither(true);
        mPaintProgressBar.setStyle(Paint.Style.STROKE);//设置填充样式
        mPaintProgressBar.setAntiAlias(true);//抗锯齿功能
        mPaintProgressBar.setStrokeWidth(U.getDisplayUtils().dip2px(3));//设置画笔宽度
        mPaintProgressBar.setStrokeCap(Paint.Cap.SQUARE);


//
//        mPaintProgressBar = new Paint();//这个是画矩形的画笔，方便大家理解这个圆弧
//        mPaintProgressBar.setStyle(Paint.Style.STROKE);
//        mPaintProgressBar.setColor(Color.RED);
//
//        mPaintProgressBar.setAntiAlias(true);//取消锯齿
//        mPaintProgressBar.setStyle(Paint.Style.FILL);//设置画圆弧的画笔的属性为描边(空心)，个人喜欢叫它描边，叫空心有点会引起歧义
//        mPaintProgressBar.setStrokeWidth(20);
//        mPaintProgressBar.setColor(Color.CYAN);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mBgDrawable.setBounds(0, 0, w1, h1);
        mBgDrawable.draw(canvas);

        canvas.save();
        float temp = curProgress * (w1 + extendX - sx) / 100.0f;

        Path path = new Path();
        path.lineTo(temp + sx, 0);
        path.lineTo(temp + sx - px, h1);
        path.lineTo(0, h1);
        path.close();
        canvas.clipPath(path);
//        canvas.drawColor(Color.RED);
        mPrDrawable.setBounds((int) tx, (int) ty, w2, h2);
        mPrDrawable.draw(canvas);
        canvas.restore();

        if (tryPostInvalidateDelayed()) {
            float cx = temp + sx - px / 2;
            float cy = h1 / 2;
            float r = h1 / 2;
            //画光圈
            Shader shader = new RadialGradient(cx, cy, r, Color.parseColor("#FD2C9A"), 0x00000000, Shader.TileMode.CLAMP);
            mPaintCircle.setShader(shader);
            mPaintCircle.setAlpha(235);
            canvas.drawCircle(cx, cy, r, mPaintCircle);
        }

        int colors[] = new int[]{
                Color.parseColor("#169DDC"),
                Color.parseColor("#CA2C60"),

        };

        Shader shader = new SweepGradient(h1 / 2, h1 / 2, colors, new float[]{0, prgress2 / 100.f});
        mPaintProgressBar.setShader(shader);
        mPaintProgressBar.setStrokeWidth(U.getDisplayUtils().dip2px((h1 - h2) / 2.0f));
        // 画进度条
        float a = (h1 - h2) / 2.0f;
        RectF rectF = new RectF(tx, ty, h2, h2);

        canvas.drawArc(rectF, 0, 360 * prgress2 / 100.0f, false, mPaintProgressBar);

    }


    boolean tryPostInvalidateDelayed() {
        if (curProgress == progress) {
            return false;
        }
        if (curProgress < progress) {
            int np = curProgress + getSpeed();
            if (np > progress) {
                np = progress;
            }
            curProgress = np;
        }
        if (curProgress > progress) {
            int np = curProgress - getSpeed();
            if (np < progress) {
                np = progress;
            }
            curProgress = np;
        }
        postInvalidateDelayed(30);
        return true;
    }

    int getSpeed() {
//        oldProgress,progress,curProgress
        int speed = (progress - curProgress) / 5;
        if (curProgress > progress) {
            if (speed <= 0) {
                speed = 1;
            }
        } else {
            if (speed >= 0) {
                speed = -1;
            }
        }

        return speed;
    }

    public void setProgress(int p) {
        this.oldProgress = progress;
        this.progress = p;
        tryPostInvalidateDelayed();
    }

}
