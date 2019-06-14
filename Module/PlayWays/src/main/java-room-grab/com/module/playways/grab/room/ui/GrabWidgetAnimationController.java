package com.module.playways.grab.room.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;

import com.common.utils.U;


/**
 * 来控制执行视频模式下面板的一些复杂动画
 */
public class GrabWidgetAnimationController {

    GrabRoomFragment mF;

    public GrabWidgetAnimationController(GrabRoomFragment grabRoomFragment) {
        mF = grabRoomFragment;
    }

    /**
     * 使得主区域下移到 view 的下方
     *
     */
    public void openBelowLyricView() {
        int t = U.getDisplayUtils().dip2px(120);
        ObjectAnimator animator = ObjectAnimator.ofFloat(mF.mGrabTopContentView, View.TRANSLATION_Y, mF.mGrabTopContentView.getTranslationY(), t);
        animator.setDuration(300);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mF.mGrabTopContentView.setArrowIcon(false);
            }
        });
    }

    public void fold() {

    }

    public void unfold() {

    }

    public void destroy() {

    }
}
