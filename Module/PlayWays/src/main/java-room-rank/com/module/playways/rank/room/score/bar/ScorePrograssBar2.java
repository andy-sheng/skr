package com.module.playways.rank.room.score.bar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.common.log.MyLog;
import com.common.utils.U;
import com.module.rank.R;

public class ScorePrograssBar2 extends View {

    public final static String TAG = "ScorePrograssBar2";

    DrawableWithLocation mLevelDrawables[];

    Drawable mBgDrawable = U.app().getResources().getDrawable(R.drawable.xuecao);
    int w1, h1;

    Drawable mPrDrawable = U.app().getResources().getDrawable(R.drawable.xueye);
    int w2, h2;

    int tx, ty;

    float sx = 0; // progress=0的位置
    float px = 0; // 角度产生的 x 轴，下面边的偏移量
    float extendX = 0;// 如果到100% ，斜边矩形是会往外延伸的
    //    int speed = 1;// 速度
    int mCurProgress = 0;// 当前进度
    int mOldProgress = 0;// 之前的进度
    int mProgress = 0;// 目标进度

    Paint mPaintCircle;

    Paint mPaintCircleBar;
    RectF mRectF;
    float mPrgress2 = 0;


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

        ty = (h1 - h2) / 2;
        tx = (w1 - w2) / 2;

        sx = w1 * 25 / 100.0f;
        px = (float) (Math.tan(Math.PI / 4) * h1);

        float bb = (h1 - U.getDisplayUtils().dip2px(25)) / 2.0f;
        extendX = (float) (Math.tan(Math.PI / 4) * bb);

        mPaintCircle = new Paint();
        mPaintCircle.setAntiAlias(true);
        mPaintCircle.setDither(true);

        mPaintCircleBar = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintCircleBar.setDither(true);
        mPaintCircleBar.setStyle(Paint.Style.STROKE);//设置填充样式
        mPaintCircleBar.setAntiAlias(true);//抗锯齿功能
        mPaintCircleBar.setStrokeCap(Paint.Cap.SQUARE);
        Matrix matrix = new Matrix();

        int colors[] = new int[]{
                Color.parseColor("#169DDC"),
                Color.parseColor("#CA2C60"),

        };
        float strokeWidth = U.getDisplayUtils().dip2px(10);
        mPaintCircleBar.setStrokeWidth(strokeWidth);//设置画笔宽度
        float cx = h1 / 2;
        float cy = h1 / 2;
        Shader shader = new SweepGradient(cx, cy, colors, null);
        matrix.setRotate(90, cx, cy);
        shader.setLocalMatrix(matrix);
        mPaintCircleBar.setShader(shader);

        float r = h2 / 2.0f - strokeWidth / 2.0f;
        mRectF = new RectF(cx - r, cy - r, cx + r, cy + r);
//        mPaintProgressBar = new Paint();//这个是画矩形的画笔，方便大家理解这个圆弧
//        mPaintProgressBar.setStyle(Paint.Style.STROKE);
//        mPaintProgressBar.setColor(Color.RED);
//
//        mPaintProgressBar.setAntiAlias(true);//取消锯齿
//        mPaintProgressBar.setStyle(Paint.Style.FILL);//设置画圆弧的画笔的属性为描边(空心)，个人喜欢叫它描边，叫空心有点会引起歧义
//        mPaintProgressBar.setStrokeWidth(20);
//        mPaintProgressBar.setColor(Color.CYAN);

