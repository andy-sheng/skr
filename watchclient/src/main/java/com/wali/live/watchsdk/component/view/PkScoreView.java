package com.wali.live.watchsdk.component.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.wali.live.watchsdk.R;

import static android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT;
import static android.graphics.drawable.GradientDrawable.Orientation.RIGHT_LEFT;

/**
 * Created by yangli on 2017/9/11.
 */
public class PkScoreView extends View {

    private GradientDrawable mLeftDrawable;
    private GradientDrawable mRightDrawable;

    private int mWidth = 0;
    private int mHeight = 0;
    private int mSplitPosition = 0;
    private float mRatio = 0.0f;

    public PkScoreView(Context context) {
        this(context, null);
    }

    public PkScoreView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PkScoreView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        int leftFromColor = getResources().getColor(R.color.color_ff6100);
        int leftToColor = getResources().getColor(R.color.color_ffd121);
        int rightFromColor = getResources().getColor(R.color.color_3961f4);
        int rightToColor = getResources().getColor(R.color.color_95daff);
        mLeftDrawable = new GradientDrawable(LEFT_RIGHT, new int[]{leftFromColor, leftToColor});
        mRightDrawable = new GradientDrawable(RIGHT_LEFT, new int[]{rightFromColor, rightToColor});
    }

    public void updateRatio(long score1, long score2) {
        float ratio = score1 == score2 ? 0.5f : (float) score1 / (score1 + score2);
        if (Math.abs(mRatio - ratio) <= 0.001f) {
            return;
        }
        mRatio = ratio;
        mSplitPosition = (int) (mHeight / 2 + mRatio * (mWidth - mHeight));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth(), height = getHeight();
        if (mWidth != width || mHeight != height) {
            final int radius = height / 2;
            if (mHeight != height) {
                mLeftDrawable.setCornerRadii(new float[]{radius, radius, 0, 0, 0, 0, radius, radius});
                mRightDrawable.setCornerRadii(new float[]{0, 0, radius, radius, radius, radius, 0, 0});
            }
            mWidth = width;
            mHeight = height;
            mSplitPosition = (int) (radius + mRatio * (mWidth - mHeight));
        }
        mLeftDrawable.setBounds(0, 0, mSplitPosition, mHeight);
        mLeftDrawable.draw(canvas);
        mRightDrawable.setBounds(mSplitPosition, 0, mWidth, mHeight);
        mRightDrawable.draw(canvas);
    }

}
