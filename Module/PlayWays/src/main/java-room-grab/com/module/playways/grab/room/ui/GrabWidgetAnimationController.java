package com.module.playways.grab.room.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

import com.common.utils.U;

import java.util.ArrayList;
import java.util.List;


/**
 * 来控制执行视频模式下面板的一些复杂动画
 */
public class GrabWidgetAnimationController {

    static final int OPEN_TYPE_FOR_NORMAL = 1;

    static final int OPEN_TYPE_FOR_LYRIC = 2;

    GrabRoomFragment mF;

    int mOpenType = OPEN_TYPE_FOR_NORMAL;

    AnimatorSet mMainAnimatorSet;

    public GrabWidgetAnimationController(GrabRoomFragment grabRoomFragment) {
        mF = grabRoomFragment;
    }

    private int getTranslateByOpenType(){
        if(mOpenType == OPEN_TYPE_FOR_NORMAL){
            return U.getDisplayUtils().dip2px(32);
        }else if(mOpenType == OPEN_TYPE_FOR_LYRIC){
            return U.getDisplayUtils().dip2px(120);
        }
        return 0;
    }
    /**
     * 使得主区域下移到 view 的下方
     */
    public void openBelowLyricView() {
        mOpenType = OPEN_TYPE_FOR_LYRIC;
        open();
    }

    public void open() {
        if (mMainAnimatorSet != null && mMainAnimatorSet.isRunning()) {
            mMainAnimatorSet.cancel();
        }
        // 需要改变偏移的对象
        List<View> viewList = new ArrayList<>();
        viewList.add(mF.mGrabTopContentView);
        viewList.add(mF.mPracticeFlagIv);

        List<Animator> animators1 = new ArrayList<>();
        for (View view : viewList) {
            if (view != null) {
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.getTranslationY(), getTranslateByOpenType());
                animators1.add(objectAnimator);
            }
        }
        mMainAnimatorSet = new AnimatorSet();
        mMainAnimatorSet.playTogether(animators1);
        mMainAnimatorSet.setDuration(300);
        mMainAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
//                if(mOpenType == OPEN_TYPE_FOR_NORMAL){
//                    mF.mGrabTopOpView.setVisibility(View.VISIBLE);
//                    mF.mGrabVideoSelfSingCardView.setVisibility(View.GONE);
//                }else if(mOpenType == OPEN_TYPE_FOR_LYRIC){
//                    mF.mGrabTopOpView.setVisibility(View.GONE);
//                    mF.mGrabVideoSelfSingCardView.setVisibility(View.VISIBLE);
//                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mF.mGrabTopContentView.setArrowIcon(true);
            }
        });
        mMainAnimatorSet.start();
    }

    /**
     * 使得主区域下移到 view 的下方
     */
    public void close() {
        if (mMainAnimatorSet != null && mMainAnimatorSet.isRunning()) {
            mMainAnimatorSet.cancel();
        }
        // 需要改变偏移的对象
        List<View> viewList = new ArrayList<>();
        viewList.add(mF.mGrabTopContentView);
        viewList.add(mF.mPracticeFlagIv);

        List<Animator> animators1 = new ArrayList<>();
        for (View view : viewList) {
            if (view != null) {
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.getTranslationY(), 0);
                animators1.add(objectAnimator);
            }
        }
        mMainAnimatorSet = new AnimatorSet();
        mMainAnimatorSet.playTogether(animators1);
        mMainAnimatorSet.setDuration(300);
        mMainAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mF.mGrabTopContentView.setArrowIcon(false);
            }
        });
        mMainAnimatorSet.start();
    }


    public void destroy() {
        if (mMainAnimatorSet != null) {
            mMainAnimatorSet.cancel();
        }
    }


}
