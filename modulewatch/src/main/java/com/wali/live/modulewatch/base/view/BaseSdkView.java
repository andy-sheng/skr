package com.wali.live.modulewatch.base.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import com.common.log.MyLog;
import com.thornbirds.component.ComponentView;
import com.thornbirds.component.IEventObserver;
import com.wali.live.modulewatch.base.component.BaseSdkController;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by yangli on 2017/8/2.
 *
 * @module 基础架构页面
 */
public abstract class BaseSdkView<VIEW extends View, CONTROLLER extends BaseSdkController>
        extends ComponentView<VIEW, CONTROLLER> implements IEventObserver {

    protected Activity mActivity;

    protected final <T> T deRef(WeakReference reference) {
        return reference != null ? (T) reference.get() : null;
    }

    protected final void addViewToSet(int[] idSet, List<View>... listSet) {
        if (idSet == null || listSet == null) {
            return;
        }
        for (int id : idSet) {
            View view = $(id);
            for (List<View> viewSet : listSet) {
                viewSet.add(view);
            }
        }
    }

//    protected final void addViewToSet(int parentViewId ,int[] childIdSet, List<View>... listSet) {
//        if ( listSet == null) {
//            return;
//        }
//        View parentView = $(parentViewId);
//        for (int childId : childIdSet) {
//            View view = parentView.findViewById(childId);
//            for (List<View> viewSet : listSet) {
//                viewSet.add(view);
//            }
//        }
//    }

    // add view to mContentView
    protected final <T extends View> void addViewAboveAnchor(
            @NonNull T view,
            @NonNull ViewGroup.LayoutParams params,
            View anchorView) {
        ViewGroup rootView = (ViewGroup) mContentView;
        int pos = anchorView != null ? rootView.indexOfChild(anchorView) : -1;
        if (pos >= 0) {
            rootView.addView(view, pos + 1, params);
        } else {
            rootView.addView(view, params);
        }
    }

    // add view to mContentView
    protected final <T extends View> void addViewUnderAnchor(
            @NonNull T view,
            @NonNull ViewGroup.LayoutParams params,
            View anchorView) {
        ViewGroup rootView = (ViewGroup) mContentView;
        int pos = anchorView != null ? rootView.indexOfChild(anchorView) : -1;
        if (pos >= 0) {
            rootView.addView(view, pos, params);
        } else {
            rootView.addView(view, 0, params);
        }
    }

    protected final void registerAction(int event) {
        mController.registerObserverForEvent(event, this);
    }

    protected final void unregisterAction(int event) {
        mController.unregisterObserverForEvent(event, this);
    }

    public BaseSdkView(
            @NonNull Activity activity,
            @NonNull ViewGroup parentView,
            @NonNull CONTROLLER controller) {
        super(parentView, controller);
        mActivity = activity;
    }

    @Override
    public void startView() {
        super.startView();
        MyLog.w(TAG, "startView");
    }

    @Override
    @CallSuper
    public void stopView() {
        super.stopView();
        MyLog.w(TAG, "stopView");
        mController.unregisterObserver(this);
    }

    @Override
    public void release() {
        super.release();
        MyLog.w(TAG, "release");
    }

    public abstract class AnimationHelper {

        protected final boolean startRefAnimator(WeakReference<? extends Animator> reference) {
            Animator animator = deRef(reference);
            if (animator != null) {
                if (!animator.isStarted() && !animator.isRunning()) {
                    animator.start();
                }
                return true;
            }
            return false;
        }

        protected final void stopRefAnimator(WeakReference<? extends Animator> reference) {
            Animator animator = deRef(reference);
            if (animator != null) {
                animator.cancel();
            }
        }

        protected final void stopRefAnimation(WeakReference<? extends Animation> reference) {
            Animation animation = deRef(reference);
            if (animation != null) {
                animation.cancel();
                animation.reset();
            }
        }

        protected final ValueAnimator startNewAnimator(
                ValueAnimator.AnimatorUpdateListener updateListener,
                Animator.AnimatorListener listener) {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            valueAnimator.setDuration(300);
            valueAnimator.addUpdateListener(updateListener);
            valueAnimator.addListener(listener);
            valueAnimator.start();
            return valueAnimator;
        }

        /**
         * 停止动画
         */
        protected void stopAllAnimator() {
        }

        /**
         * 停止动画，并释放动画资源引用
         */
        public void clearAnimation() {
        }
    }
}
