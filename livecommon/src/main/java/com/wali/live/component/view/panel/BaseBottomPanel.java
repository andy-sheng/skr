package com.wali.live.component.view.panel;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.support.annotation.CallSuper;
import android.support.annotation.FloatRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.thornbirds.component.view.IOrientationListener;

/**
 * Created by yangli on 17-2-14.
 *
 * @module 底部面板
 */
public abstract class BaseBottomPanel<CONTENT extends View, CONTAINER extends ViewGroup>
        implements IOrientationListener {
    protected String TAG = getTAG();

    protected final static int PANEL_WIDTH_LANDSCAPE = GlobalData.screenWidth;

    @NonNull
    protected CONTAINER mParentView;
    @NonNull
    protected CONTENT mContentView;
    @NonNull
    private AnimationHelper mAnimationHelper;

    protected boolean mIsLandscape = false;
    protected boolean mHasFirstOriented = false;

    protected boolean mIsShow = false;

    @LayoutRes
    protected abstract int getLayoutResId();

    protected final <T> T $(int id) {
        return (T) mContentView.findViewById(id);
    }

    protected final void $click(int id, View.OnClickListener listener) {
        mContentView.findViewById(id).setOnClickListener(listener);
    }

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    protected String getTAG() {
        return BaseBottomPanel.class.getSimpleName();
    }

    public BaseBottomPanel(@NonNull CONTAINER parentView) {
        mParentView = parentView;
    }

    private boolean containsInParent() {
        return mContentView != null && mParentView.indexOfChild(mContentView) != -1;
    }

    @CallSuper
    protected void inflateContentView() {
        mContentView = (CONTENT) LayoutInflater.from(
                mParentView.getContext()).inflate(getLayoutResId(), mParentView, false);
    }

    public boolean isShow() {
        return mIsShow;
    }

    public void showSelf(boolean useAnimation, boolean isLandscape) {
        if (mIsShow) {
            return;
        }
        mIsShow = true;
        if (!containsInParent()) {
            addSelfToParent();
            onOrientation(isLandscape);
        } else if (mIsLandscape != isLandscape) {
            onOrientation(isLandscape);
        }
        if (useAnimation) {
            if (mAnimationHelper == null) {
                mAnimationHelper = createAnimationHelper();
            }
            mAnimationHelper.startAnimation();
        } else {
            mContentView.setVisibility(View.VISIBLE);
        }
    }

    protected AnimationHelper createAnimationHelper() {
        return new AnimationHelper();
    }

    public void hideSelf(boolean useAnimation) {
        if (!mIsShow) {
            return;
        }
        mIsShow = false;
        if (containsInParent()) {
            if (useAnimation && mAnimationHelper != null) {
                mAnimationHelper.startAnimation();
            } else {
                removeSelfFromParent();
            }
        }
    }

    protected void orientSelf() {
        ViewGroup.LayoutParams layoutParams = mContentView.getLayoutParams();
        if (mIsLandscape) {
            layoutParams.width = PANEL_WIDTH_LANDSCAPE;
        } else {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        mContentView.setLayoutParams(layoutParams);
    }

    @Override
    public final void onOrientation(boolean isLandscape) {
        MyLog.w(TAG, "onOrientation isLandscape=" + isLandscape);
        // 增加mHasFirstOriented变量，标识首次的初始化
        if (mIsLandscape != isLandscape || !mHasFirstOriented) {
            mHasFirstOriented = true;
            mIsLandscape = isLandscape;
            orientSelf();
        }
    }

    private void addSelfToParent() {
        MyLog.w(TAG, "addSelfToParent");
        if (mContentView == null) {
            inflateContentView();
        }
        if (mParentView.getChildCount() == 0) {
            mParentView.setVisibility(View.VISIBLE);
        }
        mParentView.addView(mContentView);
        mContentView.setVisibility(View.INVISIBLE);
    }

    private void removeSelfFromParent() {
        if (containsInParent()) {
            MyLog.w(TAG, "removeSelfFromParent");
            mParentView.removeView(mContentView);
            if (mParentView.getChildCount() == 0) {
                mParentView.setVisibility(View.GONE);
            }
        } else {
            MyLog.w(TAG, "removeSelfFromParent, but contentView has not been added");
        }
    }

    @CallSuper
    public void clearAnimation() {
        if (mAnimationHelper != null) {
            mAnimationHelper.stopAnimation();
        }
    }

    @CallSuper
    protected void onAnimationStart() {
        MyLog.w(TAG, "onAnimationStart isShow=" + mIsShow);
        if (mIsShow) {
            mContentView.setAlpha(0.0f);
            mContentView.setVisibility(View.VISIBLE);
        }
    }

    protected void onAnimationValue(@FloatRange(from = 0.0, to = 1.0) float value) {
        mContentView.setAlpha(value);
        if (mIsLandscape) {
            mContentView.setTranslationX(mContentView.getWidth() * (1.0f - value));
            mContentView.setTranslationY(0);
        } else {
            mContentView.setTranslationX(0);
            mContentView.setTranslationY(mContentView.getHeight() * (1.0f - value));
        }
    }

    @CallSuper
    protected void onAnimationEnd() {
        MyLog.w(TAG, "onAnimationEnd isShow=" + mIsShow);
        if (!mIsShow) {
            removeSelfFromParent();
        }
    }

    // 面板动画辅助类
    protected class AnimationHelper {
        protected ValueAnimator valueAnimator;

        protected void setupAnimation() {
            if (valueAnimator == null) {
                valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
                valueAnimator.setDuration(300);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float ratio = (float) animation.getAnimatedValue();
                        if (!mIsShow) {
                            ratio = 1 - ratio;
                        }
                        BaseBottomPanel.this.onAnimationValue(ratio);
                    }
                });
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        BaseBottomPanel.this.onAnimationStart();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        BaseBottomPanel.this.onAnimationEnd();
                    }
                });
            }
        }

        protected void startAnimation() {
            setupAnimation();
            if (!valueAnimator.isRunning()) {
                valueAnimator.start();
            }
        }

        protected void stopAnimation() {
            if (valueAnimator != null && valueAnimator.isStarted()) {
                valueAnimator.cancel();
            }
        }
    }
}
