package com.wali.live.watchsdk.component.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.gift.redenvelope.RedEnvelopeModel;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.component.presenter.EnvelopePresenter;

/**
 * Created by yangli on 2017/07/12.
 *
 * @module 抢红包视图
 */
public class EnvelopeView extends BaseBottomPanel<RelativeLayout, RelativeLayout>
        implements View.OnClickListener {
    private static final String TAG = "EnvelopeView";

    public final static int ENVELOPE_TYPE_SMALL = 1;
    public final static int ENVELOPE_TYPE_MIDDLE = 2;
    public final static int ENVELOPE_TYPE_LARGE = 3;

    private EnvelopePresenter.EnvelopeInfo mEnvelopeInfo;
    private IPresenter mPresenter;

    private View mTopView;
    private View mBottomView;
    private BaseImageView mSenderAvatarIv;
    private ImageView mUserBadgeIv;
    private TextView mNameTv;
    private TextView mInfoTv;
    private TextView mGrabBtn;

    private ObjectAnimator mRotationAnimator;

    private Drawable getDrawable(@DrawableRes int res) {
        return AppCompatResources.getDrawable(mContentView.getContext(), res);
    }

    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public EnvelopePresenter.EnvelopeInfo getEnvelopeInfo() {
        return mEnvelopeInfo;
    }

    public EnvelopeView(@NonNull RelativeLayout parentView) {
        super(parentView);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.red_envelope_view;
    }

    @Override
    public void onClick(View v) {
        if (mPresenter == null || mEnvelopeInfo == null) {
            return;
        }
        int i = v.getId();
        if (i == R.id.grab_btn) {
            onGrabClick();
        } else if (i == R.id.close_iv) {
            mPresenter.removeEnvelope(mEnvelopeInfo);
        }
    }

    public void startRotation() {
        if (mContentView == null) {
            return;
        }
        if (mRotationAnimator == null) {
            mRotationAnimator = ObjectAnimator.ofFloat(mGrabBtn, "rotationY", 0f, 360f);
            mRotationAnimator.setDuration(300);
            mRotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            mRotationAnimator.setRepeatMode(ObjectAnimator.RESTART);
        }
        if (!mRotationAnimator.isStarted() && !mRotationAnimator.isRunning()) {
            mRotationAnimator.start();
        }
    }

    public void stopRotation() {
        if (mContentView == null) {
            return;
        }
        if (mRotationAnimator != null) {
            mRotationAnimator.cancel();
        }
        mGrabBtn.setRotationY(0);
    }

    private void onGrabClick() {
        if (AccountAuthManager.triggerActionNeedAccount(mParentView.getContext())) {
            startRotation();
            mPresenter.grabEnvelope(mEnvelopeInfo);
        }
    }

    public void setEnvelopeInfo(EnvelopePresenter.EnvelopeInfo envelopeInfo) {
        mEnvelopeInfo = envelopeInfo;
        if (envelopeInfo == null || envelopeInfo.envelopeModel == null || mContentView == null) {
            return;
        }
        RedEnvelopeModel model = envelopeInfo.envelopeModel;
        switch (model.getType()) {
            case ENVELOPE_TYPE_SMALL:
                mTopView.setBackground(getDrawable(R.drawable.red_packet_bg_top_1));
                mBottomView.setBackground(getDrawable(R.drawable.red_packet_bg_bottom_1));
                break;
            case ENVELOPE_TYPE_MIDDLE:
                mTopView.setBackground(getDrawable(R.drawable.red_packet_top_2));
                mBottomView.setBackground(getDrawable(R.drawable.red_packet_bg_23));
                break;
            case ENVELOPE_TYPE_LARGE:
            default:
                mTopView.setBackground(getDrawable(R.drawable.red_packet_top_3));
                mBottomView.setBackground(getDrawable(R.drawable.red_packet_bg_23));
                break;
        }
        AvatarUtils.loadAvatarByUidTs(mSenderAvatarIv, model.getUserId(), model.getAvatarTimestamp(), true);
        mUserBadgeIv.setImageDrawable(ItemDataFormatUtils.getLevelSmallImgSource(model.getLevel()));
        mNameTv.setText(model.getNickName());
        mInfoTv.setText(model.getMsg());
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();
        mTopView = $(R.id.bg_top);
        mBottomView = $(R.id.bg_bottom);
        mSenderAvatarIv = $(R.id.sender_avatar_iv);
        mUserBadgeIv = $(R.id.user_badge_iv);
        mNameTv = $(R.id.name_tv);
        mInfoTv = $(R.id.info_tv);
        mGrabBtn = $(R.id.grab_btn);

        $click(R.id.grab_btn, this);
        $click(R.id.close_iv, this);

        setEnvelopeInfo(mEnvelopeInfo);
    }

    @Override
    public void showSelf(boolean useAnimation, boolean isLandscape) {
        super.showSelf(useAnimation, isLandscape);
        mContentView.bringToFront();
    }

    @Override
    public void hideSelf(boolean useAnimation) {
        super.hideSelf(useAnimation);
        stopRotation();
    }

    @Override
    protected void orientSelf() {
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams) mContentView.getLayoutParams();
        if (mIsLandscape) {
            mContentView.setTranslationY(0);
        } else {
            mContentView.setTranslationY(DisplayUtils.dip2px(80));
        }
    }

    @Override
    protected AnimationHelper createAnimationHelper() {
        return new AnimationHelper();
    }

    @Override
    protected void onAnimationValue(
            @FloatRange(from = 0.0, to = 1.0) float value) {
        mContentView.setAlpha(value);
        if (mIsLandscape) {
            int whole = (mParentView.getHeight() + mContentView.getHeight() / 2) / 2;
            mContentView.setTranslationY(whole * (value - 1.0f));
        } else {
            int whole = (mParentView.getHeight() + mContentView.getHeight() / 2) / 2 + DisplayUtils.dip2px(80);
            mContentView.setTranslationY(whole * (value - 1.0f) + DisplayUtils.dip2px(80));
        }
    }

    protected class AnimationHelper extends BaseBottomPanel.AnimationHelper {
        protected AnimatorSet animatorSet;
        protected ValueAnimator rotateAnimator;

        protected void setupAnimation() {
            super.setupAnimation();
            // 设置下落动画
            valueAnimator.setDuration(600);
            valueAnimator.setInterpolator(new Interpolator() {
                @Override
                public float getInterpolation(float t) { // 参见walilive的RedPacketInterpolator
                    t *= 1.1041f;
                    return t < 0.6126f ? t * 1.5846f :
                            (t - 0.9f) * (t - 0.9f) * 1.44f + 0.94f;
                }
            });
            // 设置旋转动画
            if (rotateAnimator == null) {
                rotateAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
                rotateAnimator.setDuration(400);
                rotateAnimator.setInterpolator(new LinearInterpolator());
                rotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float ratio = (float) animation.getAnimatedValue();
                        if (!mIsShow) {
                            ratio = 1 - ratio;
                        }
                        mContentView.setRotation(15f * (ratio - 1.0f));
                    }
                });
            }
            if (animatorSet == null) {
                animatorSet = new AnimatorSet();
                animatorSet.playTogether(valueAnimator, rotateAnimator);
            }
        }

        @Override
        protected void startAnimation() {
            setupAnimation();
            if (!animatorSet.isRunning()) {
                animatorSet.start();
            }
        }

        @Override
        protected void stopAnimation() {
            if (animatorSet != null && animatorSet.isStarted()) {
                animatorSet.cancel();
            }
        }
    }

    public interface IPresenter {
        /**
         * 关闭红包
         */
        void removeEnvelope(EnvelopePresenter.EnvelopeInfo envelopeInfo);

        /**
         * 抢红包
         */
        void grabEnvelope(EnvelopePresenter.EnvelopeInfo envelopeInfo);
    }
}
