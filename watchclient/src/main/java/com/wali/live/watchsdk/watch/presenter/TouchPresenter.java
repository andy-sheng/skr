package com.wali.live.watchsdk.watch.presenter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.util.Property;
import android.view.View;

import com.base.activity.assist.IBindActivityLIfeCycle;
import com.base.log.MyLog;
import com.wali.live.event.SdkEventClass;
import com.wali.live.watchsdk.watch.view.TouchDelegateView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by chengsimin on 2016/12/16.
 */

public class TouchPresenter implements IBindActivityLIfeCycle {
    public final static String TAG = TouchPresenter.class.getSimpleName();

    public static final int ANIMATION_SLIDE = 0;
    public static final int ANIMATION_TAP_DISMISS = 1;

    public static final int DIRECTION_UP = 0;
    public static final int DIRECTION_DOWN = 1;
    public static final int DIRECTION_LEFT = 2;
    public static final int DIRECTION_RIGHT = 3;

    TouchDelegateView mTouchDelegateView;
    AnimationParams  mAnimationParamsPortrait;
    AnimationParams  mAnimationParamsLandscape;

    private boolean mHideAll = false;
    GestureApater mGestureApater;

    boolean mLandscape = false;

    public TouchPresenter(TouchDelegateView touchDelegateView) {
        this.mTouchDelegateView = touchDelegateView;
        mTouchDelegateView.setGestureListener(new TouchDelegateView.GestureListener() {
            @Override
            public void onLeftFlingFilterX() {
                if (getAnimationParamsMode().animationWays == ANIMATION_SLIDE) {
                    showAllView();
                }
                if (mGestureApater != null) {
                    mGestureApater.onLeftFlingFilterX();
                }
            }

            @Override
            public void onRightFlingFilterX() {
                if (getAnimationParamsMode().animationWays == ANIMATION_SLIDE) {
                    hideAllView();
                }
                if (mGestureApater != null) {
                    mGestureApater.onRightFlingFilterX();
                }
            }

            @Override
            public void onSingleTap() {
                if (mGestureApater != null) {
                    mGestureApater.onSingleTap();
                }

                if(!hasConsume && getAnimationParamsMode().animationWays == ANIMATION_TAP_DISMISS){
                    if(mHideAll){
                        showAllViewByTap();
                    }else{
                        hideAllViewByTap();
                    }
                }
            }
            boolean hasConsume = false;
            @Override
            public boolean onDown() {
                if (mGestureApater != null) {
                    hasConsume = mGestureApater.onDown();
                }
                return hasConsume;
            }

            @Override
            public void onMoveFilterX(float tx) {
                if (getAnimationParamsMode().animationWays == ANIMATION_SLIDE) {
                    moveAll(tx);
                }
                if (mGestureApater != null) {
                    mGestureApater.onMoveFilterX(tx);
                }
            }

            @Override
            public void onUp() {
                if (getAnimationParamsMode().animationWays == ANIMATION_SLIDE) {
                    resetAllView();
                }
                if (mGestureApater != null) {
                    mGestureApater.onUp();
                }
            }

            @Override
            public void onCancel() {
                if (mGestureApater != null) {
                    mGestureApater.onCancel();
                }
            }
        });
    }

    AnimationParams getAnimationParamsMode() {
        if (mLandscape) {
            return mAnimationParamsLandscape;
        } else {
            return mAnimationParamsPortrait;
        }
    }

    private void moveAll(float tx) {
        // 除了指定的某些view全部隐藏 , 但是如果他的父类隐藏了，它也显示不了
        // 后面再递归显示的它的父类,这个方案不行。它的子类没有机会再显示了。
        int i = 0;
        for (View v : getAnimationParamsMode().views) {
            if (v != null) {
                float newTx = v.getTranslationX() + tx;
                if (newTx > 0) {
                    v.setTranslationX(newTx);
                } else {
                    break;
                }
            }
            i++;
        }
    }

    protected void resetAllView() {
        // 除了指定的某些view全部隐藏 , 但是如果他的父类隐藏了，它也显示不了
        // 后面再递归显示的它的父类,这个方案不行。它的子类没有机会再显示了。
        MyLog.d("TouchEvent", "resetAll");
        for (View v : getAnimationParamsMode().views) {
            if (v != null) {
                v.setTranslationX(0);
            }
        }
    }

