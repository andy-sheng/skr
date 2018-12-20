package com.module.home.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.AnimationSet;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.RouterConstants;
import com.module.home.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

public class GameFragment extends BaseFragment {


    @Override
    public int initView() {
        return R.layout.game_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        ExImageView ivAthleticsPk = (ExImageView) mRootView.findViewById(R.id.iv_athletics_pk);
        ExImageView ivNormalPk = (ExImageView) mRootView.findViewById(R.id.iv_normal_pk);
        ExImageView mIvYulePk = (ExImageView) mRootView.findViewById(R.id.iv_yule_pk);


        RxView.clicks(ivAthleticsPk)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        clickAnimation(ivAthleticsPk);
                    }
                });

        RxView.clicks(ivNormalPk)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        clickAnimation(ivNormalPk);
                    }
                });

        RxView.clicks(mIvYulePk)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        clickAnimation(mIvYulePk);
                    }
                });

    }

    // 点击缩放动画
    public void clickAnimation(View view) {
        ObjectAnimator a1 = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f);
        ObjectAnimator a2 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f);
        ObjectAnimator a3 = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f);
        ObjectAnimator a4 = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(80);
        set.play(a1).with(a2);
        set.play(a3).with(a4).after(a1);
        set.start();

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (view.getId() == R.id.iv_athletics_pk) {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_RANKINGMODE)
                            .withBoolean("selectSong", true)
                            .greenChannel().navigation();
                } else if (view.getId() == R.id.iv_normal_pk) {
                    //TODO  test
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_RANKING_ROOM)
                            .greenChannel().navigation();
                } else if (view.getId() == R.id.iv_yule_pk) {

                }
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
