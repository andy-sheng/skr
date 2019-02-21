package com.module.playways.rank.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.utils.HandlerTaskTimer;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.rank.prepare.model.GameConfigModel;
import com.module.rank.R;

import java.util.concurrent.TimeUnit;

public class RankOpView extends RelativeLayout {
    public int mLightOffDelayTime = 20;
    ExImageView mIvBurst;
    ExImageView mIvTurnOff;
    ExTextView mTvCountDown;

    int mSeq;

    OpListener mOpListener;

    HandlerTaskTimer mCountDownTask;

    GameConfigModel mGameConfigModel;

    int mBurstMaxNum = 1;
    int mLightOffMaxNum = 2;

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

        mIvBurst.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mBurstMaxNum > 0 && mOpListener != null) {
                    mOpListener.clickBurst(mSeq);
                }
            }
        });

        RxView.clicks(mIvTurnOff)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .filter(o -> mLightOffMaxNum > 0)
                .subscribe(o -> {
                    if (mOpListener != null) {
                        mOpListener.clickLightOff(mSeq);
                    }
                });
    }

    public void setOpListener(OpListener opListener, GameConfigModel gameConfigModel) {
        mOpListener = opListener;
        mGameConfigModel = gameConfigModel;
        if (mGameConfigModel != null) {
            mBurstMaxNum = mGameConfigModel.getpKMaxShowBLightTimes();
            mLightOffMaxNum = mGameConfigModel.getpKMaxShowMLightTimes();
            mLightOffDelayTime = mGameConfigModel.getpKEnableShowMLightWaitTimeMs() / 1000;
        }
    }

    public void playCountDown(int seq) {
        if (seq <= 0) {
            return;
        }

        mSeq = seq;
        cancelTimer();
        mIvTurnOff.setVisibility(GONE);
        mTvCountDown.setVisibility(VISIBLE);

        mCountDownTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(mLightOffDelayTime)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        integer = mLightOffDelayTime - integer;
                        if (integer == 0) {
                            mIvTurnOff.setVisibility(VISIBLE);
                            mTvCountDown.setVisibility(GONE);
                            mTvCountDown.setText("");
                            return;
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
        if (success) {
            mBurstMaxNum--;
            //没有爆灯机会了
            if (mBurstMaxNum <= 0) {
                mIvBurst.setVisibility(GONE);
            }
        }
    }

    public void lightOffSuccess(boolean success, int seq) {
        if (seq == mSeq) {
            mLightOffMaxNum--;
            //没有灭灯机会了
            if (mLightOffMaxNum <= 0) {

            }
        }
    }

    public interface OpListener {
        void clickBurst(int seq);

        void clickLightOff(int seq);
    }
}
