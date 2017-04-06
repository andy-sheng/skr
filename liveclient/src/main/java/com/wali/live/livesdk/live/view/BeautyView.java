package com.wali.live.livesdk.live.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

    private IBeautyCallBack mBeautyCallBack;
    private MagicParamPresenter mMagicParamPresenter;
    private MagicParamPresenter.MagicParams mMagicParams;
    private StreamerPresenter mStreamerPresenter;
    private SingleChooser mSingleChooser;
    private boolean mMultiSelectInit = false;

    public void setBeautyCallBack(IBeautyCallBack beautyCallBack) {
        mBeautyCallBack = beautyCallBack;
    }

    public void setStreamerPresenter(StreamerPresenter presenter) {
        mStreamerPresenter = presenter;
        initMultiSelected();
    }

    private void initMultiSelected() {
        if (mStreamerPresenter != null && mSingleChooser != null && !mMultiSelectInit) {
            mMultiSelectInit = true;
            int index = mMagicParams.findBeautyPos(mStreamerPresenter.getBeautyLevel());
            switch (index) {
                case 3:
                    mSingleChooser.setSelection(R.id.high_tv);
                    break;
                case 2:
                    mSingleChooser.setSelection(R.id.middle_tv);
                    break;
                case 1:
                    mSingleChooser.setSelection(R.id.low_tv);
                    break;
                case 0:
                    mSingleChooser.setSelection(R.id.close_tv);
                    break;
                default:
                    break;
            }
        }
    }

    public BeautyView(Context context) {
        this(context, null);
    }

    public BeautyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BeautyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.beauty_view, this);
        getDataFromServer(context);
    }

    private void getDataFromServer(Context context) {
        mMagicParamPresenter = new MagicParamPresenter(null, context);
        mMagicParamPresenter.syncMagicParams(new IMagicParamsCallBack() {
            @Override
            public void onComplete() {
                initData();
                initView();
            }
        });
    }

    private void initData() {
        mMagicParams = new MagicParamPresenter.MagicParams();
        mMagicParams.loadParams(GlobalData.app());
    }

    private void initView() {
        mBeautyIv = (ImageView) findViewById(R.id.beauty_btn);
        mBeautyIv.setSelected(true);

        if (!mMagicParams.isBeauty()) {
            mBeautyIv.setVisibility(View.INVISIBLE);
        } else if (mMagicParams.isMultiBeauty()) {
            mBeautyIv.setVisibility(View.VISIBLE);
            mBeautyIv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //多級美顏
                    if (mMultiBeautyContainer.getVisibility() != View.VISIBLE) {
                        showBeautyAnim();
                    } else {
                        hideBeautyAnim();
                    }
                }
            });
            mMultiBeautyContainer = (ViewGroup) findViewById(R.id.beauty_level_container);
            mSingleChooser = new SingleChooser(
                    new SingleChooser.IChooserListener() {
                        @Override
                        public void onItemSelected(View view) {
                            int i = view.getId();
                            if (i == R.id.high_tv) {
                                updateBeautyIv(mMagicParams.getBeautyLevel(3));
                            } else if (i == R.id.middle_tv) {
                                updateBeautyIv(mMagicParams.getBeautyLevel(2));
                            } else if (i == R.id.low_tv) {
                                updateBeautyIv(mMagicParams.getBeautyLevel(1));
                            } else if (i == R.id.close_tv) {
                                updateBeautyIv(mMagicParams.getBeautyLevel(0));
                            }
                            hideBeautyAnim();
                        }
                    });
            mSingleChooser.setup(mMultiBeautyContainer, 0);
            initMultiSelected();
        } else {
            mBeautyIv.setVisibility(View.VISIBLE);
            mBeautyIv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isSelect = !view.isSelected();
                    view.setSelected(isSelect);
                    updateBeautyIv(mMagicParams.getBeautyLevel(isSelect ? 1 : 0));
                }
            });
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
            mShowAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    if (mBeautyCallBack != null) {
                        mBeautyCallBack.showMultiBeautyAnim();
                    }
                    mMultiBeautyContainer.setVisibility(View.VISIBLE);
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
            mHideAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (mBeautyCallBack != null) {
                        mBeautyCallBack.hideMultiBeautyAnim();
                    }
                    mMultiBeautyContainer.setVisibility(View.GONE);
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

    public void destroy() {
        mMagicParamPresenter.destroy();
    }

    public interface IBeautyCallBack {
        void showMultiBeautyAnim();

        void hideMultiBeautyAnim();
    }

    public interface IMagicParamsCallBack {
        void onComplete();
    }
}
