package com.module.playways.rank.room.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.common.utils.U;
import com.module.playways.rank.room.utils.ScoreAnimationHelp;
import com.module.rank.R;

/**
 * DashboardView style 2 仿芝麻信用分
 * Created by woxingxiao on 2016-11-19.
 */

public class RecordCircleView extends View {

    private int mRadius; // 画布边缘半径（去除padding后的半径）
    private int mStartAngle = 120; // 起始角度
    private int mSweepAngle = 300; // 绘制角度
    //最外面边框的大小
    private int mStrokeWidth = U.getDisplayUtils().dip2px(2);
    private int mArrowHeight = 10;
    private int mMin = 0; // 最小值
    private int mMax = 900; // 最大值
    //从哪里开始转
    private int mStart = 300;
    private int mCreditValue = 650; // 信用分
    private int mSolidCreditValue = mCreditValue; // 信用分(设定后不变)
    private int mSparkleWidth; // 亮点宽度
    private int mProgressWidth; // 进度圆弧宽度
    private float mLength1; // 刻度顶部相对边缘的长度
    private int mCalibrationWidth; // 刻度圆弧宽度
    private float mLength2; // 刻度读数顶部相对边缘的长度

    private int mPadding;
    private float mCenterX, mCenterY; // 圆心坐标
    private Paint mPaint;
    private RectF mRectFProgressArc;
    private RectF mRectFCalibrationFArc;
    private RectF mRectFTextArc;
    private Path mPath;
    private Rect mRectText;

    private int mProtect;

    boolean mFullLevel = false;

    ScoreAnimationHelp.AnimationListener mAnimationListener;
    /**
     * 由于真实的芝麻信用界面信用值不是线性排布，所以播放动画时若以信用值为参考，则会出现忽慢忽快
     * 的情况（开始以为是卡顿）。因此，先计算出最终到达角度，以扫过的角度为线性参考，动画就流畅了
     */
    private boolean isAnimFinish = true;
    private float mAngleWhenAnim;

    public RecordCircleView(Context context) {
        this(context, null);
    }

    public RecordCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mProgressWidth = U.getDisplayUtils().dip2px(8);
        mCalibrationWidth = dp2px(10);
        mSparkleWidth = U.getDisplayUtils().dip2px(8);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mRectFProgressArc = new RectF();
        mRectFCalibrationFArc = new RectF();
        mRectFTextArc = new RectF();
        mPath = new Path();
        mRectText = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mPadding = Math.max(
                Math.max(getPaddingLeft(), getPaddingTop()),
                Math.max(getPaddingRight(), getPaddingBottom())
        );
        setPadding(mPadding, mPadding, mPadding, mPadding);

        mLength1 = mPadding + mSparkleWidth / 2f + dp2px(8);
        mLength2 = mLength1 + mCalibrationWidth + dp2px(1) + dp2px(5);

        int width = resolveSize(dp2px(100), widthMeasureSpec);
        mRadius = (width - mPadding * 2) / 2;

//        setMeasuredDimension(width, width);

        mCenterX = mCenterY = getMeasuredWidth() / 2f;
        mRectFProgressArc.set(
                mPadding + mSparkleWidth / 2f + mArrowHeight,
                mPadding + mSparkleWidth / 2f + mArrowHeight,
                getMeasuredWidth() - mPadding - mSparkleWidth / 2f - mArrowHeight,
                getMeasuredWidth() - mPadding - mSparkleWidth / 2f - mArrowHeight
        );

        mRectFCalibrationFArc.set(
                mLength1 + mCalibrationWidth / 2f,
                mLength1 + mCalibrationWidth / 2f,
                getMeasuredWidth() - mLength1 - mCalibrationWidth / 2f,
                getMeasuredWidth() - mLength1 - mCalibrationWidth / 2f
        );

        mPaint.setTextSize(sp2px(10));
        mPaint.getTextBounds("0", 0, "0".length(), mRectText);
        mRectFTextArc.set(
                mLength2 + mRectText.height(),
                mLength2 + mRectText.height(),
                getMeasuredWidth() - mLength2 - mRectText.height(),
                getMeasuredWidth() - mLength2 - mRectText.height()
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /**
         * 画进度圆弧背景
         */
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mProgressWidth);
        mPaint.setAlpha(255);
        mPaint.setShader(generateOutSweepGradient());
        canvas.drawArc(mRectFProgressArc, mStartAngle + 1, mSweepAngle - 2, false, mPaint);
        mPaint.setStrokeWidth(mProgressWidth - mStrokeWidth * 2);
        mPaint.setShader(generateInnerSweepGradient());
        canvas.drawArc(mRectFProgressArc, mStartAngle + 1, mSweepAngle - 2, false, mPaint);

