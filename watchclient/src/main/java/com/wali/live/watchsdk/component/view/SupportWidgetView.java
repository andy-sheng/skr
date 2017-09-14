package com.wali.live.watchsdk.component.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by lan on 16-11-30.
 */
public class SupportWidgetView extends FrameLayout {
    private static final String TAG = "SupportWidgetView";
    private static final int VISIBLE_TIME = 60; // 当剩余时间小于等于该值时，显示本控件，单位：秒

    private BaseImageView mSupportIv;
    // 倒计时的图，以及倒计时结束显示的图
    private String mWaitingPic;
    private String mSupportPic;

    // 倒计时view
    private TimerCircleView mTimerCircleView;
    private TextView mTimerTv;

    private int mTotalTime;
    private volatile int mCurrentLeftTime;

    // 倒计时后的波纹效果
    private ImageView mAnimIv;
    private ImageView mAnimIv2;
    private ImageView mAnimIv3;

    private Animator mRippleAnimator;
    private Animator mRippleAnimator2;
    private Animator mRippleAnimator3;

    // 是否正在倒计时
    private boolean mIsCountingDown = false;

    private ValueAnimator mCountDownAnimator;
    private Subscription mTimerSubscription;

    private boolean mIsInitial = false;

    public SupportWidgetView(Context context) {
        super(context);
        init();
    }

    public SupportWidgetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SupportWidgetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.support_widget_view, this);

        mSupportIv = (BaseImageView) findViewById(R.id.waiting_iv);

        mTimerCircleView = (TimerCircleView) findViewById(R.id.timer_circle_view);
        mTimerTv = (TextView) findViewById(R.id.timer_tv);

        mAnimIv = (ImageView) findViewById(R.id.anim_iv);
        mAnimIv2 = (ImageView) findViewById(R.id.anim_iv2);
        mAnimIv3 = (ImageView) findViewById(R.id.anim_iv3);

        mRippleAnimator = loadAnimator(mAnimIv, 0);
        mRippleAnimator2 = loadAnimator(mAnimIv2, 800);
        mRippleAnimator3 = loadAnimator(mAnimIv3, 1600);
    }

    private ObjectAnimator loadAnimator(View view, int startDelay) {
        PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.75f);
        PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.75f);
        PropertyValuesHolder holder3 = PropertyValuesHolder.ofFloat("alpha", 1f, 0f);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, holder1, holder2, holder3);
        animator.setDuration(2400);
        animator.setStartDelay(startDelay);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        return animator;
    }

    public void setTotalTime(int timeSecond) {
        mTotalTime = timeSecond;
    }

    public void setPic(String waitingPic, String supportPic) {
        this.mWaitingPic = waitingPic;
        this.mSupportPic = supportPic;
    }

    public void showWaiting() {
        MyLog.d(TAG, "showWaiting");
        mIsInitial = true;
        setVisibility(GONE);
        bindImage(mSupportIv, mWaitingPic);

        start();
        stopRippleAnimator();
    }

    /**
     * 倒计时已完成
     */
    private void showSupport() {
        MyLog.d(TAG, "showSupport");
        mTimerTv.setVisibility(GONE);
        bindImage(mSupportIv, mSupportPic);
        if (TextUtils.isEmpty(mSupportPic)) {
            MyLog.e(TAG, "supportPic is null");
            return;
        }
        startRippleAnimator();
    }

    private void bindImage(BaseImageView iv, String picUrl) {
        if (iv != null) {
            if (!TextUtils.isEmpty(picUrl)) {
                AvatarUtils.loadCoverByUrl(iv, picUrl, false, 0, iv.getWidth(), iv.getHeight());
                iv.setVisibility(View.VISIBLE);
            } else {
                iv.setVisibility(View.GONE);
            }
        }
    }

    private void start() {
        if (mTotalTime <= 0) {
            MyLog.e(TAG, "waiting time is under 0");
            return;
        }

        if (mTimerCircleView == null || mTimerTv == null) {
            return;
        }

        int smoothness = 360;// 进度变化平滑度，值越大越平滑
        int countDownSecond = mTotalTime > VISIBLE_TIME ? VISIBLE_TIME : mTotalTime;// 当该控件可见时剩余的秒数

        mTimerCircleView.setMax(smoothness);
        mTimerCircleView.setProgress(0);
        mTimerTv.setText(String.valueOf(countDownSecond));
        mTimerTv.setVisibility(VISIBLE);

        if (mCountDownAnimator != null) {
            mCountDownAnimator.removeAllUpdateListeners();
            mCountDownAnimator.cancel();
        }

        mCountDownAnimator = ValueAnimator.ofInt(0, smoothness);
        mCountDownAnimator.setDuration(countDownSecond * 1000);
        mCountDownAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (Integer) animation.getAnimatedValue();
                mTimerCircleView.setProgress(animatedValue);
            }
        });

        mCurrentLeftTime = mTotalTime;
        mIsCountingDown = true;

        if (mTimerSubscription != null && !mTimerSubscription.isUnsubscribed()) {
            mTimerSubscription.unsubscribe();
        }
        mTimerSubscription = Observable
                .interval(1, TimeUnit.SECONDS)
                .take(mCurrentLeftTime + 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long l) {
                        long index = mCurrentLeftTime - l;
                        if (index > 0) {
                            mTimerTv.setText(index + "s");

                            if (index <= VISIBLE_TIME && mCountDownAnimator != null && !mCountDownAnimator.isStarted()) {
                                setVisibility(VISIBLE);
                                mCountDownAnimator.start();
                            }
                        } else {
                            showSupport();
                            stopCountdown();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }

    /**
     * 停止更新环形进度条和倒计时数字
     */
    public void stopCountdown() {
        mIsCountingDown = false;
        if (mTimerCircleView != null) {
            mTimerCircleView.setProgress(0);
        }
        stopCountDownAnimator();
        stopTimerSubscription();
    }

    public boolean isCountingDown() {
        return mIsCountingDown;
    }

    /**
     * 显示水波扩散的动画
     */
    private void startRippleAnimator() {
        mAnimIv.setVisibility(VISIBLE);
        mRippleAnimator.start();

        mAnimIv2.setVisibility(VISIBLE);
        mRippleAnimator2.start();

        mAnimIv3.setVisibility(VISIBLE);
        mRippleAnimator3.start();
    }


    public void stopRippleAnimator() {
        mAnimIv.setVisibility(GONE);
        mRippleAnimator.cancel();

        mAnimIv2.setVisibility(GONE);
        mRippleAnimator2.cancel();

        mAnimIv3.setVisibility(GONE);
        mRippleAnimator3.cancel();
    }

    private void stopCountDownAnimator() {
        if (mCountDownAnimator != null) {
            mCountDownAnimator.removeAllUpdateListeners();
            mCountDownAnimator.cancel();
            mCountDownAnimator = null;
        }
    }

    private void stopTimerSubscription() {
        if (mTimerSubscription != null && !mTimerSubscription.isUnsubscribed()) {
            mTimerSubscription.unsubscribe();
            mTimerSubscription = null;
        }
    }

    public void destroy() {
        MyLog.d(TAG, "destroy");
        stopRippleAnimator();
        stopCountDownAnimator();
        stopTimerSubscription();

        mIsInitial = false;
    }

    public boolean hasInitial() {
        return mIsInitial;
    }

    public void cleanInitial() {
        mIsInitial = false;
    }
}
