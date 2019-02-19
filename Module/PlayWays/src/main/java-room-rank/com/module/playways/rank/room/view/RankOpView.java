package com.module.playways.rank.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.common.utils.HandlerTaskTimer;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.rank.R;
import java.util.concurrent.TimeUnit;

public class RankOpView extends RelativeLayout {
    ExImageView mIvBurst;
    ExImageView mIvTurnOff;
    ExTextView mTvCountDown;

    int mSeq;

    OpListener mOpListener;

    HandlerTaskTimer mCountDownTask;

    volatile boolean mBurstSuccess = false;

    public RankOpView(Context context) {
        super(context);
        init();
    }

    public RankOpView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.rank_op_view, this);
        mIvBurst = findViewById(R.id.iv_burst);
        mIvTurnOff = findViewById(R.id.iv_turn_off);
        mTvCountDown = findViewById(R.id.tv_count_down);

        RxView.clicks(mIvBurst)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .filter(o -> !mBurstSuccess)
                .subscribe(o -> {
                    mOpListener.clickBurst(mSeq);
                });

        RxView.clicks(mIvTurnOff)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    mOpListener.clickLightOff(mSeq);
                });
    }

    public void setOpListener(OpListener opListener) {
        mOpListener = opListener;
    }

    public void playCountDown(int seq) {
        mSeq = seq;
        cancelTimer();
        mIvTurnOff.setEnabled(false);

        mCountDownTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(20)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        if (integer == 0) {
                            mIvTurnOff.setEnabled(true);
                        }
                        mTvCountDown.setText(integer + "");
                    }
                });
    }

    private void cancelTimer() {
        if (mCountDownTask != null) {
            mCountDownTask.dispose();
        }
    }

    public void burstSuccess(boolean success, int seq) {
        mBurstSuccess = success;
        if (mBurstSuccess) {
            mIvBurst.setVisibility(GONE);
        }
    }

    public void lightOffSuccess(boolean success, int seq) {
        if (seq == mSeq) {

        }
    }

    public interface OpListener {
        void clickBurst(int seq);

        void clickLightOff(int seq);
    }
}
