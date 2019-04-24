package com.module.playways.room.gift.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.utils.U;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.room.gift.inter.IContinueSendView;
import com.module.playways.room.gift.model.BaseGift;
import com.module.playways.room.gift.presenter.BuyGiftPresenter;
import com.module.playways.room.gift.scheduler.ContinueSendScheduler;


public class ContinueSendView extends FrameLayout implements ContinueSendScheduler.ContinueSendListener, IContinueSendView {
    ImageView mIvBg;
    ImageView mIvContinueText;
    TextView mTvContinueNum;

    BaseGift mBaseGift;

    GrabRoomData mBaseRoomData;

    ContinueSendScheduler mContinueSendScheduler;

    BuyGiftPresenter mBuyGiftPresenter;

    AnimatorSet mScaleAnimatorSet;

    AnimatorSet mJumpAnimatorSet;

    long mReceiverId;

    public ContinueSendView(Context context) {
        super(context);
        init();
    }

    public ContinueSendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ContinueSendView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setBaseRoomData(GrabRoomData baseRoomData) {
        mBaseRoomData = baseRoomData;
    }

    public void startBuy(BaseGift baseGift, long receiverId) {
        mBaseGift = baseGift;
        mReceiverId = receiverId;
        if (baseGift.isCanContinue()) {
            mContinueSendScheduler.send(baseGift, receiverId);
            setVisibility(VISIBLE);
        } else {
            mBuyGiftPresenter.buyGift(baseGift, 1, mBaseRoomData.getGameId(), receiverId, System.currentTimeMillis());
        }
    }

    private void init() {
        inflate(getContext(), R.layout.continue_send_view_layout, this);
//        EventBus.getDefault().register(this);
        mIvBg = (ImageView) findViewById(R.id.iv_bg);
        mIvContinueText = (ImageView) findViewById(R.id.iv_continue_text);
        mTvContinueNum = (TextView) findViewById(R.id.tv_continue_num);

        mContinueSendScheduler = new ContinueSendScheduler(5000, this);
        mBuyGiftPresenter = new BuyGiftPresenter(this);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBaseGift != null) {
                    mContinueSendScheduler.send(mBaseGift, mReceiverId);
                }

                if (mScaleAnimatorSet != null) {
                    mScaleAnimatorSet.cancel();
                }

                ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(mIvContinueText, "scaleX", 1.0f, 0.8f, 1.0f);
                ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(mIvContinueText, "scaleY", 1.0f, 0.8f, 1.0f);
                mScaleAnimatorSet = new AnimatorSet();
                mScaleAnimatorSet.play(objectAnimator1).with(objectAnimator2);
                mScaleAnimatorSet.setDuration(500);
                mScaleAnimatorSet.start();
            }
        });
    }

    @Override
    public void buySuccess(BaseGift baseGift, int continueCount) {
        mTvContinueNum.setText("+" + String.valueOf(continueCount));
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(mTvContinueNum, "translationY", 0.0f, -40);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(mTvContinueNum, "alpha", 1.0f, 0.7f, 0.0f);

        if (mJumpAnimatorSet != null) {
            mJumpAnimatorSet.cancel();
        }

        mJumpAnimatorSet = new AnimatorSet();
        mJumpAnimatorSet.play(objectAnimator1).with(objectAnimator2);
        mJumpAnimatorSet.setDuration(500);
        mJumpAnimatorSet.start();
    }

    @Override
    public void continueSendStart() {

    }

    @Override
    public void continueSendProgressUpdate(long totalTime, long remainTime, int progress) {

    }

    @Override
    public void continueSendEnd() {
        setVisibility(GONE);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            mIvBg.clearAnimation();
        } else {
            RotateAnimation rotate = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            LinearInterpolator lin = new LinearInterpolator();
            rotate.setInterpolator(lin);
            rotate.setDuration(1000);
            rotate.setRepeatCount(-1);
            rotate.setFillAfter(true);
            rotate.setStartOffset(10);
            mIvBg.setAnimation(rotate);
        }
    }

    @Override
    public void buyGift(BaseGift baseGift, int continueCount, long continueId, long receiverId) {
        mBuyGiftPresenter.buyGift(baseGift, continueCount, mBaseRoomData.getGameId(), receiverId, continueId);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mScaleAnimatorSet != null) {
            mScaleAnimatorSet.cancel();
        }
        if (mJumpAnimatorSet != null) {
            mJumpAnimatorSet.cancel();
        }
        mIvBg.clearAnimation();
    }

    public void destroy() {
        mBuyGiftPresenter.destroy();
    }
}
