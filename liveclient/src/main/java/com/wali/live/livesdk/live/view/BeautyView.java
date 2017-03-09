package com.wali.live.livesdk.live.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mi.live.engine.base.GalileoConstants;
import com.wali.live.livesdk.R;

/**
 * Created by zyh on 2017/3/7.
 */

public class BeautyView extends RelativeLayout implements View.OnClickListener {
    public static final int FACE_BEAUTY_DISABLED = 0;
    public static final int FACE_BEAUTY_MULTI_LEVEL = 1;
    public static final int FACE_BEAUTY_DEFAULT = 2;

    private int mBeautySupportCode = FACE_BEAUTY_MULTI_LEVEL;
    private int mBeautyLevel = GalileoConstants.BEAUTY_LEVEL_HIGHEST;
    private ImageView mBeautyIv;
    private View mMultiBeautyContainer;
    private boolean mShowBeautyContainer = false;

    private BeautyCallBack mBeautyCallBack;
    private View mPreSelectView;

    public void setBeautyCallBack(BeautyCallBack beautyCallBack) {
        mBeautyCallBack = beautyCallBack;
    }

    public BeautyView(Context context) {
        super(context);
        init(context);
    }

    public BeautyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BeautyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.beauty_view, this);
        initView();
    }

    private void initView() {
        mBeautyIv = (ImageView) findViewById(R.id.beauty_btn);
        mBeautyIv.setSelected(true);
        mBeautyIv.setOnClickListener(this);
        mMultiBeautyContainer = findViewById(R.id.beauty_level_container);

        findViewById(R.id.high_tv).setOnClickListener(this);
        findViewById(R.id.middle_tv).setOnClickListener(this);
        findViewById(R.id.low_tv).setOnClickListener(this);
        findViewById(R.id.close_tv).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.beauty_btn) {
            showBeautyLevelContainer(!mShowBeautyContainer);
        } else {
            int level = GalileoConstants.BEAUTY_LEVEL_HIGHEST;
            if (mPreSelectView != null) {
                if (mPreSelectView.equals(v)) {
                    return;
                }
                mPreSelectView.setSelected(false);
            }
            mPreSelectView = v;
            mPreSelectView.setSelected(true);
            if (i == R.id.high_tv) {
                level = GalileoConstants.BEAUTY_LEVEL_HIGHEST;
            } else if (i == R.id.middle_tv) {
                level = GalileoConstants.BEAUTY_LEVEL_MIDDLE;
            } else if (i == R.id.low_tv) {
                level = GalileoConstants.BEAUTY_LEVEL_LOW;
            } else if (i == R.id.close_tv) {
                level = GalileoConstants.BEAUTY_LEVEL_OFF;
            } else {
                //默认level值
            }
            if (level != mBeautyLevel) {
                updateBeautyIv(level);
            }
            showBeautyLevelContainer(false);
        }
    }

    private void showBeautyLevelContainer(boolean showBeautyContainer) {
        switch (mBeautySupportCode) {
            case FACE_BEAUTY_MULTI_LEVEL:
                mShowBeautyContainer = showBeautyContainer;
                if (showBeautyContainer) {
                    if (mMultiBeautyContainer.getVisibility() != View.VISIBLE) {
                        mMultiBeautyContainer.setVisibility(View.VISIBLE);
                    }
                    showBeautyAnim();
                } else {
                    hideBeautyAnim();
                }
                break;
            case FACE_BEAUTY_DEFAULT:
                mBeautyIv.setSelected(!mBeautyIv.isSelected());
                updateBeautyIv(mBeautyIv.isSelected() ? mBeautyLevel : GalileoConstants.BEAUTY_LEVEL_OFF);
                break;
            default:
                break;
        }
    }

    private ValueAnimator mShowAnimation;
    private ValueAnimator mHideAnimation;

    private void showBeautyAnim() {
        if (mHideAnimation != null && mHideAnimation.isRunning()) {
            mHideAnimation.cancel();
        }
        if (mShowAnimation == null) {
            mShowAnimation = ValueAnimator.ofFloat(0.0f, 1.0f);
            mShowAnimation.setDuration(300);
            mShowAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            mShowAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mBeautyCallBack != null) {
                        mBeautyCallBack.showMultiBeautyAnim();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mShowAnimation.addUpdateListener(
                    new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            float value = (float) animation.getAnimatedValue();
                            mMultiBeautyContainer.setAlpha(value);
                        }
                    });
        }
        if (!mShowAnimation.isRunning()) {
            mShowAnimation.start();
        }
        mMultiBeautyContainer.setEnabled(true);
    }

    private void hideBeautyAnim() {
        if (mShowAnimation != null && mShowAnimation.isRunning()) {
            mShowAnimation.cancel();
        }
        if (mHideAnimation == null) {
            mHideAnimation = ValueAnimator.ofFloat(1.0f, 0.0f);
            mHideAnimation.setDuration(300);
            mHideAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            mHideAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mBeautyCallBack != null) {
                        mBeautyCallBack.hideMultiBeautyAnim();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mHideAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    mMultiBeautyContainer.setAlpha(value);
                }
            });
        }
        if (!mHideAnimation.isRunning()) {
            mHideAnimation.start();
        }
        mMultiBeautyContainer.setEnabled(false);
    }

    private void updateBeautyIv(int beautyLevel) {
        mBeautyLevel = beautyLevel;
        mBeautyIv.setSelected(beautyLevel != GalileoConstants.BEAUTY_LEVEL_OFF);
//        TODO 通知 Livesdk的引擎更新美颜级别
    }

    public interface BeautyCallBack {
        void showMultiBeautyAnim();

        void hideMultiBeautyAnim();
    }
}