        mPaint.setAlpha(255);

        if(mFullLevel){
            mPaint.setShader(generateSweepGradient());
            canvas.drawArc(mRectFProgressArc, mStartAngle + 1,
                    mSweepAngle, false, mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setShader(null);
            mPaint.setAlpha(255);
            mPaint.setTextSize(sp2px(15));
            mPaint.setTextAlign(Paint.Align.CENTER);
            mPaint.setColor(Color.WHITE);
            canvas.drawText("满级", mCenterX, mCenterY + U.getDisplayUtils().dip2px(5), mPaint);
            return;
        }

        if (isAnimFinish) {
            /**
             * 画进度圆弧(起始到信用值)
             */
            mPaint.setShader(generateSweepGradient());
            canvas.drawArc(mRectFProgressArc, mStartAngle + 1,
                    calculateRelativeAngleWithValue(mSolidCreditValue), false, mPaint);
            /**
             * 画信用值指示亮点
             */
//            float[] point = getCoordinatePoint(
//                    mRadius - mSparkleWidth / 2f,
//                    mStartAngle + calculateRelativeAngleWithValue(mCreditValue)
//            );
//            mPaint.setStyle(Paint.Style.FILL);
//            mPaint.setShader(generateRadialGradient(point[0], point[1]));
//            canvas.drawCircle(point[0], point[1], mSparkleWidth / 2f, mPaint);

//            canvas.save();
//            canvas.rotate(mAngleWhenAnim + 90, mCenterX, mCenterY);
//            Drawable d = getResources().getDrawable(R.drawable.zhanji_sanjiaoxing);
//            int startX = (int) mCenterX - 10;
//            int startY = 0;
//            int width = (int) mCenterX + 10;
//            int height = 20;
//            d.setBounds(startX, startY, width, height);
//            d.draw(canvas);
//            canvas.restore();
        } else {
            /**
             * 画进度圆弧(起始到信用值)
             */
            mPaint.setShader(generateSweepGradient());
            canvas.drawArc(mRectFProgressArc, mStartAngle + 1,
                    mAngleWhenAnim - mStartAngle - 2, false, mPaint);
//            /**
//             * 画信用值指示亮点
//             */
//            float[] point = getCoordinatePoint(
//                    mRadius - mSparkleWidth / 2f,
//                    mAngleWhenAnim
//            );
//            mPaint.setStyle(Paint.Style.FILL);
//            mPaint.setShader(generateRadialGradient(point[0], point[1]));
//            canvas.drawCircle(point[0], point[1], mSparkleWidth / 2f, mPaint);

        }

        float degree = calculateRelativeAngleWithValue(mProtect) + mStartAngle;
        canvas.save();
        canvas.rotate(degree + 90, mCenterX, mCenterY);
        Drawable d = getResources().getDrawable(R.drawable.zhanji_sanjiaoxing);
        int startX = (int) mCenterX - 10;
        int startY = 0;
        int width = (int) mCenterX + 10;
        int height = 20;
        d.setBounds(startX, startY, width, height);
        d.draw(canvas);
        canvas.restore();


        /**
         * 画实时度数值
         */
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setShader(null);
        mPaint.setAlpha(255);
        mPaint.setTextSize(sp2px(15));
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setColor(Color.WHITE);
        String value = String.valueOf(mSolidCreditValue);
        canvas.drawText(value, mCenterX, mCenterY + dp2px(10), mPaint);

        mPaint.setTextSize(sp2px(8));
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setColor(getResources().getColor(R.color.white_trans_50));
        canvas.drawText("当前", mCenterX, mCenterY - dp2px(6), mPaint);
        canvas.drawText(mMax + "", mCenterX + dp2px(10), mCenterY + dp2px(32), mPaint);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    private int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                Resources.getSystem().getDisplayMetrics());
    }

    private SweepGradient generateInnerSweepGradient() {
        SweepGradient sweepGradient = new SweepGradient(mCenterX, mCenterY,
                new int[]{Color.GRAY, Color.GRAY},
                new float[]{0, 0}
        );

        Matrix matrix = new Matrix();
        matrix.setRotate(mStartAngle - 10, mCenterX, mCenterY);
        sweepGradient.setLocalMatrix(matrix);

        return sweepGradient;
    }

    private SweepGradient generateSweepGradient() {
        SweepGradient sweepGradient = new SweepGradient(mCenterX, mCenterY,
                new int[]{0xFFDA8E00, 0xFFFFED61},
                new float[]{0, calculateRelativeAngleWithValue(mCreditValue) / 360}
        );

        Matrix matrix = new Matrix();
        matrix.setRotate(mStartAngle - 10, mCenterX, mCenterY);
        sweepGradient.setLocalMatrix(matrix);
        return sweepGradient;
    }

    private SweepGradient generateOutSweepGradient() {
        SweepGradient sweepGradient = new SweepGradient(mCenterX, mCenterY,
                new int[]{0xFF0C2275, 0xFF0C2275},
                new float[]{0, 0}
        );

        Matrix matrix = new Matrix();
        matrix.setRotate(mStartAngle - 10, mCenterX, mCenterY);
        sweepGradient.setLocalMatrix(matrix);

        return sweepGradient;
    }

    private RadialGradient generateRadialGradient(float x, float y) {
        return new RadialGradient(x, y, mSparkleWidth / 2f,
                new int[]{Color.argb(255, 255, 255, 255), Color.argb(80, 255, 255, 255)},
                new float[]{0.4f, 1},
                Shader.TileMode.CLAMP
        );
    }

    private float[] getCoordinatePoint(float radius, float angle) {
        float[] point = new float[2];

        double arcAngle = Math.toRadians(angle); //将角度转换为弧度
        if (angle < 90) {
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (angle == 90) {
            point[0] = mCenterX;
            point[1] = mCenterY + radius;
        } else if (angle > 90 && angle < 180) {
            arcAngle = Math.PI * (180 - angle) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (angle == 180) {
            point[0] = mCenterX - radius;
            point[1] = mCenterY;
        } else if (angle > 180 && angle < 270) {
            arcAngle = Math.PI * (angle - 180) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        } else if (angle == 270) {
            point[0] = mCenterX;
            point[1] = mCenterY - radius;
        } else {
            arcAngle = Math.PI * (360 - angle) / 180.0;
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        }

        return point;
    }

    /**
     * 相对起始角度计算信用分所对应的角度大小
     */
    private float calculateRelativeAngleWithValue(int value) {
        int v = mMax - mMin;
        return ((float) mSweepAngle / ((float) mMax - (float) mMin)) * value;
    }

    public void fullLevel() {
        mFullLevel = true;
        postInvalidate();
    }

    public void setData(int min, int max, int cur, int target, int protect, ScoreAnimationHelp.AnimationListener listener) {
        mFullLevel = false;
        mMin = 0;
        mMax = max;
        mStart = cur;
        mSolidCreditValue = target;
        mProtect = protect;

        mAnimationListener = listener;
        setCreditValueWithAnim(target);
    }

    /**
     * 设置信用值并播放动画
     *
     * @param creditValue 信用值
     */
    public void setCreditValueWithAnim(int creditValue) {
        if (creditValue < mMin || creditValue > mMax || !isAnimFinish) {
            return;
        }

        setVisibility(VISIBLE);

        mSolidCreditValue = creditValue;

        ValueAnimator creditValueAnimator = ValueAnimator.ofInt(mStart, mSolidCreditValue);
        creditValueAnimator.setInterpolator(new DecelerateInterpolator());
        creditValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCreditValue = (int) animation.getAnimatedValue();
                postInvalidate();
            }
        });

        // 计算最终值对应的角度，以扫过的角度的线性变化来播放动画
        float degree = calculateRelativeAngleWithValue(mSolidCreditValue);

        ValueAnimator degreeValueAnimator = ValueAnimator.ofFloat(mStartAngle + calculateRelativeAngleWithValue(mStart), mStartAngle + degree);
        degreeValueAnimator.setInterpolator(new DecelerateInterpolator());
        degreeValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAngleWhenAnim = (float) animation.getAnimatedValue();
            }
        });

        long delay = 5 * Math.abs(creditValue - mStart);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet
                .setDuration(delay)
                .playTogether(creditValueAnimator, degreeValueAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                isAnimFinish = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isAnimFinish = true;
                if (mAnimationListener != null) {
                    mAnimationListener.onFinish();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                onAnimationEnd(animation);
                isAnimFinish = true;
            }
        });
        animatorSet.start();
    }

}
