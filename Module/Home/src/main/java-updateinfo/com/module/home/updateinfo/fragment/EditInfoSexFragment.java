package com.module.home.updateinfo.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.account.UserAccountManager;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.titlebar.CommonTitleBar;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.home.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

//编辑性别
public class EditInfoSexFragment extends BaseFragment {

    CommonTitleBar mTitlebar;
    ExImageView mMale;
    ExImageView mMaleTaoxin;
    ExImageView mFemale;
    ExImageView mFemaleTaoxin;

    @Override
    public int initView() {
        return R.layout.edit_info_sex_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mMale = (ExImageView) mRootView.findViewById(R.id.male);
        mMaleTaoxin = (ExImageView) mRootView.findViewById(R.id.male_taoxin);
        mFemale = (ExImageView) mRootView.findViewById(R.id.female);
        mFemaleTaoxin = (ExImageView) mRootView.findViewById(R.id.female_taoxin);

        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getFragmentUtils().popFragment(EditInfoSexFragment.this);
                    }
                });

        RxView.clicks(mMale)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        clickMale();
                    }
                });

        RxView.clicks(mFemale)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        clickFeMale();
                    }
                });
    }


    private void clickMale() {
        mMaleTaoxin.setVisibility(View.VISIBLE);
        ObjectAnimator a1 = ObjectAnimator.ofFloat(mMale, "scaleX", 1f, 1.2f);
        ObjectAnimator a2 = ObjectAnimator.ofFloat(mMale, "scaleY", 1f, 1.2f);
        ObjectAnimator a3 = ObjectAnimator.ofFloat(mMaleTaoxin, "scaleX", 0f, 1f);
        ObjectAnimator a4 = ObjectAnimator.ofFloat(mMaleTaoxin, "scaleY", 0f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(80);
        set.playTogether(a1,a2,a3,a4);
        set.start();

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mMaleTaoxin.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                onAnimationEnd(animator);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

    }

    private void clickFeMale() {
        mFemaleTaoxin.setVisibility(View.VISIBLE);
        ObjectAnimator a1 = ObjectAnimator.ofFloat(mFemale, "scaleX", 1f, 1.2f);
        ObjectAnimator a2 = ObjectAnimator.ofFloat(mFemale, "scaleY", 1f, 1.2f);
        ObjectAnimator a3 = ObjectAnimator.ofFloat(mFemaleTaoxin, "scaleX", 0f, 1f);
        ObjectAnimator a4 = ObjectAnimator.ofFloat(mFemaleTaoxin, "scaleY", 0f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(80);
        set.playTogether(a1,a2,a3,a4);
        set.start();

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mFemaleTaoxin.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                onAnimationEnd(animator);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

    }


    @Override
    public boolean useEventBus() {
        return false;
    }
}
