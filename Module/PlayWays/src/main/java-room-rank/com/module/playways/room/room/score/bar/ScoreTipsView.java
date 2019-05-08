package com.module.playways.room.room.score.bar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.module.playways.R;

import java.util.ArrayList;
import java.util.List;

public class ScoreTipsView extends RelativeLayout {

    ImageView mLevelIv;
    ImageView mJiahaoIv;
    ImageView mNumIv;
    AnimatorSet mAnimatorSet;

    public ScoreTipsView(Context context) {
        super(context);
        init();
    }

    public ScoreTipsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScoreTipsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.score_tips_view_layout, this);

        mLevelIv = (ImageView) this.findViewById(R.id.level_iv);
        mJiahaoIv = (ImageView) this.findViewById(R.id.jihao_iv);
        mNumIv = (ImageView) this.findViewById(R.id.num_iv);
    }

    private void bindData(Item item) {
        mLevelIv.setImageResource(item.level.mDrawableId);
        mNumIv.setImageResource(getNum(item.num));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            mAnimatorSet.removeAllListeners();
            mAnimatorSet.cancel();
        }
    }

    private int getNum(int num) {
        switch (num) {
            case 1:
                return R.drawable.yanchangjiemian_1;
            case 2:
                return R.drawable.yanchangjiemian_2;
            case 3:
                return R.drawable.yanchangjiemian_3;
            case 4:
                return R.drawable.yanchangjiemian_4;
            case 5:
                return R.drawable.yanchangjiemian_5;
            case 6:
                return R.drawable.yanchangjiemian_6;
            case 7:
                return R.drawable.yanchangjiemian_7;
            case 8:
                return R.drawable.yanchangjiemian_8;
            case 9:
                return R.drawable.yanchangjiemian_9;
            default:
                return R.drawable.yanchangjiemian_9;
        }
    }

    private void startPlay() {
        List<Animator> animatorSetList = new ArrayList<>();
        {
            int scaleB = 3;
            int scaleE = 1;
            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(this, View.SCALE_X, scaleB, scaleE);
            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(this, View.SCALE_Y, scaleB, scaleE);
            ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(this, View.ALPHA, 0.1f, 1);
            ObjectAnimator objectAnimator4 = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, U.getDisplayUtils().dip2px(7), 0);
            AnimatorSet animatorSet1 = new AnimatorSet();
            animatorSet1.playTogether(objectAnimator1, objectAnimator2, objectAnimator3, objectAnimator4);
            animatorSet1.setDuration(300);
            animatorSet1.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorSetList.add(animatorSet1);
        }

//        {
//            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(this, View.SCALE_X, 1, 1f);
//            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1, 1f);
//            ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 1f);
//            ObjectAnimator objectAnimator4 = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 0, 0);
//            AnimatorSet animatorSet1 = new AnimatorSet();
//            animatorSet1.playTogether(objectAnimator1, objectAnimator2, objectAnimator3, objectAnimator4);
//            animatorSet1.setDuration(2000);
//            animatorSetList.add(animatorSet1);
//        }


        {
            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(this, View.SCALE_X, 1, 0.8f);
            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1, 0.8f);
            ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0.7f);
            ObjectAnimator objectAnimator4 = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 0, -U.getDisplayUtils().dip2px(7));
            AnimatorSet animatorSet1 = new AnimatorSet();
            animatorSet1.playTogether(objectAnimator1, objectAnimator2, objectAnimator3, objectAnimator4);
            animatorSet1.setInterpolator(new LinearInterpolator());
            animatorSet1.setDuration(800);
            animatorSetList.add(animatorSet1);
        }

        {
            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(this, View.SCALE_X, 0.8f, 0.2f);
            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(this, View.SCALE_Y, 0.8f, 0.2f);
            ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(this, View.ALPHA, 0.7f, 0.1f);
            ObjectAnimator objectAnimator4 = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, -U.getDisplayUtils().dip2px(7), -U.getDisplayUtils().dip2px(33));
            AnimatorSet animatorSet1 = new AnimatorSet();
            animatorSet1.playTogether(objectAnimator1, objectAnimator2, objectAnimator3, objectAnimator4);
            animatorSet1.setInterpolator(new AccelerateInterpolator());
            animatorSet1.setDuration(300);
            animatorSetList.add(animatorSet1);
        }

        if (mAnimatorSet != null) {
            mAnimatorSet.removeAllListeners();
            mAnimatorSet.cancel();
        }
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playSequentially(animatorSetList);
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (ScoreTipsView.this.getParent() != null) {
                    ViewGroup vg = (ViewGroup) ScoreTipsView.this.getParent();
                    vg.removeView(ScoreTipsView.this);
                }
            }
        });
        mAnimatorSet.start();
    }

    public static void play(RelativeLayout parent, Item item) {
        play(parent, item, 1);
    }

    public static void play(RelativeLayout parent, Item item, int postion) {
        if (item == null) {
            return;
        }
        if (item.level == Level.Bad && item.num > 1) {
            // 丢人不能连续
            return;
        }
        ScoreTipsView scoreTipsView = new ScoreTipsView(parent.getContext());
        scoreTipsView.bindData(item);
        RelativeLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (postion == 1) {
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        } else if (postion == 2) {
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lp.rightMargin = U.getDisplayUtils().dip2px(20);
        }
        lp.topMargin = U.getDisplayUtils().dip2px(135);
        parent.addView(scoreTipsView, lp);
        scoreTipsView.startPlay();
    }

    public enum Level {
        Perfect(R.drawable.level_chaowanmei),
        Good(R.drawable.level_bucuoo),
        Ok(R.drawable.level_haikeyi),
        Bad(R.drawable.level_renzhendian),

        Grab_renzhen(R.drawable.level_renzhendian),
        Grab_jiayou(R.drawable.level_jiayouo),
        Grab_bucuo(R.drawable.level_bucuoo),
        Grab_wanmei(R.drawable.level_chaowanmei),
        Grab_keyi(R.drawable.level_haikeyi),
        Grab_taibang(R.drawable.level_taibangle);

        int mDrawableId;

        Level(int id) {
            mDrawableId = id;
        }
    }


    public static class Item {
        Level level;

        int num = 1;

        public Level getLevel() {
            return level;
        }

        public void setLevel(Level level) {
            this.level = level;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public int getNum() {
            return num;
        }
    }
}
