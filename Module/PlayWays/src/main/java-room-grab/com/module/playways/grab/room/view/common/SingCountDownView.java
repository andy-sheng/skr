package com.module.playways.grab.room.view.common;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.utils.HandlerTaskTimer;
import com.common.view.countdown.CircleCountDownView;
import com.component.busilib.view.BitmapTextView;
import com.module.rank.R;

/**
 * 演唱时的倒计时view
 */
public class SingCountDownView extends RelativeLayout {
    public SingCountDownView(Context context) {
        super(context);
        init();
    }

    public SingCountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    ImageView mIvTag;
    CircleCountDownView mCircleCountDownView;
    BitmapTextView mCountDownTv;

    HandlerTaskTimer mCounDownTask;

    private void init() {
        inflate(getContext(), R.layout.grab_sing_count_down_view_layout, this);
        mIvTag = (ImageView) this.findViewById(R.id.iv_tag);
        mCircleCountDownView = (CircleCountDownView) this.findViewById(R.id.circle_count_down_view);
        mCountDownTv = (BitmapTextView) this.findViewById(R.id.count_down_tv);
    }

    public void reset() {
        mCircleCountDownView.setProgress(0);
        mCircleCountDownView.setMax(360);
        mCircleCountDownView.cancelAnim();
        if (mCounDownTask != null) {
            mCounDownTask.dispose();
        }
    }

    public void startPlay(int fromProgress, int totalMs, boolean playNow) {
        mCircleCountDownView.go(fromProgress, totalMs);
        if (playNow) {
            startCountDownText(totalMs / 1000);
        } else {
            if (mCounDownTask != null) {
                mCounDownTask.dispose();
            }
            mCountDownTv.setText(totalMs / 1000 + "");
        }
    }

    private void startCountDownText(int counDown) {
        if (mCounDownTask != null) {
            mCounDownTask.dispose();
        }
        mCounDownTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(counDown)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        mCountDownTv.setText((counDown - integer) + "");
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        if (mCounDownTask != null) {
                            mCounDownTask.dispose();
                        }
                    }
                });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mCounDownTask != null) {
            mCounDownTask.dispose();
        }
    }

    public void setTagImgRes(int resId) {
        mIvTag.setBackgroundResource(resId);
    }
}
