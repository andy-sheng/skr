package com.module.playways.room.gift.scheduler;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;

import com.module.playways.room.gift.model.BaseGift;

public class ContinueSendScheduler {
    private BaseGift mBaseGift;

    private long mReceiverId;

    private int continueCount = 0;

    private long continueId = 0;

    private long mCanContinueDuration = 5000;

    ContinueSendListener mContinueSendListener;

    ValueAnimator mValueAnimator;

    public ContinueSendScheduler(long canContinueDuration, ContinueSendListener continueSendListener) {
        mCanContinueDuration = canContinueDuration;
        mContinueSendListener = continueSendListener;
    }

    public void send(BaseGift baseGift, long receiverId) {
        cancelAnimator();
        if (baseGift == mBaseGift && mReceiverId == receiverId) {
            if (mContinueSendListener != null) {
                mContinueSendListener.buyGift(baseGift, ++continueCount, continueId, mReceiverId);
            }
        } else {
            continueCount = 0;
            mBaseGift = baseGift;
            mReceiverId = receiverId;
            continueId = System.currentTimeMillis();
            if (mContinueSendListener != null) {
                mContinueSendListener.buyGift(baseGift, ++continueCount, continueId, mReceiverId);
                mContinueSendListener.continueSendStart();
            }
        }

        mValueAnimator = ValueAnimator.ofInt(360, 0);
        mValueAnimator.setDuration(mCanContinueDuration);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mContinueSendListener != null) {
                    int progress = (int) animation.getAnimatedValue();
                    mContinueSendListener.continueSendProgressUpdate(mCanContinueDuration, mCanContinueDuration * progress / 360, progress);
                }
            }
        });

        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation, boolean isReverse) {
                endContinueSend();
            }
        });
        mValueAnimator.start();
    }

    private void endContinueSend() {
        if (mContinueSendListener != null) {
            mContinueSendListener.continueSendEnd();
        }

        mBaseGift = null;
        continueCount = 0;
    }

    private void cancelAnimator() {
        if (mValueAnimator != null) {
            mValueAnimator.removeAllUpdateListeners();
            mValueAnimator.removeAllListeners();
            mValueAnimator.cancel();
        }
    }

    public interface ContinueSendListener {
        void continueSendStart();

        void continueSendProgressUpdate(long totalTime, long remainTime, int progress);

        void continueSendEnd();

        void buyGift(BaseGift baseGift, int continueCount, long continueId, long receiverId);
    }
}
