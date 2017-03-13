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

/**
 * Created by yangli on 17-2-14.
 *
 * @module 底部面板
 */
public abstract class BaseBottomPanel<CONTENT extends View, CONTAINER extends ViewGroup> {
    protected String TAG = getTAG();

    protected final static int PANEL_WIDTH_LANDSCAPE = GlobalData.screenWidth;

    @NonNull
    protected CONTAINER mParentView;
    @NonNull
    protected CONTENT mContentView;
    @NonNull
    protected AnimationHelper mAnimationHelper;

    protected boolean mIsLandscape = false;

    @LayoutRes
    protected abstract int getLayoutResId();

    protected <T> T $(int id) {
        return (T) mContentView.findViewById(id);
    }

    protected void $click(int id, View.OnClickListener listener) {
        mContentView.findViewById(id).setOnClickListener(listener);
    }

    protected String getTAG() {
        return BaseBottomPanel.class.getSimpleName();
    }

    public BaseBottomPanel(@NonNull CONTAINER parentView) {
        mParentView = parentView;
    }

    private boolean containsInParent() {
        return mParentView.indexOfChild(mContentView) != -1;
    }

    @CallSuper
    protected void inflateContentView() {
        mContentView = (CONTENT) LayoutInflater.from(
                mParentView.getContext()).inflate(getLayoutResId(), mParentView, false);
    }

    public void showSelf(boolean useAnimation, boolean isLandscape) {
        if (!containsInParent()) {
            addSelfToParent();
            onOrientation(isLandscape);
        } else if (mIsLandscape != isLandscape) {
            onOrientation(isLandscape);
        }
        if (useAnimation) {
            if (mAnimationHelper == null) {
                mAnimationHelper = new AnimationHelper();
            }
            mAnimationHelper.startAnimation(true);
        } else {
            mContentView.setVisibility(View.VISIBLE);
        }
    }

    public void hideSelf(boolean useAnimation) {
        if (containsInParent()) {
            if (useAnimation && mAnimationHelper != null) {
                mAnimationHelper.startAnimation(false);
            } else {
                removeSelfFromParent();
            }
        }
    }

    @CallSuper
    public void onOrientation(boolean isLandscape) {
        MyLog.w(TAG, "onOrientation isLandscape=" + isLandscape);
        mIsLandscape = isLandscape;
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
    protected void onAnimationStart(boolean isShow) {
        MyLog.w(TAG, "onAnimationStart isShow=" + isShow);
        if (isShow) {
            mContentView.setAlpha(0.0f);
            mContentView.setVisibility(View.VISIBLE);
        }
    }

    protected void onAnimationValue(
            @FloatRange(from = 0.0, to = 1.0) float value, boolean isShow) {
        mContentView.setAlpha(value);
        // TODO setTranslation会有问题，后续处理 YangLi
//        if (mIsLandscape) {
//            mContentView.setTranslationX(mContentView.getMeasuredWidth() * (1.0f - value));
//            mContentView.setTranslationY(0);
//        } else {
//            mContentView.setTranslationX(0);
//            mContentView.setTranslationY(mContentView.getMeasuredHeight() * (1.0f - value));
//        }
    }

    @CallSuper
    protected void onAnimationEnd(boolean isShow) {
        MyLog.w(TAG, "onAnimationEnd isShow=" + isShow);
        if (!isShow) {
            removeSelfFromParent();
        }
    }

    // 面板动画辅助类
    protected class AnimationHelper {
        private ValueAnimator valueAnimator;
        private boolean isShow = false;

        private void setupAnimation() {
            if (valueAnimator == null) {
                valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
                valueAnimator.setDuration(300);
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float ratio = (float) animation.getAnimatedValue();
                        if (!isShow) {
                            ratio = 1 - ratio;
                        }
                        BaseBottomPanel.this.onAnimationValue(ratio, isShow);
                    }
                });
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        BaseBottomPanel.this.onAnimationStart(isShow);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        BaseBottomPanel.this.onAnimationEnd(isShow);
                    }
                });
            }
        }

        private void startAnimation(boolean isShow) {
            setupAnimation();
            this.isShow = isShow;
            if (!valueAnimator.isRunning()) {
                valueAnimator.start();
            }
        }

        private void stopAnimation() {
            if (valueAnimator != null && valueAnimator.isStarted()) {
                valueAnimator.cancel();
            }
        }
    }
}
