package com.wali.live.livesdk.live.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.base.global.GlobalData;
import com.mi.live.engine.base.GalileoConstants;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.livesdk.live.liveshow.data.MagicParamPresenter;
import com.wali.live.livesdk.live.liveshow.presenter.adapter.SingleChooser;

/**
 * Created by zyh on 2017/3/7.
 */

public class BeautyView extends RelativeLayout {

    private ImageView mBeautyIv;
    private ViewGroup mMultiBeautyContainer;

    private boolean mShowBeautyContainer = false;
    private BeautyCallBack mBeautyCallBack;
    private MagicParamPresenter.MagicParams mMagicParams;
    private StreamerPresenter mStreamerPresenter;

    private final SingleChooser mSingleChooser = new SingleChooser(
            new SingleChooser.IChooserListener() {
                @Override
                public void onItemSelected(View view) {
                    int i = view.getId();
                    int level;
                    if (i == R.id.high_tv) {
                        level = 3;
                    } else if (i == R.id.middle_tv) {
                        level = 2;
                    } else if (i == R.id.low_tv) {
                        level = 1;
                    } else {
                        level = 0;
                    }
                    updateBeautyIv(mMagicParams.getBeautyLevel(level));
                    showBeautyLevelContainer(false);
                }
            });

    public void setBeautyCallBack(BeautyCallBack beautyCallBack) {
        mBeautyCallBack = beautyCallBack;
    }

    public void setStreamerPresenter(StreamerPresenter presenter) {
        mStreamerPresenter = presenter;
        if (mStreamerPresenter != null) {
            beautyIndexSelectInMulti(mStreamerPresenter.getBeautyLevel());
        }
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
        initData();
        initView();
    }

    private void initData() {
        mMagicParams = new MagicParamPresenter.MagicParams();
        mMagicParams.loadParams(GlobalData.app());
    }

    private void initView() {
        mBeautyIv = (ImageView) findViewById(R.id.beauty_btn);
        mBeautyIv.setSelected(true);
        mBeautyIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showBeautyLevelContainer(!mShowBeautyContainer);
            }
        });

        if (!mMagicParams.isBeauty()) {
            mBeautyIv.setVisibility(View.INVISIBLE);
        } else if (mMagicParams.isMultiBeauty()) {
            mMultiBeautyContainer = (ViewGroup) findViewById(R.id.beauty_level_container);
        }
    }

    private void beautyIndexSelectInMulti(int beautyLevel) {
        if (mMultiBeautyContainer != null) {
            int selectId;
            if (mMagicParams.getBeautyLevel(3) == beautyLevel) {
                selectId = R.id.high_tv;
            } else if (mMagicParams.getBeautyLevel(2) == beautyLevel) {
                selectId = R.id.middle_tv;
            } else if (mMagicParams.getBeautyLevel(1) == beautyLevel) {
                selectId = R.id.low_tv;
            } else {
                selectId = R.id.close_tv;
            }
            mSingleChooser.setup(mMultiBeautyContainer, selectId);
        }
    }

    private void showBeautyLevelContainer(boolean showBeautyContainer) {
        if (mMultiBeautyContainer != null) { //多级美颜
            mShowBeautyContainer = showBeautyContainer;
            if (showBeautyContainer) {
                if (mMultiBeautyContainer.getVisibility() != View.VISIBLE) {
                    mMultiBeautyContainer.setVisibility(View.VISIBLE);
                }
                showBeautyAnim();
            } else {
                hideBeautyAnim();
            }
        } else { //单级美颜
            mBeautyIv.setSelected(!mBeautyIv.isSelected());
            updateBeautyIv(mBeautyIv.isSelected() ? mMagicParams.getBeautyLevel(1) : mMagicParams.getBeautyLevel(0));
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
        mBeautyIv.setSelected(beautyLevel != GalileoConstants.BEAUTY_LEVEL_OFF);
        if (mStreamerPresenter != null) {
            mStreamerPresenter.setBeautyLevel(beautyLevel);
        }
    }

    public interface BeautyCallBack {
        void showMultiBeautyAnim();

        void hideMultiBeautyAnim();
    }
}
