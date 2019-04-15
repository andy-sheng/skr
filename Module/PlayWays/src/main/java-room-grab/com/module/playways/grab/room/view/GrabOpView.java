package com.module.playways.grab.room.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.common.base.BaseActivity;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.GrabSomeOneLightBurstEvent;
import com.module.playways.grab.room.event.GrabSomeOneLightOffEvent;
import com.module.playways.grab.room.model.GrabConfigModel;
import com.module.rank.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 抢唱模式操作面板
 * 倒计时 抢 灭 等按钮都在上面
 */
public class GrabOpView extends RelativeLayout {
    public final static String TAG = "GrabOpView";
    public final static String KEY_SHOW_CHALLENGE_TIME = "showChallengeTime";
    public long mShowBurstTime = 15000;
    public long mShowLightOffTime = 5000;

    public static final int MSG_HIDE_FROM_END_GUIDE_AUDIO = 0;
    public static final int MSG_HIDE = 1;
    public static final int MSG_SHOW_BRUST_BTN = 2;

    // 抢唱阶段可抢
    public static final int STATUS_GRAP = 1;
    // 抢唱阶段倒计时
    public static final int STATUS_COUNT_DOWN = 2;
    // 演唱阶段可操作
    public static final int STATUS_CAN_OP = 3;
    // 演唱阶段已经操作完成
    public static final int STATUS_HAS_OP = 4;

    int mSeq = -1;

    // 抢唱按钮模块
    RelativeLayout mGrabContainer;
    ExImageView mGrabIv;
    RoundRectangleView mRrlProgress;

    // 挑战按钮模块
    RelativeLayout mGrab2Container;
    ExImageView mGrab2Iv;
    RoundRectangleView mRrl2Progress;
    ExImageView mCoinFlagIv;

    int mShowChallengeTime = 0;

    int mStatus;

    ExTextView mIvLightOff;

    Listener mListener;

    ExImageView mIvBurst;

    HandlerTaskTimer mCountDownTask;

    GrabRoomData mGrabRoomData;

    boolean mGrabPreRound = false; // 标记上一轮是否抢了

