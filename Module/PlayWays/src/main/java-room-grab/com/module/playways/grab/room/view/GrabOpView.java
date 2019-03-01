package com.module.playways.grab.room.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.module.playways.grab.room.event.SomeOneLightBurstEvent;
import com.module.playways.grab.room.event.SomeOneLightOffEvent;
import com.module.rank.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static android.animation.ValueAnimator.REVERSE;
import static android.view.animation.Animation.INFINITE;

/**
 * 抢唱模式操作面板
 * 倒计时 抢 灭 等按钮都在上面
 */
public class GrabOpView extends RelativeLayout {
    public final static String TAG = "GrabOpView";
    public static final long SHOW_BURST_DELAY_TIME = 15000;

    public static final int MSG_HIDE_FROM_END_GUIDE_AUDIO = 0;
    public static final int MSG_HIDE = 1;
    public static final int MSG_SHOW_BRUST_BTN = 2;

    public static final int STATUS_GRAP = 1;
    public static final int STATUS_COUNT_DOWN = 2;
    //可操作
    public static final int STATUS_CAN_OP = 3;
    //已经操作完成
    public static final int STATUS_HAS_OP = 4;

    int mSeq = -1;

    RoundRectangleView mRrlProgress;

    int mStatus;

    public ExImageView mBtnIv;

    ExTextView mIvLightOff;

    Listener mListener;

    ExImageView mIvBurst;

    RelativeLayout mGrabContainer;

    HandlerTaskTimer mCountDownTask;

    Handler mUiHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_HIDE_FROM_END_GUIDE_AUDIO:
                    hide();
                    if (mListener != null) {
                        mListener.grabCountDownOver();
                    }
                    break;
                case MSG_HIDE:
                    mIvLightOff.setVisibility(GONE);
                    mGrabContainer.setVisibility(GONE);
                    mIvBurst.setVisibility(GONE);
                    break;
                case MSG_SHOW_BRUST_BTN:
                    TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,1.0f,Animation.RELATIVE_TO_SELF,0.0f,
                            Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0);
                    animation.setDuration(200);
                    animation.setRepeatMode(Animation.REVERSE);
                    animation.setInterpolator(new OvershootInterpolator());
                    animation.setFillAfter(true);
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
        mBtnIv = this.findViewById(R.id.iv_text);
        mRrlProgress = findViewById(R.id.rrl_progress);
        mIvLightOff = findViewById(R.id.iv_light_off);
        mGrabContainer = findViewById(R.id.grab_container);
        mIvBurst = findViewById(R.id.iv_burst);

        mBtnIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                MyLog.d(TAG,"mStatus ==" + mStatus);
                if (mStatus == STATUS_GRAP) {
                    if (mListener != null) {
                        mListener.clickGrabBtn(mSeq);
                        mBtnIv.setClickable(false);
                    }
                }
            }
        });

        mIvBurst.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                MyLog.d(TAG,"mStatus ==" + mStatus);
                if (mStatus == STATUS_CAN_OP) {
                    if (mListener != null) {
                        mListener.clickBurst(mSeq);
                        mIvBurst.setClickable(false);
                    }
                }
            }
        });

        mIvLightOff.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                MyLog.d(TAG,"mStatus ==" + mStatus);
                if(mStatus == STATUS_CAN_OP){
                    if (mListener != null) {
                        mListener.clickLightOff();
                    }
                }
            }
        });
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    /**
     *
     * @param num     倒计时时间，倒计时结束后变成想唱
     * @param waitNum 等待想唱时间
     */
    public void playCountDown(int seq, int num, int waitNum) {
        // 播放 3 2 1 导唱倒计时
        MyLog.d(TAG, "playCountDown");
        mSeq = seq;

        mStatus = STATUS_COUNT_DOWN;
        onChangeState();
        mUiHandler.removeCallbacksAndMessages(null);

        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,1.0f,Animation.RELATIVE_TO_SELF,0.0f,
                Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0);
        animation.setDuration(200);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setInterpolator(new OvershootInterpolator());
        animation.setFillAfter(true);
        startAnimation(animation);

        cancelCountDownTask();
        mCountDownTask = HandlerTaskTimer.newBuilder().interval(1000)
                .take(num)
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
                        mBtnIv.setImageDrawable(drawable);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        if (mListener != null) {
                            mListener.countDownOver();
                        }
                        // 按钮变成抢唱，且可点击
                        mUiHandler.removeMessages(MSG_HIDE_FROM_END_GUIDE_AUDIO);

                        if(waitNum <= 0){
                            MyLog.e(TAG, "等待时间是0");
                            Message msg = mUiHandler.obtainMessage(MSG_HIDE_FROM_END_GUIDE_AUDIO);
                            mUiHandler.sendMessageDelayed(msg, 0);
                        } else {
                            mRrlProgress.startCountDown(waitNum - 2000);
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
    private void onChangeState(){
        switch (mStatus){
            case STATUS_COUNT_DOWN:
                mIvLightOff.setVisibility(GONE);
                mIvBurst.setVisibility(GONE);
                mGrabContainer.setVisibility(VISIBLE);
                mBtnIv.setEnabled(false);
                mBtnIv.setBackground(U.getDrawable(R.drawable.xiangchang_bj));

                break;
            case STATUS_GRAP:
                mGrabContainer.setVisibility(VISIBLE);
                mIvLightOff.setVisibility(GONE);
                mIvBurst.setVisibility(GONE);
                mBtnIv.setEnabled(true);
                mBtnIv.setBackground(null);
                mBtnIv.setBackground(null);
                Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20))
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setPressedDrawable(U.getDrawable(R.drawable.xiangchang_anxia))
                        .setUnPressedDrawable(U.getDrawable(R.drawable.xiangchang_daojishi))
                        .build();
                mBtnIv.setBackground(drawable);

                break;
            case STATUS_CAN_OP:
                mGrabContainer.setVisibility(GONE);
                mIvLightOff.setVisibility(VISIBLE);
                mIvBurst.setVisibility(VISIBLE);

                mIvLightOff.setBackground(U.getDrawable(R.drawable.miedeng_bj));
                mIvLightOff.setClickable(false);
                break;
            case STATUS_HAS_OP:
                hide();
                break;
        }

    }

    public void hide(){
        MyLog.d(TAG, "hide");
        cancelCountDownTask();
        mBtnIv.clearAnimation();
        mRrlProgress.stopCountDown();
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,1.0f,
                Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0);
        animation.setDuration(200);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setInterpolator(new OvershootInterpolator());
        animation.setFillAfter(true);
        startAnimation(animation);

        mUiHandler.removeMessages(MSG_HIDE_FROM_END_GUIDE_AUDIO);
        mUiHandler.removeMessages(MSG_SHOW_BRUST_BTN);
        Message msg = mUiHandler.obtainMessage(MSG_HIDE);
        mUiHandler.sendMessageDelayed(msg, 200);
    }

    /**
     * 开始演唱
     */
    public void toSingState() {
        MyLog.d(TAG, "toSingState");

        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,1.0f,Animation.RELATIVE_TO_SELF,0.0f,
                Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0);
        animation.setDuration(200);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setInterpolator(new OvershootInterpolator());
        animation.setFillAfter(true);
        startAnimation(animation);

        setVisibility(VISIBLE);
        mStatus = STATUS_CAN_OP;
        onChangeState();

        mUiHandler.removeCallbacksAndMessages(null);

        cancelCountDownTask();
        mCountDownTask = HandlerTaskTimer.newBuilder().interval(1000)
                .take(6)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        int num1 = 7 - integer - 1;
