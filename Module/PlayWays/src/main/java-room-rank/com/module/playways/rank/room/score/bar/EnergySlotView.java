package com.module.playways.rank.room.score.bar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.common.utils.U;
import com.module.rank.R;


public class EnergySlotView extends View {
    int mWidth = -1;// view的宽度
    int mHeight = -1;// view的高度

    Drawable mFullEnergyDrawable;
    Drawable mEmptyEnergyDrawable;

    AnimatorSet mAnimatorSet;

    int mCur = 0;
    int mTarget = 100;

    public EnergySlotView(Context context) {
        this(context, null);
        init();
    }

    public EnergySlotView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public EnergySlotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getWidth();
        mHeight = getHeight();
    }

    private void init() {
        mFullEnergyDrawable = U.getDrawable(R.drawable.xulichi_man);
        mEmptyEnergyDrawable = U.getDrawable(R.drawable.xulichi_kong);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                setTarget(0, null);
            }
        }, 500);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        postInvalidateDelayed(100);
        if (mWidth <= 0 || mHeight <= 0) {
            return;
        }

        Rect rect1 = new Rect(0, 0, mFullEnergyDrawable.getIntrinsicWidth(), mFullEnergyDrawable.getIntrinsicHeight());
        mFullEnergyDrawable.setBounds(rect1);
        canvas.save();
        canvas.clipRect(getLeftClipRect());
        mFullEnergyDrawable.draw(canvas);
        canvas.restore();

        canvas.save();
        Rect rect2 = new Rect(0, 0, mEmptyEnergyDrawable.getIntrinsicWidth(), mEmptyEnergyDrawable.getIntrinsicHeight());
        mEmptyEnergyDrawable.setBounds(rect2);
        canvas.clipRect(getRightClipRect());
        mEmptyEnergyDrawable.draw(canvas);
        canvas.restore();
    }

    public void reset() {
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.cancel();
        }

        mTarget = 0;
        postInvalidate();
    }

    public void setTarget(int target, AnimatorListenerAdapter mAnimatorListenerAdapter) {
        mTarget = target;

        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.cancel();
        }

        ValueAnimator creditValueAnimator = ValueAnimator.ofInt(mCur, target);
        creditValueAnimator.setInterpolator(new DecelerateInterpolator());
        creditValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCur = (int) animation.getAnimatedValue();
                postInvalidate();
            }
        });


        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mAnimatorListenerAdapter != null) {
                    mAnimatorListenerAdapter.onAnimationEnd(animation);
                }
            }
        });

        mAnimatorSet.setDuration(Math.abs(target - mCur) * 20)
                .play(creditValueAnimator);

        mAnimatorSet.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.cancel();
        }
    }

    private RectF getLeftClipRect() {
        int width = mWidth * mCur / 100;
        return new RectF(0, 0, width, mHeight);
    }

    private RectF getRightClipRect() {
        int left = mWidth * mCur / 100;
        return new RectF(left, 0, mWidth, mHeight);
    }
}
