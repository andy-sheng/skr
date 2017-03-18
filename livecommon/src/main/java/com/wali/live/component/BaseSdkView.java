package com.wali.live.component;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.FloatRange;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.base.presenter.Presenter;
import com.live.module.common.R;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.component.view.IComponentView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangli on 2017/2/17.
 *
 * @author YangLi
 * @mail yanglijd@gmail.com
 */
public abstract class BaseSdkView<T extends ComponentController> {
    private static final String TAG = "BaseSdkView";

    @NonNull
    protected Activity mActivity;
    @NonNull
    protected T mComponentController;

    protected final List<IComponentView> mComponentViewSet = new ArrayList<>();
    protected final List<ComponentPresenter> mComponentPresenterSet = new ArrayList<>();

    @Nullable
    @CheckResult
    protected final <V extends View> V $(@IdRes int id) {
        return (V) mActivity.findViewById(id);
    }

    @Nullable
    @CheckResult
    protected final <V extends View> V $(@NonNull ViewGroup viewGroup, @IdRes int id) {
        return (V) viewGroup.findViewById(id);
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

    // add view to activity
    protected final <T extends View> void addViewAboveAnchor(
            @NonNull T view,
            @NonNull ViewGroup.LayoutParams params,
            @IdRes int anchorId) {
        ViewGroup rootView = (ViewGroup) mActivity.findViewById(R.id.main_act_container);
        View anchorView = $(anchorId);
        int pos = anchorView != null ? rootView.indexOfChild(anchorView) : -1;
        if (pos >= 0) {
            rootView.addView(view, pos + 1, params);
        } else {
            rootView.addView(view, params);
        }
    }

    // add view to activity
    protected final <T extends View> void addViewUnderAnchor(
            @NonNull T view,
            @NonNull ViewGroup.LayoutParams params,
            View anchorView) {
        ViewGroup rootView = (ViewGroup) mActivity.findViewById(R.id.main_act_container);
        int pos = anchorView != null ? rootView.indexOfChild(anchorView) : -1;
        if (pos >= 0) {
            rootView.addView(view, pos, params);
        } else {
            rootView.addView(view, 0, params);
        }
    }

    protected final void addComponentView(
            @NonNull IComponentView view,
            @NonNull ComponentPresenter presenter) {
        view.setPresenter(presenter);
        presenter.setComponentView(view.getViewProxy());
        mComponentViewSet.add(view);
        mComponentPresenterSet.add(presenter);
    }

    protected final void addComponentView(
            @NonNull ComponentPresenter presenter) {
        mComponentPresenterSet.add(presenter);
    }

    public BaseSdkView(@NonNull Activity activity,
                       @NonNull T componentController) {
        mActivity = activity;
        mComponentController = componentController;
    }

    /**
     * 初始化SdkView
     */
    public abstract void setupSdkView();

    /**
     * 销毁SdkView，并释放资源
     */
    public void releaseSdkView() {
        for (Presenter presenter : mComponentPresenterSet) {
            presenter.destroy();
        }
        mComponentPresenterSet.clear();
        mComponentViewSet.clear();
    }

    public abstract class Action implements ComponentPresenter.IAction {

        protected final <T extends ValueAnimator> T deRef(WeakReference<T> reference) {
            return reference != null ? reference.get() : null;
        }

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

        @CallSuper
        public void registerAction() {
            mComponentController.registerAction(ComponentController.MSG_ON_ORIENT_PORTRAIT, this);
            mComponentController.registerAction(ComponentController.MSG_ON_ORIENT_LANDSCAPE, this);
            mComponentController.registerAction(ComponentController.MSG_INPUT_VIEW_SHOWED, this);
            mComponentController.registerAction(ComponentController.MSG_INPUT_VIEW_HIDDEN, this);
            mComponentController.registerAction(ComponentController.MSG_BACKGROUND_CLICK, this);
        }
    }

}
