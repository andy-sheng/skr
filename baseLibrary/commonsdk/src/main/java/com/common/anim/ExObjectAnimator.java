package com.common.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;

public class ExObjectAnimator {

    ObjectAnimator mObjectAnimator;
    Listener mListener;

    public ExObjectAnimator(ObjectAnimator objectAnimator) {
        mObjectAnimator = objectAnimator;
        mObjectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                if (mListener != null) {
                    mListener.onAnimationEnd(animation);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mListener != null) {
                    mListener.onAnimationEnd(animation);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (mListener != null) {
                    mListener.onAnimationStart(animation);
                }
            }

            @Override
            public void onAnimationPause(Animator animation) {
                super.onAnimationPause(animation);

            }

            @Override
            public void onAnimationResume(Animator animation) {
                super.onAnimationResume(animation);
            }
        });
    }

    public static ExObjectAnimator ofFloat(Object target, String propertyName, float... values) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(target, propertyName, values);
        ExObjectAnimator exObjectAnimator = new ExObjectAnimator(oa);
        return exObjectAnimator;
    }

    public void setDuration(int i) {
        mObjectAnimator.setDuration(i);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void start() {
        mObjectAnimator.start();
    }

    public interface Listener{
        void onAnimationStart(Animator animator);
        void onAnimationEnd(Animator animator);
    }
}
