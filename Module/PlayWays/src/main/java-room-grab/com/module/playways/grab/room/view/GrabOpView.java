package com.module.playways.grab.room.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
    public static final int STATUS_GRAP = 1;
    public static final int STATUS_COUNT_DOWN = 2;
    public static final int STATUS_LIGHT_OFF = 3;

    RoundRectangleView mRrlProgress;

    int mStatus;

    public ExImageView mDescTv;

    ExImageView mIvLightOff;

    Listener mListener;

    RelativeLayout mGrabContainer;

    HandlerTaskTimer mCountDownTask;

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
        mDescTv = (ExImageView) this.findViewById(R.id.iv_text);
        mRrlProgress = (RoundRectangleView) findViewById(R.id.rrl_progress);
        mIvLightOff = (ExImageView) findViewById(R.id.iv_light_off);
        mGrabContainer = (RelativeLayout)findViewById(R.id.grab_container);

        mDescTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStatus == STATUS_GRAP) {
                    if (mListener != null) {
                        mListener.clickGrabBtn();
                    }
                }
            }
        });

        RxView.clicks(mIvLightOff)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .filter(new Predicate<Object>() {
            @Override
            public boolean test(Object o) {
                return mStatus == STATUS_LIGHT_OFF;
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
        MyLog.d(TAG, "toSingState");
        mDescTv.clearAnimation();
        mDescTv.setClickable(false);
        mIvLightOff.setVisibility(GONE);
        mGrabContainer.setVisibility(VISIBLE);
        mStatus = STATUS_COUNT_DOWN;

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
                        mDescTv.setImageDrawable(drawable);
//                        mGrabOpBtn.setBackgroundResource(R.drawable.yanchangjiemian_dabian);
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        if (mListener != null) {
                            mListener.countDownOver();
                        }
                        // 按钮变成抢唱，且可点击
                        mDescTv.setClickable(true);
                        mDescTv.setImageDrawable(U.getDrawable(R.drawable.xiangchang));
                        if(waitNum <= 3000){
                            MyLog.e(TAG, "等待时间是0，很严重的问题");
                        } else {
                            mRrlProgress.startCountDown(waitNum - 3000);
                        }

                        mStatus = STATUS_GRAP;

                        // 以view中心为缩放点，由初始状态放大两倍
                        ScaleAnimation animation = new ScaleAnimation(
                                1.0f, 1.1f, 1.0f, 1.1f,
                                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
                        );

                        animation.setRepeatCount(INFINITE);
                        animation.setRepeatMode(REVERSE);
                        animation.setDuration(500);
                        mDescTv.startAnimation(animation);
                    }
                });
    }

    public void hide(){
        MyLog.d(TAG, "hide");
        cancelCountDownTask();
        mDescTv.clearAnimation();
        mRrlProgress.stopCountDown();
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,0.0f,Animation.RELATIVE_TO_SELF,1.0f,
                Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0);
        animation.setDuration(200);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setInterpolator(new OvershootInterpolator());
        animation.setFillAfter(true);
        startAnimation(animation);

        mIvLightOff.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIvLightOff.setVisibility(GONE);
                mGrabContainer.setVisibility(GONE);
            }
        }, 200);
    }

    /**
     * 开始演唱
     */
    public void toSingState() {
        MyLog.d(TAG, "toSingState");
        setVisibility(VISIBLE);
        mStatus = STATUS_LIGHT_OFF;
        mIvLightOff.setVisibility(VISIBLE);
        mGrabContainer.setVisibility(GONE);
        mIvLightOff.setClickable(false);

        cancelCountDownTask();
        mCountDownTask = HandlerTaskTimer.newBuilder().interval(1000)
                .take(5)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        int num1 = 6 - integer;
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
                        mIvLightOff.setImageDrawable(U.getDrawable(R.drawable.mie_zi));
                    }
                });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mDescTv.clearAnimation();
        cancelCountDownTask();
    }

    private void cancelCountDownTask(){
        if(mCountDownTask != null){
            mCountDownTask.dispose();
        }
    }

    public interface Listener {
        void clickGrabBtn();

        void clickLightOff();

        void countDownOver();
    }
}
