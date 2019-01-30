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
import com.common.view.ex.ExImageView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.rank.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

import static android.animation.ValueAnimator.REVERSE;
import static android.view.animation.Animation.INFINITE;

/**
 * 抢唱模式操作面板
 * 倒计时 抢 灭 等按钮都在上面
 */
public class GrabOpView extends RelativeLayout {
    public final static String TAG = "GrabOpView";

    public static final int MSG_HIDE_FROM_END_GUIDE_AUDIO = 0;
    public static final int MSG_HIDE = 1;

    public static final int STATUS_GRAP = 1;
    public static final int STATUS_COUNT_DOWN = 2;
    public static final int STATUS_CAN_LIGHT_OFF = 3;
    public static final int STATUS_LIGHT_OFF = 4;


    RoundRectangleView mRrlProgress;

    int mStatus;

    public ExImageView mBtnIv;

    ExImageView mIvLightOff;

    Listener mListener;

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
        mBtnIv = (ExImageView) this.findViewById(R.id.iv_text);
        mRrlProgress = (RoundRectangleView) findViewById(R.id.rrl_progress);
        mIvLightOff = (ExImageView) findViewById(R.id.iv_light_off);
        mGrabContainer = (RelativeLayout)findViewById(R.id.grab_container);

        mBtnIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStatus == STATUS_GRAP) {
                    if (mListener != null) {
                        mListener.clickGrabBtn();
                        mBtnIv.setClickable(false);
                    }
                }
            }
        });

        RxView.clicks(mIvLightOff)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .filter(new Predicate<Object>() {
            @Override
            public boolean test(Object o) {
                return mStatus == STATUS_CAN_LIGHT_OFF;
            }
        }).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                if (mListener != null) {
                    mListener.clickLightOff();
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
    public void playCountDown(int num, int waitNum) {
        // 播放 3 2 1 导唱倒计时
        MyLog.d(TAG, "playCountDown");
        mBtnIv.clearAnimation();
        mBtnIv.setClickable(false);
        mIvLightOff.setVisibility(GONE);
        mGrabContainer.setVisibility(VISIBLE);
        mStatus = STATUS_COUNT_DOWN;
        mUiHandler.removeMessages(MSG_HIDE_FROM_END_GUIDE_AUDIO);
        mUiHandler.removeMessages(MSG_HIDE);

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
                                drawable = U.getDrawable(R.drawable.zhanji_3);
                                break;
                            case 2:
                                drawable = U.getDrawable(R.drawable.zhanji_2);
                                break;
                            case 1:
                                drawable = U.getDrawable(R.drawable.zhanji_1);
                                break;
                        }
                        mBtnIv.setImageDrawable(drawable);
//                        mGrabOpBtn.setBackgroundResource(R.drawable.yanchangjiemian_dabian);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        if (mListener != null) {
                            mListener.countDownOver();
                        }
                        // 按钮变成抢唱，且可点击
                        mBtnIv.setClickable(true);
                        mBtnIv.setImageDrawable(U.getDrawable(R.drawable.xiangchang));

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

                        ScaleAnimation animation = new ScaleAnimation(
                                1.0f, 1.1f, 1.0f, 1.1f,
                                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
                        );

                        animation.setRepeatCount(INFINITE);
                        animation.setRepeatMode(REVERSE);
                        animation.setDuration(500);
                        mBtnIv.startAnimation(animation);
                    }
                });
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
        mStatus = STATUS_CAN_LIGHT_OFF;
        mIvLightOff.setVisibility(VISIBLE);
        mIvLightOff.setBackground(U.getDrawable(R.drawable.mie_red_bj));
        mGrabContainer.setVisibility(GONE);
        mIvLightOff.setClickable(false);
        mUiHandler.removeMessages(MSG_HIDE_FROM_END_GUIDE_AUDIO);
        mUiHandler.removeMessages(MSG_HIDE);

        cancelCountDownTask();
        mCountDownTask = HandlerTaskTimer.newBuilder().interval(1000)
                .take(6)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        int num1 = 7 - integer - 1;
                        Drawable drawable = null;
                        switch (num1) {
                            case 5:
                                drawable = U.getDrawable(R.drawable.mie_5);
                                break;
                            case 4:
                                drawable = U.getDrawable(R.drawable.mie_4);
                                break;
                            case 3:
                                drawable = U.getDrawable(R.drawable.mie_3);
                                break;
                            case 2:
                                drawable = U.getDrawable(R.drawable.mie_2);
                                break;
                            case 1:
                                drawable = U.getDrawable(R.drawable.mie_1);
                                break;
                        }

                        mIvLightOff.setImageDrawable(drawable);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        if (mListener != null) {
                            mListener.countDownOver();
                        }
                        // 按钮变成抢唱，且可点击
                        mIvLightOff.setClickable(true);
                        mIvLightOff.setImageDrawable(U.getDrawable(R.drawable.miedeng_zi));
                    }
                });
    }

    public void toLightOffState(){
        if(mStatus == STATUS_CAN_LIGHT_OFF){
            mStatus = STATUS_LIGHT_OFF;
            mIvLightOff.setBackground(U.getDrawable(R.drawable.mie_an_bj));
            mIvLightOff.setClickable(false);
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
        void clickGrabBtn();

        void clickLightOff();

        void grabCountDownOver();

        void countDownOver();
    }
}
