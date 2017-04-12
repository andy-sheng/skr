package com.base.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.base.common.R;
import com.base.utils.display.DisplayUtils;

/**
 * Created by lan on 16/12/14.
 */
public class MyRatingBar extends View {
    private static final int STAR_NUM = 5;
    private static final float STAR_RATING = 3.5f;
    private static final int STAR_PADDING = DisplayUtils.dip2px(1.67f);
    private static final int STAR_WIDTH = DisplayUtils.dip2px(9.33f);

    private int mStarNum;
    private Drawable mBgDrawable;
    private Drawable mFgDrawable;
    private Drawable mHalfFgDrawable;
    private int mStarPadding;
    private int mStarWidth;
    private boolean mUseDrawableWidth;

    private int mWidth;
    private int mHeight;

    private float mStarValue;

    public MyRatingBar(Context context) {
        super(context);
        init(null);
    }

    public MyRatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MyRatingBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MyRatingBar);
            mStarNum = a.getInt(R.styleable.MyRatingBar_numStars, STAR_NUM);
            mStarValue = a.getFloat(R.styleable.MyRatingBar_ratingStars, STAR_RATING);
            if (a.hasValue(R.styleable.MyRatingBar_backgroundDrawable)) {
                mBgDrawable = a.getDrawable(R.styleable.MyRatingBar_backgroundDrawable);
            }
            if (a.hasValue(R.styleable.MyRatingBar_foregroundDrawable)) {
                mFgDrawable = a.getDrawable(R.styleable.MyRatingBar_foregroundDrawable);
            }
            if (a.hasValue(R.styleable.MyRatingBar_halfForegroundDrawable)) {
                mHalfFgDrawable = a.getDrawable(R.styleable.MyRatingBar_halfForegroundDrawable);
            }
            mStarPadding = a.getDimensionPixelSize(R.styleable.MyRatingBar_starPadding, STAR_PADDING);
            mUseDrawableWidth = a.getBoolean(R.styleable.MyRatingBar_useDrawableWidth, false);
            if (!mUseDrawableWidth) {
                mStarWidth = a.getDimensionPixelSize(R.styleable.MyRatingBar_starWidth, STAR_WIDTH);
            }
            a.recycle();
        }
        if (mBgDrawable == null) {
            mBgDrawable = getResources().getDrawable(R.drawable.game_score_star_dim);
        }
        if (mFgDrawable == null) {
            mFgDrawable = getResources().getDrawable(R.drawable.game_score_star_light);
        }
        if (mHalfFgDrawable == null) {
            mHalfFgDrawable = getResources().getDrawable(R.drawable.game_score_star_half);
        }
        if (mUseDrawableWidth) {
            mStarWidth = mBgDrawable.getIntrinsicWidth();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mWidth = getWidth();
        mHeight = getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int starValueInt = (int) mStarValue;
        for (int i = 0; i < starValueInt; i++) {
            drawStar(canvas, mFgDrawable, i);
        }
        for (int i = starValueInt + 1; i < mStarNum; i++) {
            drawStar(canvas, mBgDrawable, i);
        }
        drawStar(canvas, starValueInt != mStarValue ? mHalfFgDrawable : mBgDrawable, starValueInt);
    }

    private void drawStar(Canvas canvas, Drawable drawable, int i) {
        int start = i * (mStarWidth + mStarPadding);
        drawable.setBounds(start, 0, start + mStarWidth, mStarWidth);
        drawable.draw(canvas);
    }

    public void setStarValue(float starValue) {
        if (starValue < 0 || starValue > mStarNum) {
            return;
        }
        mStarValue = starValue;
        invalidate();
    }
}
