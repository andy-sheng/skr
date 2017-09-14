package com.wali.live.watchsdk.component.view.panel;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IOrientationListener;
import com.thornbirds.component.view.IViewProxy;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.view.PkScoreView;

/**
 * Created by yangli on 2017/09/11.
 * <p>
 * Generated using create_panel_with_presenter.py
 *
 * @module PK信息面板视图
 */
public class PkInfoPanel extends BaseBottomPanel<RelativeLayout, RelativeLayout>
        implements View.OnClickListener, IComponentView<PkInfoPanel.IPresenter, PkInfoPanel.IView> {
    private static final String TAG = "PkInfoPanel";

    @Nullable
    protected IPresenter mPresenter;

    @ColorInt
    private int mBackgroundColor;

    private View mLeftResultView;
    private View mLeftBgView;
    private ImageView mLeftImgView;
    private View mRightResultView;
    private View mRightBgView;
    private ImageView mRightImgView;
    private View mMiddleResultView;
    private View mMiddleBgView;
    private ImageView mMiddleImgView;

    private TextView mPkTypeView;
    private TextView mTimeAreaView;
    private SimpleDraweeView mAnchorLeft;
    private SimpleDraweeView mAnchorRight;
    private TextView mTicketLeft;
    private TextView mTicketRight;
    private PkScoreView mPkScoreView;
    private View mScoreAreaView;

    private final AnimationHelper mAnimationHelper = new AnimationHelper();

    private final Runnable mDelayStopTask = new Runnable() {
        @Override
        public void run() {
            if (mPresenter != null) {
                mPresenter.stopPresenter();
            }
        }
    };

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.pk_info_panel;
    }

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public PkInfoPanel(@NonNull RelativeLayout parentView) {
        super(parentView);
        mBackgroundColor = parentView.getResources().getColor(R.color.color_black_trans_30);
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();

        mLeftResultView = $(R.id.left_result);
        mLeftBgView = $(R.id.left_bg);
        mLeftImgView = $(R.id.left_view);

        mRightResultView = $(R.id.right_result);
        mRightBgView = $(R.id.right_bg);
        mRightImgView = $(R.id.right_view);

        mMiddleResultView = $(R.id.middle_result);
        mMiddleBgView = $(R.id.middle_bg);
        mMiddleImgView = $(R.id.middle_view);

        mPkTypeView = $(R.id.pk_type);
        mTimeAreaView = $(R.id.time_area);
        mAnchorLeft = $(R.id.anchor_1);
        mAnchorRight = $(R.id.anchor_2);
        mTicketLeft = $(R.id.ticket_1);
        mTicketRight = $(R.id.ticket_2);
        mScoreAreaView = $(R.id.score_area);
        mPkScoreView = $(R.id.pk_score_view);
    }

    @Override
    protected void orientSelf() {
        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) mContentView.getLayoutParams();
        if (mIsLandscape) {
            layoutParams.width = PANEL_WIDTH_LANDSCAPE;
            layoutParams.bottomMargin = DisplayUtils.dip2px(53.33f);
            mScoreAreaView.setBackgroundResource(R.drawable.score_area_bg);
        } else {
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.bottomMargin = DisplayUtils.dip2px(259.33f);
            mScoreAreaView.setBackgroundColor(mBackgroundColor);
        }
        mContentView.setLayoutParams(layoutParams);
    }

    @Override
    protected void onAnimationValue(@FloatRange(from = 0.0, to = 1.0) float value) {
        mContentView.setAlpha(value);
        mContentView.setTranslationY(mScoreAreaView.getHeight() * (1.0f - value));
    }

    @Override
    public IView getViewProxy() {
        class ComponentView implements IView {
            @Override
            public <T extends View> T getRealView() {
                return (T) mContentView;
            }

            @Override
            public boolean isShow() {
                return PkInfoPanel.this.isShow();
            }

            @Override
            public void showSelf(boolean useAnimation, boolean isLandscape) {
                clearAnimation();
                PkInfoPanel.this.hideSelf(false);
                PkInfoPanel.this.showSelf(useAnimation, isLandscape);
            }

            @Override
            public void hideSelf(boolean useAnimation) {
                PkInfoPanel.this.hideSelf(useAnimation);
                mAnimationHelper.stopAnimation();
            }

            @Override
            public void onPkStart(String pkType, long uuid1, long uuid2) {
                mPkTypeView.setText(pkType);

                String url1 = AvatarUtils.getAvatarUrlByUidTs(uuid1, 0, AvatarUtils.SIZE_TYPE_AVATAR_SMALL);
                FrescoWorker.loadImage(mAnchorLeft, ImageFactory.newHttpImage(url1)
                        .setIsCircle(true)
                        .setBorderColor(0xffff6100)
                        .setBorderWidth(DisplayUtils.dip2px(1f))
                        .build());
                String url2 = AvatarUtils.getAvatarUrlByUidTs(uuid2, 0, AvatarUtils.SIZE_TYPE_AVATAR_SMALL);
                FrescoWorker.loadImage(mAnchorRight, ImageFactory.newHttpImage(url2)
                        .setIsCircle(true)
                        .setBorderColor(0xff3961f4)
                        .setBorderWidth(DisplayUtils.dip2px(1f))
                        .build());
                mLeftResultView.setVisibility(View.GONE);
                mRightResultView.setVisibility(View.GONE);
                mMiddleResultView.setVisibility(View.GONE);
                mAnimationHelper.stopAnimation();
            }

            @Override
            public void onUpdateRemainTime(long remainTime) {
                mTimeAreaView.setText(String.format("%02d:%02d", remainTime / 60, remainTime % 60));
            }

            @Override
            public void onUpdateScoreInfo(long ticket1, long ticket2) {
                mTicketLeft.setText(String.valueOf(ticket1));
                mTicketRight.setText(String.valueOf(ticket2));
                mPkScoreView.updateRatio(ticket1, ticket2);
            }

            @Override
            public void onPkEnd(boolean ownerWin, long ticket1, long ticket2) {
                onUpdateScoreInfo(ticket1, ticket2);
                if (ownerWin) {
                    mLeftBgView.setBackgroundResource(R.drawable.live_img_pk_win_shining);
                    mLeftImgView.setImageResource(R.drawable.live_img_pk_win);
                    mRightBgView.setBackgroundResource(R.drawable.live_img_pk_lost_shining);
                    mRightImgView.setImageResource(R.drawable.live_img_pk_lost);
                } else {
                    mLeftBgView.setBackgroundResource(R.drawable.live_img_pk_lost_shining);
                    mLeftImgView.setImageResource(R.drawable.live_img_pk_lost);
                    mRightBgView.setBackgroundResource(R.drawable.live_img_pk_win_shining);
                    mRightImgView.setImageResource(R.drawable.live_img_pk_win);
                }
                mAnimationHelper.startDefeatAnimation();
            }

            @Override
            public void onPkEnd(long ticket1, long ticket2) {
                if (ticket1 == ticket2) {
                    onUpdateScoreInfo(ticket1, ticket2);
                    mMiddleBgView.setBackgroundResource(R.drawable.live_img_pk_win_shining);
                    mMiddleImgView.setImageResource(R.drawable.live_img_pk_draw);
                    mAnimationHelper.startTieAnimation();
                } else {
                    onPkEnd(ticket1 > ticket2, ticket1, ticket2);
                }
            }

            @Override
            public void onOrientation(boolean isLandscape) {
                PkInfoPanel.this.onOrientation(isLandscape);
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        void stopPresenter();
    }

    public interface IView extends IViewProxy, IOrientationListener {

        boolean isShow();

        void showSelf(boolean useAnimation, boolean isLandscape);

        void hideSelf(boolean useAnimation);

        /**
         * Pk开始
         *
         * @param pkType PK类型
         * @param uuid1  本房主ID
         * @param uuid2  对方ID
         */
        void onPkStart(String pkType, long uuid1, long uuid2);

        /**
         * 更新PK倒计时
         *
         * @param remainTime PK倒计时，以秒为单位
         */
        void onUpdateRemainTime(long remainTime);

        /**
         * 更新分数
         *
         * @param ticket1 本房主得分
         * @param ticket2 对方得分
         */
        void onUpdateScoreInfo(long ticket1, long ticket2);

        /**
         * Pk提前结束
         *
         * @param ownerWin 本房主获胜
         * @param ticket1  本房主得分
         * @param ticket2  对方得分
         */
        void onPkEnd(boolean ownerWin, long ticket1, long ticket2);

        /**
         * Pk正常结束，此时以分数定输赢
         *
         * @param ticket1 本房主得分
         * @param ticket2 对方得分
         */
        void onPkEnd(long ticket1, long ticket2);
    }

    protected class AnimationHelper {

        private AnimatorSet mAnimatorSet;
        private boolean mIsTie = false;

        private void setScale(View view, float scale) {
            view.setScaleX(scale);
            view.setScaleY(scale);
        }

        void resetAnimation() {
            if (mAnimatorSet == null) {
                ValueAnimator scaleAnimator1 = ValueAnimator.ofFloat(0, 1);
                scaleAnimator1.setDuration(1000);
                scaleAnimator1.setInterpolator(new OvershootInterpolator());
                scaleAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        if (mIsTie) {
                            setScale(mMiddleImgView, value);
                        } else {
                            setScale(mLeftImgView, value);
                            setScale(mRightImgView, value);
                        }
                    }
                });
                ValueAnimator scaleAnimator2 = ValueAnimator.ofFloat(0, 1);
                scaleAnimator2.setDuration(500);
                scaleAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        if (mIsTie) {
                            setScale(mMiddleBgView, value);
                        } else {
                            setScale(mLeftBgView, value);
                            setScale(mRightBgView, value);
                        }
                    }
                });
                ValueAnimator rotateAnimator = ValueAnimator.ofFloat(0f, 720f);
                rotateAnimator.setDuration(5000);
                rotateAnimator.setInterpolator(new LinearInterpolator());
                rotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        if (mIsTie) {
                            mMiddleBgView.setRotation(value);
                        } else {
                            mLeftBgView.setRotation(value);
                            mRightBgView.setRotation(value);
                        }
                    }
                });
                mAnimatorSet = new AnimatorSet();
                mAnimatorSet.play(scaleAnimator1);
                mAnimatorSet.play(scaleAnimator2).after(500);
                mAnimatorSet.play(rotateAnimator).after(scaleAnimator2);
            }
            if (mAnimatorSet.isStarted() || mAnimatorSet.isRunning()) {
                mAnimatorSet.cancel();
            }
        }

        void startTieAnimation() {
            resetAnimation();
            mIsTie = true;
            mMiddleResultView.setVisibility(View.VISIBLE);
            setScale(mMiddleBgView, 0);
            setScale(mMiddleImgView, 0);
            mParentView.removeCallbacks(mDelayStopTask);
            mParentView.postDelayed(mDelayStopTask, 6400);
            mAnimatorSet.start();
        }

        void startDefeatAnimation() {
            resetAnimation();
            mIsTie = false;
            mLeftResultView.setVisibility(View.VISIBLE);
            mRightResultView.setVisibility(View.VISIBLE);
            setScale(mLeftBgView, 0);
            setScale(mLeftImgView, 0);
            setScale(mRightBgView, 0);
            setScale(mRightImgView, 0);
            mParentView.removeCallbacks(mDelayStopTask);
            mParentView.postDelayed(mDelayStopTask, 6400);
            mAnimatorSet.start();
        }

        void stopAnimation() {
            if (mAnimatorSet != null && (mAnimatorSet.isStarted() || mAnimatorSet.isRunning())) {
                mAnimatorSet.cancel();
                mParentView.removeCallbacks(mDelayStopTask);
            }
        }
    }
}