        mLevelDrawables = new DrawableWithLocation[]{
                new DrawableWithLocation(U.getDrawable(R.drawable.ycjm_jdt_a), getXByProgress(60), 60),
                new DrawableWithLocation(U.getDrawable(R.drawable.ycjm_jdt_s), getXByProgress(70), 70),
                new DrawableWithLocation(U.getDrawable(R.drawable.ycjm_jdt_ss), getXByProgress(90), 90),
                new DrawableWithLocation(U.getDrawable(R.drawable.ycjm_jdt_sss), getXByProgress(95), 95),
        };

    }

    private int getXByProgress(int p) {
        float temp = p * (w1 + extendX - sx) / 100.0f + sx;
        return (int) temp;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mBgDrawable.setBounds(0, 0, w1, h1);
        mBgDrawable.draw(canvas);

        canvas.save();
        float temp = mCurProgress * (w1 + extendX - sx) / 100.0f;

        Path path = new Path();
        path.lineTo(temp + sx, 0);
        path.lineTo(temp + sx - px, h1);
        path.lineTo(0, h1);
        path.close();
        canvas.clipPath(path);
//        canvas.drawColor(Color.RED);
        mPrDrawable.setBounds(tx, ty, w2 + tx, h2 + ty);
        mPrDrawable.draw(canvas);
        canvas.restore();

        if (mCurProgress != mProgress) {
            float cx = temp + sx - px / 2;
            float cy = h1 / 2;
            float r = h1 / 2;
            //画光圈
            Shader shader = new RadialGradient(cx, cy, r, Color.parseColor("#FD2C9A"), 0x00000000, Shader.TileMode.CLAMP);
            mPaintCircle.setShader(shader);
            mPaintCircle.setAlpha(235);
            canvas.drawCircle(cx, cy, r, mPaintCircle);
        }

        if (mPrgress2 > 0) {
            // 画进度条
            canvas.drawArc(mRectF, 80, -360 * mPrgress2 / 100.0f, false, mPaintCircleBar);
        }

        // 画level图标
        for (int i = 0; i < mLevelDrawables.length; i++) {
            DrawableWithLocation drawableW = mLevelDrawables[i];

            int oy = 0;
            Drawable drawable = drawableW.getDrawable();
            int ox = drawableW.getTransLateX() - drawable.getIntrinsicWidth() / 2;
            drawable.setBounds(ox, oy, ox + drawable.getIntrinsicWidth(), oy + drawable.getIntrinsicHeight());
            drawable.draw(canvas);
        }
    }

    public int getStarXByScore(int p) {
        for (int i = mLevelDrawables.length - 1; i >= 0; i--) {
            DrawableWithLocation drawableW = mLevelDrawables[i];
            if (p > drawableW.score) {
                return drawableW.transLateX - drawableW.mDrawable.getIntrinsicWidth() / 2;
            }
        }
        return -1;
    }
//    boolean tryPostInvalidateDelayed() {
//        if (mCurProgress == mProgress) {
//            return false;
//        }
//        int speed = getSpeed();
//        if (mCurProgress < mProgress) {
//            int np = mCurProgress + speed;
//            if (np > mProgress) {
//                np = mProgress;
//            }
//            mCurProgress = np;
//        }
//        if (mCurProgress > mProgress) {
//            int np = mCurProgress - speed;
//            if (np < mProgress) {
//                np = mProgress;
//            }
//            mCurProgress = np;
//        }
//        MyLog.d(TAG, "tryPostInvalidateDelayed curProgress:" + mCurProgress);
//
//        postInvalidateDelayed(30);
//        return true;
//    }

//    int getSpeed() {
////        oldProgress,progress,curProgress
//        int speed = Math.abs((mProgress - mCurProgress) / 4);
//        MyLog.d(TAG, "getSpeed progress=" + mProgress + " curProgress=" + mCurProgress + " speed=" + speed);
//        if (speed < 2) {
//            speed = 2;
//        }
//        return speed;
//    }

    ValueAnimator mValueAnimator;

    public void setProgress1(int p) {
        MyLog.d(TAG, "setProgress" + " p=" + p);
        this.mOldProgress = mProgress;
        this.mProgress = p;
        if (mValueAnimator == null) {
            mValueAnimator = ValueAnimator.ofInt(0, mProgress);
            mValueAnimator.setInterpolator(new DecelerateInterpolator());
            mValueAnimator.setDuration(500);
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCurProgress = (int) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mValueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                }
            });
        }
        mValueAnimator.cancel();
        mValueAnimator.setIntValues(0, mProgress);
        mValueAnimator.start();
//        tryPostInvalidateDelayed();
    }

    public void setProgress2(int p) {
        mPrgress2 = p;
        invalidate();
    }

    static class DrawableWithLocation {
        Drawable mDrawable;
        int transLateX;
        int score;

        public DrawableWithLocation(Drawable drawable, int transLateX, int score) {
            mDrawable = drawable;
            this.transLateX = transLateX;
            this.score = score;
        }

        public Drawable getDrawable() {
            return mDrawable;
        }

        public int getTransLateX() {
            return transLateX;
        }

        public int getScore() {
            return score;
        }

    }
}
