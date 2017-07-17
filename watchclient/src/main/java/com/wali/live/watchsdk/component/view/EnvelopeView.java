package com.wali.live.watchsdk.component.view;

import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
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
        if (mGrabBtn == null) {
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
        if (mGrabBtn == null) {
            return;
        }
        if (mRotationAnimator != null) {
            mRotationAnimator.cancel();
        }
        mGrabBtn.setTranslationY(0);
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
