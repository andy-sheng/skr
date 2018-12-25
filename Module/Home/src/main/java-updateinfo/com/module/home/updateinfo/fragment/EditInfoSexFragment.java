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
import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.titlebar.CommonTitleBar;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.home.R;
import com.zq.live.proto.Common.ESex;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

//编辑性别
public class EditInfoSexFragment extends BaseFragment {

    CommonTitleBar mTitlebar;
    ExImageView mMale;
    ExImageView mMaleTaoxin;
    ExImageView mFemale;
    ExImageView mFemaleTaoxin;

    int sex = 0;// 未知、非法参数

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

        RxView.clicks(mTitlebar.getRightTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        clickComplete();
                    }
                });

        RxView.clicks(mMale)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        selectSex(true);
                    }
                });

        RxView.clicks(mFemale)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        selectSex(false);
                    }
                });
    }

    private void clickComplete() {
        if (this.sex == 0) {
            U.getFragmentUtils().popFragment(EditInfoSexFragment.this);
            return;
        } else {
            MyUserInfoManager.getInstance().updateInfo(null, sex, null, null, null, null);
            U.getFragmentUtils().popFragment(EditInfoSexFragment.this);
        }
    }


    // 选一个，另一个需要缩放动画
    private void selectSex(boolean isMale) {
        this.sex = isMale ? ESex.SX_MALE.getValue() : ESex.SX_FEMALE.getValue();
        boolean needAnimation = false; //另一个性别是否需要缩放动画
        if (mMaleTaoxin.getVisibility() == View.VISIBLE || mFemaleTaoxin.getVisibility() == View.VISIBLE) {
            // 当前已有被选中的，需要一个缩放动画
            needAnimation = true;
        }

        // 放大动画
        ObjectAnimator a1 = ObjectAnimator.ofFloat(isMale ? mMale : mFemale, "scaleX", 1f, 1.2f);
        ObjectAnimator a2 = ObjectAnimator.ofFloat(isMale ? mMale : mFemale, "scaleY", 1f, 1.2f);
        ObjectAnimator a3 = ObjectAnimator.ofFloat(isMale ? mMaleTaoxin : mFemaleTaoxin, "scaleX", 0f, 1f);
        ObjectAnimator a4 = ObjectAnimator.ofFloat(isMale ? mMaleTaoxin : mFemaleTaoxin, "scaleY", 0f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(80);

        if (needAnimation) {
            // 缩小动画
            ObjectAnimator s1 = ObjectAnimator.ofFloat(isMale ? mFemale : mMale, "scaleX", 1.2f, 1f);
            ObjectAnimator s2 = ObjectAnimator.ofFloat(isMale ? mFemale : mMale, "scaleY", 1.2f, 1f);
            ObjectAnimator s3 = ObjectAnimator.ofFloat(isMale ? mFemaleTaoxin : mMaleTaoxin, "scaleX", 1f, 0f);
            ObjectAnimator s4 = ObjectAnimator.ofFloat(isMale ? mFemaleTaoxin : mMaleTaoxin, "scaleY", 1f, 0f);
            set.playTogether(a1, a2, a3, a4, s1, s2, s3, s4);
        } else {
            set.playTogether(a1, a2, a3, a4);
        }

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mMaleTaoxin.setVisibility(isMale ? View.VISIBLE : View.GONE);
                mFemaleTaoxin.setVisibility(isMale ? View.GONE : View.VISIBLE);

                mMale.setClickable(isMale ? false : true);
                mMaleTaoxin.setClickable(isMale ? true : false);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                onAnimationEnd(animator);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        set.start();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
