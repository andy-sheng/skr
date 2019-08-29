package com.common.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;

public abstract class AnimateClickListener extends DebounceViewClickListener {
    public AnimateClickListener() {
    }

    public AnimateClickListener(int debounceTime) {
        super(debounceTime);
    }

    @Override
    public void clickValid(View view) {
        PropertyValuesHolder pvhSX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0.95f, 1f);
        PropertyValuesHolder pvhSY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0.95f, 1f);

        ObjectAnimator o1 = ObjectAnimator.ofPropertyValuesHolder(view, pvhSX, pvhSY);
        o1.setDuration(80);
        o1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                click(view);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                onAnimationEnd(animator);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        o1.start();
    }

    public abstract void click(View view);
}
