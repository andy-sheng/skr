package com.module.playways.rank.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.rank.prepare.model.GameConfigModel;
import com.module.rank.R;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class RankOpView extends RelativeLayout {
    public final static String TAG = "RankOpView";
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

    HashSet<Integer> mHasOpSeq = new HashSet<>();

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
                .filter(o -> mBurstMaxNum > 0)
                .subscribe(o -> {
                    if (mOpListener != null) {
                        if(mHasOpSeq.contains(mSeq)){
                            U.getToastUtil().showShort("灭灯之后不能爆灯哦");
                            return;
                        }
                        mOpListener.clickBurst(mSeq);
                    }
                });

        RxView.clicks(mIvTurnOff)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .filter(o -> mLightOffMaxNum > 0)
                .subscribe(o -> {
                    if (mOpListener != null) {
                        if(mHasOpSeq.contains(mSeq)){
                            U.getToastUtil().showShort("爆灯之后不能灭灯哦");
                            return;
                        }
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
            mBurstMaxNum = 1;
            mLightOffMaxNum = 2;
            mLightOffDelayTime = 20;
        }
    }

    public void playCountDown(int seq) {
        if (seq <= 0) {
            setVisibility(GONE);
            return;
        }

        mSeq = seq;
        cancelTimer();

        if(mBurstMaxNum <= 0){
            mIvBurst.setVisibility(GONE);
        } else {
            mIvBurst.setVisibility(VISIBLE);
            mIvBurst.setEnabled(true);
        }

        if(mLightOffMaxNum <= 0){
            mIvTurnOff.setVisibility(GONE);
            mTvCountDown.setVisibility(GONE);
        } else {
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
                                if(mLightOffMaxNum > 0){
                                    mIvTurnOff.setVisibility(VISIBLE);
                                }

                                mTvCountDown.setVisibility(GONE);
                                mTvCountDown.setText("");
                                return;
                            }

                            mTvCountDown.setText(integer + "");
                        }
                    });
        }
    }

    private void cancelTimer() {
        if (mCountDownTask != null) {
            mCountDownTask.dispose();
        }
    }

    public void burstSuccess(boolean success, int seq) {
        MyLog.w(TAG, "burstSuccess" + " success=" + success + " seq=" + seq);
        if(success){
            mHasOpSeq.add(seq);
            mBurstMaxNum--;
            MyLog.w(TAG, "burstSuccess" + " mBurstMaxNum=" + mBurstMaxNum + ", mSeq " + mSeq);
            if (seq == mSeq) {
                mIvBurst.setEnabled(false);
            }
        }
    }

    public void lightOffSuccess(boolean success, int seq) {
        MyLog.w(TAG, "lightOffSuccess" + " success=" + success + " seq=" + seq);
        if (success) {
            mHasOpSeq.add(seq);
            mLightOffMaxNum--;
            MyLog.w(TAG, "lightOffSuccess" + " mLightOffMaxNum=" + mLightOffMaxNum + ", mSeq " + mSeq);
            if(seq == mSeq){
                mIvTurnOff.setEnabled(false);
            }
        }
    }

    public interface OpListener {
        void clickBurst(int seq);

        void clickLightOff(int seq);
    }
}