//                        Drawable drawable = null;
//                        switch (num1) {
//                            case 5:
//                                drawable = U.getDrawable(R.drawable.mie_5);
//                                break;
//                            case 4:
//                                drawable = U.getDrawable(R.drawable.mie_4);
//                                break;
//                            case 3:
//                                drawable = U.getDrawable(R.drawable.mie_3);
//                                break;
//                            case 2:
//                                drawable = U.getDrawable(R.drawable.mie_2);
//                                break;
//                            case 1:
//                                drawable = U.getDrawable(R.drawable.mie_1);
//                                break;
//                        }

                        mIvLightOff.setText(String.valueOf(num1));
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        if (mListener != null) {
                            mListener.countDownOver();
                        }

                        mIvLightOff.setClickable(true);
                        Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20))
                                .setShape(DrawableCreator.Shape.Rectangle)
                                .setPressedDrawable(U.getDrawable(R.drawable.grab_yanchang_miedeng))
                                .setUnPressedDrawable(U.getDrawable(R.drawable.grab_miedeng_anxia))
                                .build();

                        mIvLightOff.setBackground(drawable);

                    }
                });

        Message msg = Message.obtain();
        msg.what = MSG_SHOW_BRUST_BTN;
        mUiHandler.sendMessageDelayed(msg, SHOW_BURST_DELAY_TIME);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SomeOneLightBurstEvent event) {
        if(mSeq == event.getRoundInfo().getRoundSeq()){
            mStatus = STATUS_HAS_OP;
            onChangeState();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SomeOneLightOffEvent event) {
        if(mSeq == event.getRoundInfo().getRoundSeq()){
            mStatus = STATUS_HAS_OP;
            onChangeState();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBtnIv.clearAnimation();
        cancelCountDownTask();
        mUiHandler.removeCallbacksAndMessages(null);
    }

    private void cancelCountDownTask(){
        if(mCountDownTask != null){
            mCountDownTask.dispose();
        }
    }

    public interface Listener {
        void clickGrabBtn(int seq);

        void clickLightOff();

        void clickBurst(int seq);

        void grabCountDownOver();

        void countDownOver();
    }
}
