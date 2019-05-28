package com.module.playways.room.room.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.common.core.account.UserAccountManager;
import com.common.log.MyLog;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.playways.RoomDataUtils;
import com.module.playways.room.room.RankRoomData;
import com.module.playways.room.room.event.PkMyBurstSuccessEvent;
import com.module.playways.room.room.event.PkMyLightOffSuccessEvent;
import com.module.playways.R;

import java.util.HashSet;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class RankOpView extends RelativeLayout {
    public final static String TAG = "RankOpView";
    public final static int MSG_SHOW_BURST = 0;
    public int mLightOffDelayTime = 20;
    public int mLightBurstDelayTime = 30;
    ExImageView mIvBurst;
    ExImageView mIvTurnOff;
    ExImageView mIvCountDown;
    ExTextView mTvCountDown;

    int mSeq;

    OpListener mOpListener;

    HandlerTaskTimer mCountDownTask;

    RankRoomData mRoomData;

    HashSet<Integer> mHasOpSeq = new HashSet<>();

    ScaleAnimation mShakeAnimation; //抖动动画

    Handler mUiHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == MSG_SHOW_BURST){
                mIvBurst.setVisibility(VISIBLE);
                mIvBurst.setEnabled(true);
                TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
                animation.setDuration(200);
                animation.setRepeatMode(Animation.REVERSE);
                animation.setInterpolator(new OvershootInterpolator());
                animation.setFillAfter(true);
                mIvBurst.startAnimation(animation);
            }
        }
    };

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
        mIvCountDown = findViewById(R.id.iv_count_down);
        mTvCountDown = findViewById(R.id.tv_count_down);
        mIvBurst.setLongClickable(false);
        mIvBurst.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mRoomData.getLeftBurstLightTimes() > 0) {
                    if (mOpListener != null) {
                        if (mHasOpSeq.contains(mSeq)) {
                            U.getToastUtil().showShort("爆灯之后不能爆灯哦");
                            return;
                        }
                    }
                    mOpListener.clickBurst(mSeq);
                }
            }
        });

        mIvTurnOff.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });


        mIvTurnOff.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mRoomData.getLeftLightOffTimes() > 0) {
                    if (mOpListener != null) {
                        if (mHasOpSeq.contains(mSeq)) {
                            U.getToastUtil().showShort("灭灯之后不能再灭灯哦");
                            return;
                        }
                        MyLog.w(TAG, "clickValid LightOff " + " seq=" + mSeq);
                        mOpListener.clickLightOff(mSeq);
//                        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_RANK), "game_x", null);
                    }
                }
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
        if (mCountDownTask != null) {
            mCountDownTask.dispose();
        }
        mUiHandler.removeCallbacksAndMessages(null);
    }

    public void setRoomData(RankRoomData roomData) {
        mRoomData = roomData;
        mLightOffDelayTime = mRoomData.getGameConfigModel().getpKEnableShowMLightWaitTimeMs() / 1000;
        mLightBurstDelayTime = mRoomData.getGameConfigModel().getpKEnableShowBLightWaitTimeMs() / 1000;
    }

    public void setOpListener(OpListener opListener) {
        mOpListener = opListener;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PkMyBurstSuccessEvent event) {
        MyLog.w(TAG, "onEvent PkMyBurstSuccessEvent " + " seq is " + event.roundInfo.getRoundSeq());
        mHasOpSeq.add(event.roundInfo.getRoundSeq());
        if (RoomDataUtils.isCurrentRunningRound(event.roundInfo.getRoundSeq(), mRoomData)) {
            // 爆灯成功
            mIvBurst.setEnabled(false);
            TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
            animation.setDuration(200);
            animation.setRepeatMode(Animation.REVERSE);
            animation.setInterpolator(new OvershootInterpolator());
            animation.setFillAfter(true);
            mIvBurst.startAnimation(animation);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PkMyLightOffSuccessEvent event) {
        mHasOpSeq.add(event.roundInfo.getRoundSeq());
        MyLog.w(TAG, "onEvent PkMyLightOffSuccessEvent " + " seq is " + event.roundInfo.getRoundSeq());
        if (RoomDataUtils.isCurrentRunningRound(event.roundInfo.getRoundSeq(), mRoomData)) {
            // 灭灯成功
            mIvTurnOff.setEnabled(false);
        }
    }

    public void playCountDown(int seq, boolean startCountDown) {
        if (seq <= 0) {
            setVisibility(GONE);
            return;
        }

        mSeq = seq;
        cancelTimer();

        mIvBurst.setVisibility(GONE);
        if (mRoomData.getLeftBurstLightTimes() > 0 && startCountDown) {
            mUiHandler.removeMessages(MSG_SHOW_BURST);
            Message msg = mUiHandler.obtainMessage(MSG_SHOW_BURST);
            mUiHandler.sendMessageDelayed(msg, mLightBurstDelayTime * 1000);
        }

//        if (mRoomData.getLeftLightOffTimes() <= 0) {
//            mIvTurnOff.setVisibility(GONE);
//            mIvCountDown.setVisibility(GONE);
//            mTvCountDown.setVisibility(GONE);
//            stopShake();
//        } else {
//            mIvTurnOff.setVisibility(GONE);
//            mIvCountDown.setVisibility(VISIBLE);
//            mTvCountDown.setVisibility(VISIBLE);
//            mTvCountDown.setText(mLightOffDelayTime + "");
//            if (startCountDown) {
//                playShake();
//                mCountDownTask = HandlerTaskTimer.newBuilder()
//                        .interval(1000)
//                        .take(mLightOffDelayTime)
//                        .start(new HandlerTaskTimer.ObserverW() {
//                            @Override
//                            public void onNext(Integer integer) {
//                                integer = mLightOffDelayTime - integer;
//                                if (integer == 0) {
//                                    if (mRoomData.getLeftLightOffTimes() > 0) {
//                                        mIvTurnOff.setVisibility(VISIBLE);
//                                        mIvTurnOff.setEnabled(true);
//                                    } else {
//                                        mIvTurnOff.setVisibility(GONE);
//                                        mIvTurnOff.setEnabled(false);
//                                    }
//
//                                    mTvCountDown.setVisibility(GONE);
//                                    mIvCountDown.setVisibility(GONE);
//                                    mTvCountDown.setText("");
//                                    return;
//                                }
//
//                                mTvCountDown.setText(integer + "");
//                            }
//
//                            @Override
//                            public void onComplete() {
//                                super.onComplete();
//                                stopShake();
//                            }
//                        });
//            }
//        }
    }

    private void playShake() {
        mShakeAnimation = new ScaleAnimation(1.0f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mShakeAnimation.setInterpolator(new OvershootInterpolator());
        mShakeAnimation.setDuration(500);
        mShakeAnimation.setRepeatCount(Animation.INFINITE);
        mShakeAnimation.setRepeatMode(Animation.REVERSE);
        mTvCountDown.startAnimation(mShakeAnimation);
    }

    private void stopShake() {
        if (mShakeAnimation != null) {
            mShakeAnimation.cancel();
        }
    }

    private void cancelTimer() {
        if (mCountDownTask != null) {
            mCountDownTask.dispose();
        }
    }

    public interface OpListener {
        void clickBurst(int seq);

        void clickLightOff(int seq);
    }
}