    Animation mExitAnimation;

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HIDE_FROM_END_GUIDE_AUDIO:
                    hide("MSG_HIDE_FROM_END_GUIDE_AUDIO");
                    if (mListener != null) {
                        mListener.grabCountDownOver();
                    }
                    break;
                case MSG_HIDE:
                    mIvLightOff.setVisibility(GONE);
                    mIvBurst.setVisibility(GONE);
                    mGrabContainer.setVisibility(GONE);
                    mGrab2Container.setVisibility(GONE);
                    mListener.hideChallengeTipView();
                    break;
                case MSG_SHOW_BRUST_BTN:
                    MyLog.d(TAG, "handleMessage" + " msg=" + MSG_SHOW_BRUST_BTN);
                    TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                            Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
                    animation.setDuration(200);
                    animation.setRepeatMode(Animation.REVERSE);
                    animation.setInterpolator(new OvershootInterpolator());
                    animation.setFillAfter(true);
                    mIvBurst.setVisibility(VISIBLE);
                    mIvBurst.startAnimation(animation);
                    mIvBurst.setEnabled(true);
                    break;
            }
        }
    };

    public GrabOpView(Context context) {
        super(context);
        init();
    }

    public GrabOpView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GrabOpView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_op_view_layout, this);
        {
            mGrabIv = this.findViewById(R.id.grab_iv);
            mRrlProgress = findViewById(R.id.rrl_progress);
            mGrabContainer = findViewById(R.id.grab_container);
            mGrabIv.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    MyLog.d(TAG, "mBtnIv mStatus ==" + mStatus);
                    if (mStatus == STATUS_GRAP) {
                        if (mListener != null) {
                            mListener.clickGrabBtn(mSeq, false);
                        }
                    }
                }
            });
        }
        {
            mGrab2Iv = this.findViewById(R.id.grab2_iv);
            mRrl2Progress = findViewById(R.id.rrl2_progress);
            mGrab2Container = findViewById(R.id.grab2_container);
            mCoinFlagIv = findViewById(R.id.coin_flag_iv);

            mGrab2Iv.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    MyLog.d(TAG, "mBtnIv mStatus ==" + mStatus);
                    if (mStatus == STATUS_GRAP) {
                        if (mListener != null) {
                            mListener.clickGrabBtn(mSeq, true);
                            mListener.hideChallengeTipView();
                        }
                    }
                }
            });
        }


        mIvBurst = findViewById(R.id.iv_burst);
        mIvBurst.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                MyLog.d(TAG, "mIvBurst mStatus ==" + mStatus);
                if (mStatus == STATUS_CAN_OP) {
                    if (mListener != null) {
                        mListener.clickBurst(mSeq);
                        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
                                "game_like", null);
//                        mIvBurst.setEnabled(false);
                    }
                }
            }
        });
        mIvLightOff = findViewById(R.id.iv_light_off);
        mIvLightOff.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                MyLog.d(TAG, "mIvLightOff mStatus ==" + mStatus);
                if (mStatus == STATUS_CAN_OP) {
                    if (mListener != null) {
                        mListener.clickLightOff();
                        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
                                "game_dislike", null);
                    }
                }
            }
        });

        mShowChallengeTime = U.getPreferenceUtils().getSettingInt(KEY_SHOW_CHALLENGE_TIME, 0);
    }

    public void setGrabRoomData(GrabRoomData grabRoomData) {
        mGrabRoomData = grabRoomData;
        if (mGrabRoomData == null || mGrabRoomData.getGrabConfigModel() == null) {
            MyLog.d(TAG, "setGrabRoomData GrabRoomData error");
            return;
        }
        mShowBurstTime = mGrabRoomData.getGrabConfigModel().getEnableShowBLightWaitTimeMs();
        mShowLightOffTime = mGrabRoomData.getGrabConfigModel().getEnableShowMLightWaitTimeMs();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     * @param num     倒计时时间，倒计时结束后变成想唱
     * @param waitNum 等待想唱时间
     */
    public void playCountDown(int seq, int num, int waitNum) {
        // 播放 3 2 1 导唱倒计时
        MyLog.d(TAG, "playCountDown" + " seq=" + seq + " num=" + num + " waitNum=" + waitNum);
        mSeq = seq;
        mStatus = STATUS_COUNT_DOWN;
        onChangeState();
        mUiHandler.removeCallbacksAndMessages(null);

        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        animation.setDuration(200);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setInterpolator(new OvershootInterpolator());
        animation.setFillAfter(true);
        startAnimation(animation);

        cancelCountDownTask();
        long interval = 1000;
        if (mGrabPreRound) {
            GrabConfigModel grabConfigModel = mGrabRoomData.getGrabConfigModel();
            if (grabConfigModel != null) {
                int delay = grabConfigModel.getWantSingDelayTimeMs() / 3;
                if (delay > 0) {
                    interval += delay;
                }
            }
        }
        MyLog.d(TAG, "playCountDown" + " interval=" + interval);
        mCountDownTask = HandlerTaskTimer.newBuilder().interval(interval)
                .take(num)
                .compose((BaseActivity) getContext())
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        int num1 = num - integer;
                        Drawable drawable = null;
                        switch (num1) {
                            case 3:
                                drawable = U.getDrawable(R.drawable.xiangchang_3);
                                break;
                            case 2:
                                drawable = U.getDrawable(R.drawable.xiangchang_2);
                                break;
                            case 1:
                                drawable = U.getDrawable(R.drawable.xiangchang_1);
                                break;
                        }
                        mIvBurst.setVisibility(GONE);
                        mGrabIv.setImageDrawable(drawable);
                        mGrab2Iv.setImageDrawable(drawable);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        if (mListener != null) {
                            mListener.countDownOver();
                        }
                        // 按钮变成抢唱，且可点击
                        mUiHandler.removeMessages(MSG_HIDE_FROM_END_GUIDE_AUDIO);
                        if (waitNum <= 0) {
                            MyLog.e(TAG, "等待时间是0");
                            Message msg = mUiHandler.obtainMessage(MSG_HIDE_FROM_END_GUIDE_AUDIO);
                            mUiHandler.sendMessageDelayed(msg, 0);
                        } else {
                            mRrlProgress.setVisibility(VISIBLE);
                            mRrlProgress.startCountDown(waitNum - 2000);

                            mRrl2Progress.setVisibility(VISIBLE);
                            mRrl2Progress.startCountDown(waitNum - 2000);
                            Message msg = mUiHandler.obtainMessage(MSG_HIDE_FROM_END_GUIDE_AUDIO);
                            mUiHandler.sendMessageDelayed(msg, waitNum - 2000);
                        }
                        mStatus = STATUS_GRAP;
                        onChangeState();
                    }
                });
    }

    /**
     * 状态发生变化
     */
    private void onChangeState() {
        MyLog.d(TAG, "onChangeState mStatus=" + mStatus);
        switch (mStatus) {
            case STATUS_COUNT_DOWN:
                mIvLightOff.setVisibility(GONE);
                mIvBurst.clearAnimation();
                mIvBurst.setVisibility(GONE);

                mGrabContainer.setVisibility(VISIBLE);
                mGrabIv.setEnabled(false);
                mGrabIv.setImageDrawable(null);
                mGrabIv.setBackground(U.getDrawable(R.drawable.ycdd_qiangchang_bj));

                if (mGrabRoomData.isChallengeAvailable()) {
                    mGrab2Container.setVisibility(VISIBLE);
                    mGrab2Iv.setEnabled(false);
                    mGrab2Iv.setImageDrawable(null);
                    mGrab2Iv.setBackground(U.getDrawable(R.drawable.ycdd_tiaozhan_bg));
                    mCoinFlagIv.setVisibility(GONE);


                    if(mShowChallengeTime < 3){
                        if(mListener != null){
                            mListener.showChallengeTipView();
                        }

                        U.getPreferenceUtils().setSettingInt(KEY_SHOW_CHALLENGE_TIME, ++mShowChallengeTime);
                    }
                }
                break;
            case STATUS_GRAP:
                mIvLightOff.setVisibility(GONE);
                mIvBurst.clearAnimation();
                mIvBurst.setVisibility(GONE);
            {
                mGrabContainer.setVisibility(VISIBLE);
                mGrabIv.setEnabled(true);
                mGrabIv.setImageDrawable(null);
                Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20))
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setPressedDrawable(U.getDrawable(R.drawable.ycdd_qiangchang_anxia))
                        .setUnPressedDrawable(U.getDrawable(R.drawable.ycdd_qiangchang))
                        .build();
                mGrabIv.setBackground(drawable);
                mGrabIv.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        //MyLog.d(TAG, "onTouch" + " v=" + v + " event=" + event);
                        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                            mRrlProgress.setVisibility(GONE);
                        } else {
                            mRrlProgress.setVisibility(VISIBLE);
                        }
                        return false;
                    }
                });
            }
            {
                if (mGrabRoomData.isChallengeAvailable()) {
                    mGrab2Container.setVisibility(VISIBLE);
                    mGrab2Iv.setEnabled(true);
                    mGrab2Iv.setImageDrawable(null);
                    Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20))
                            .setShape(DrawableCreator.Shape.Rectangle)
                            .setPressedDrawable(U.getDrawable(R.drawable.ycdd_tiaozhan_anxia))
                            .setUnPressedDrawable(U.getDrawable(R.drawable.ycdd_tiaozhan))
                            .build();
                    mGrab2Iv.setBackground(drawable);
                    mCoinFlagIv.setVisibility(VISIBLE);
                    mGrab2Iv.setOnTouchListener(new OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            //MyLog.d(TAG, "onTouch" + " v=" + v + " event=" + event);
                            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                                mRrl2Progress.setVisibility(GONE);
                            } else {
                                mRrl2Progress.setVisibility(VISIBLE);
                            }
                            return false;
                        }
                    });
                }
            }
            break;
            case STATUS_CAN_OP:
                mGrabContainer.setVisibility(GONE);
                mGrab2Container.setVisibility(GONE);
                mIvLightOff.setVisibility(VISIBLE);
                mIvLightOff.setBackground(U.getDrawable(R.drawable.miedeng_bj));
                mIvLightOff.setEnabled(false);
                break;
            case STATUS_HAS_OP:
                hide("STATUS_HAS_OP");
                break;
        }
    }

    public void setGrabPreRound(boolean grabPreRound) {
        mGrabPreRound = grabPreRound;
    }

    public void hide(String from) {
        MyLog.d(TAG, "hide from=" + from);
        cancelCountDownTask();
        mGrabIv.clearAnimation();
        mRrlProgress.stopCountDown();
        mGrab2Iv.clearAnimation();
        mRrl2Progress.stopCountDown();
        if (mExitAnimation == null) {
            mExitAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
            mExitAnimation.setDuration(200);
            mExitAnimation.setRepeatMode(Animation.REVERSE);
            mExitAnimation.setInterpolator(new OvershootInterpolator());
            mExitAnimation.setFillAfter(true);
            mExitAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mIvLightOff.setVisibility(GONE);
                    mIvBurst.setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
        if (mExitAnimation.hasStarted() && !mExitAnimation.hasEnded()) {

        } else {
            startAnimation(mExitAnimation);
        }
        mUiHandler.removeCallbacksAndMessages(null);
        Message msg = mUiHandler.obtainMessage(MSG_HIDE);
        mUiHandler.sendMessageDelayed(msg, 200);
    }

    /**
     * 开始演唱
     */
    public void toOtherSingState() {
        MyLog.d(TAG, "toOtherSingState");

        mStatus = STATUS_CAN_OP;
        onChangeState();

        mUiHandler.removeCallbacksAndMessages(null);

        cancelCountDownTask();
        mCountDownTask = HandlerTaskTimer.newBuilder().delay(mShowLightOffTime)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        if (mListener != null) {
                            mListener.countDownOver();
                        }

                        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
                        animation.setDuration(200);
                        animation.setRepeatMode(Animation.REVERSE);
                        animation.setInterpolator(new OvershootInterpolator());
                        animation.setFillAfter(true);
                        startAnimation(animation);
                        setVisibility(VISIBLE);

                        mIvLightOff.setEnabled(true);
                        Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20))
                                .setShape(DrawableCreator.Shape.Rectangle)
                                .setPressedDrawable(U.getDrawable(R.drawable.ycdd_miedeng_anxia))
                                .setUnPressedDrawable(U.getDrawable(R.drawable.ycdd_miedeng))
                                .build();

                        mIvLightOff.setBackground(drawable);
                    }
                });

        mUiHandler.removeMessages(MSG_SHOW_BRUST_BTN);
        Message msg = Message.obtain();
        msg.what = MSG_SHOW_BRUST_BTN;
        mUiHandler.sendMessageDelayed(msg, mShowBurstTime);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabSomeOneLightBurstEvent event) {
        if (mSeq == event.getRoundInfo().getRoundSeq()) {
            mStatus = STATUS_HAS_OP;
            onChangeState();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabSomeOneLightOffEvent event) {
        if (mSeq == event.getRoundInfo().getRoundSeq() && event.uid == MyUserInfoManager.getInstance().getUid()) {
            mStatus = STATUS_HAS_OP;
            onChangeState();
        }
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
        mGrabIv.clearAnimation();
        mGrab2Iv.clearAnimation();
        cancelCountDownTask();
        mUiHandler.removeCallbacksAndMessages(null);
        clearAnimation();
        mIvBurst.clearAnimation();
        mListener.hideChallengeTipView();
    }

    private void cancelCountDownTask() {
        if (mCountDownTask != null) {
            mCountDownTask.dispose();
        }
    }

    public interface Listener {
        void clickGrabBtn(int seq, boolean challenge);

        void clickLightOff();

        void clickBurst(int seq);

        void grabCountDownOver();

        void countDownOver();

        void showChallengeTipView();

        void hideChallengeTipView();
    }
}
