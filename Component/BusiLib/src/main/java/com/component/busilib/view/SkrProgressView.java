package com.component.busilib.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.component.busilib.R;

// 公用的进度条
public class SkrProgressView extends ConstraintLayout {

    ConstraintLayout mContainer;
    ExImageView mBgIv;
    ImageView mProgressIv;
    TextView mProgressTv;

    boolean isClickCanThrough = false;
    boolean hasAnimation = true;
    Drawable mProgressDrwable;
    String mProgressText;

    ObjectAnimator mAnimator;

    public SkrProgressView(Context context) {
        super(context);
        initView(context, null);
    }

    public SkrProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public SkrProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.common_progress_view_layout, this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SkrProgressView);
        isClickCanThrough = typedArray.getBoolean(R.styleable.SkrProgressView_clickCanThrough, false);
        hasAnimation = typedArray.getBoolean(R.styleable.SkrProgressView_hasAnimation, true);
        mProgressDrwable = typedArray.getDrawable(R.styleable.SkrProgressView_progressDrawable);
        mProgressText = typedArray.getString(R.styleable.SkrProgressView_progressText);
        typedArray.recycle();

        mContainer = findViewById(R.id.container);
        mBgIv = findViewById(R.id.bg_iv);
        mProgressIv = findViewById(R.id.progress_iv);
        mProgressTv = findViewById(R.id.progress_tv);
    }

    public void setProgressText(CharSequence text) {
        mProgressTv.setText(text);
    }

    public void setProgressDrwable(Drawable drwable) {
        mProgressIv.setBackground(drwable);
    }

    private void tryShow() {
        mProgressTv.setText(mProgressText);
        if (mProgressDrwable != null) {
            mProgressIv.setBackground(mProgressDrwable);
        } else {
            mProgressIv.setBackground(U.getDrawable(R.drawable.common_progress_bar_icon));
        }
        if (!isClickCanThrough) {
            mContainer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }
        if (hasAnimation) {
            if (mAnimator == null) {
                mAnimator = ObjectAnimator.ofFloat(mProgressIv, View.ROTATION, 0f, 360f);
                mAnimator.setDuration(10000);
                mAnimator.setInterpolator(new LinearInterpolator());
                mAnimator.setRepeatCount(Animation.INFINITE);
            }
            mAnimator.start();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            tryShow();
        } else {
            if (mAnimator != null) {
                mAnimator.cancel();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }
}
