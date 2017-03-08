package com.wali.live.livesdk.live.view;

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

    private int[] mMultiResId = {
            R.id.high_tv, R.id.middle_tv, R.id.low_tv, R.id.close_tv
    };

    private int[] mMultiIndex = {
            GalileoConstants.BEAUTY_LEVEL_HIGHEST, GalileoConstants.BEAUTY_LEVEL_MIDDLE,
            GalileoConstants.BEAUTY_LEVEL_LOW, GalileoConstants.BEAUTY_LEVEL_OFF
    };

    private int mBeautySupportCode = FACE_BEAUTY_DEFAULT;
    private int mBeautyLevel = GalileoConstants.BEAUTY_LEVEL_HIGHEST;
    private ImageView mBeautyIv;
    private View mMultiBeautyContainer;

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
        mBeautyIv.setOnClickListener(this);
        mMultiBeautyContainer = findViewById(R.id.beauty_level_container);

        if (mMultiBeautyContainer.getVisibility() == View.VISIBLE) {
            findViewById(R.id.high_tv).setOnClickListener(this);
            findViewById(R.id.middle_tv).setOnClickListener(this);
            findViewById(R.id.low_tv).setOnClickListener(this);
        }
    }

    private void initData() {
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.beauty_btn) {
            showBeautyLevelContainer(!(mMultiBeautyContainer.getVisibility() == View.VISIBLE));
        } else if (i == R.id.high_tv || i == R.id.middle_tv || i == R.id.low_tv || i == R.id.close_tv) {
//            updateBeautyIv();
        }
    }

    private void showBeautyLevelContainer(boolean showBeautyContainer) {
        switch (mBeautySupportCode) {
            case FACE_BEAUTY_MULTI_LEVEL:
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
                updateBeautyIv(showBeautyContainer ? mBeautyLevel : GalileoConstants.BEAUTY_LEVEL_OFF);
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
        mBeautyIv.setSelected(beautyLevel != GalileoConstants.BEAUTY_LEVEL_OFF);
//        TODO 通知 Livesdk的引擎更新美颜级别
    }
}
