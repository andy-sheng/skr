package com.wali.live.componentwrapper;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.support.annotation.CallSuper;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.thornbirds.component.ComponentView;
import com.thornbirds.component.IEventObserver;

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
    }

    @Override
    @CallSuper
    public void stopView() {
        super.stopView();
        mController.unregisterObserver(this);
    }

    @Override
    public void release() {
        super.release();
    }

    public abstract class AnimationHelper {

        protected final void setAlpha(View view, @FloatRange(from = 0.0f, to = 1.0f) float alpha) {
            if (view != null) {
                view.setAlpha(alpha);
            }
        }

        protected final void setVisibility(View view, int visibility) {
            if (view != null) {
                view.setVisibility(visibility);
            }
        }

        protected WeakReference<ValueAnimator> mInputAnimatorRef; // 输入框弹起时，隐藏
        protected boolean mInputShow = false;

        /**
         * 输入框显示时，隐藏弹幕区和头部区
         * 弹幕区只在横屏下才需要显示和隐藏，直接修改visibility，在显示动画开始时显示，在消失动画结束时消失。
         */
        protected abstract void startInputAnimator(boolean inputShow);

        /**
         * 停止动画
         */
        protected abstract void stopAllAnimator();

        /**
         * 停止动画，并释放动画资源引用
         */
        public abstract void clearAnimation();
    }
}