    private void hideAllView() {
        // 除了指定的某些view全部隐藏 , 但是如果他的父类隐藏了，它也显示不了
        // 后面再递归显示的它的父类,这个方案不行。它的子类没有机会再显示了。
        for (View v : getAnimationParamsMode().views) {
            if (v != null) {
                v.setVisibility(View.GONE);
            }
        }
        mHideAll = true;
    }

    private void showAllView() {
        for (View v : getAnimationParamsMode().views) {
            if (v != null) {
                v.setVisibility(View.VISIBLE);
            }
        }
        mHideAll = false;
    }

    private void hideAllViewByTap() {
        AnimationParams animationParams = getAnimationParamsMode();
        for(int i=0;i<animationParams.views.length;i++){
            final View view = animationParams.views[i];
            if(view==null){
                continue;
            }
            int direction = animationParams.ext[i];
            int begin = 0,end = 0;
            Property<View, Float> property = null;
            switch (direction){
                case DIRECTION_UP:{
                    property =  View.TRANSLATION_Y;
                    end = -view.getHeight();
                    break;
                }
                case DIRECTION_DOWN:{
                    property =  View.TRANSLATION_Y;
                    end = view.getHeight();
                    break;
                }
                case DIRECTION_LEFT:{
                    property =  View.TRANSLATION_X;
                    end = -view.getWidth();
                    break;
                }
                case DIRECTION_RIGHT:{
                    property =  View.TRANSLATION_X;
                    end = view.getWidth();
                    break;
                }
            }
            if(property!=null) {
                final ObjectAnimator animator = ObjectAnimator.ofFloat(view, property, begin, end);
                animator.setDuration(200);
                final Property<View, Float> finalProperty = property;
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                        if(finalProperty == View.TRANSLATION_X){
                            view.setTranslationX(0);
                        }else{
                            view.setTranslationY(0);
                        }
                    }
                });
                animator.start();
            }
        }
        mHideAll = true;
    }

    private void showAllViewByTap() {
        AnimationParams animationParams = getAnimationParamsMode();
        for(int i=0;i<animationParams.views.length;i++){
            View view = animationParams.views[i];
            if(view==null){
                continue;
            }
            view.setVisibility(View.VISIBLE);
        }

        for(int i=0;i<animationParams.views.length;i++){
            View view = animationParams.views[i];
            if(view==null){
                continue;
            }
            int direction = animationParams.ext[i];
            switch (direction){
                case DIRECTION_UP:{
                    view.animate().translationY(0).setDuration(200).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        }
                    });
                    break;
                }
                case DIRECTION_DOWN:{
                    view.animate().translationY(0).setDuration(200).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        }
                    });
                    break;
                }
                case DIRECTION_LEFT:{
                    view.animate().translationX(0).setDuration(200).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        }
                    });
                    break;
                }
                case DIRECTION_RIGHT:{
                    view.animate().translationX(0).setDuration(200).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        }
                    });
                    break;
                }
            }
        }
        mHideAll = false;
    }

    @Override
    public void onActivityDestroy() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActivityCreate() {
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onEvent(SdkEventClass.OrientEvent event) {
        // 这里改掉
        mLandscape = event.isLandscape();
        if(isHideAll()){
            if(getAnimationParamsMode().animationWays == ANIMATION_TAP_DISMISS){
                showAllViewByTap();
            }else{
                showAllView();
            }
        }
    }

    public static class AnimationParams{
        public int animationWays = ANIMATION_SLIDE;
        public View views[];
        public int ext[];// 标记view是往上下左右哪个方向飘动
    }

    /* 竖屏下 */
    public void setNeedHideViewsPortrait(AnimationParams animationParams) {
        mAnimationParamsPortrait = animationParams;
    }

    /* 横屏下 */
    public void setNeedHideViewsLandscape(AnimationParams animationParams) {
        mAnimationParamsLandscape = animationParams;
    }

    public void setGestureAdapter(GestureApater gestureApater) {
        mGestureApater = gestureApater;
    }

    public boolean isHideAll() {
        return mHideAll;
    }

    public static class GestureApater implements TouchDelegateView.GestureListener {

        @Override
        public void onLeftFlingFilterX() {

        }

        @Override
        public void onRightFlingFilterX() {

        }

        @Override
        public void onSingleTap() {

        }

        @Override
        public boolean onDown() {
            return false;
        }

        @Override
        public void onMoveFilterX(float tx) {

        }

        @Override
        public void onUp() {

        }

        @Override
        public void onCancel() {

        }
    }
}
