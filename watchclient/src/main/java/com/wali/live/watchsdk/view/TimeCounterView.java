package com.wali.live.watchsdk.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.MainThread;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.assist.IBindActivityLIfeCycle;
import com.base.image.fresco.BaseImageView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.contest.media.ContestMediaHelper;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by jiyangli on 16-11-30.
 */
public class TimeCounterView extends RelativeLayout implements View.OnTouchListener, IBindActivityLIfeCycle {
    private static final String TAG = "SupportWidgetView";
    private static final int VISIBLE_TIME = 10;//当剩余时间小于等于该值时，显示本控件，单位：秒
    private Timer timer;
    private Context mContext;
    private BaseImageView imgSupportView;
    private TimeCounterCircle timerCircle;
    private volatile int currentLeftTime;// 当前剩余时间
    private static final int TIME_SPEED = 1000;
    private TextView txtTime;// 剩余时间
    private ImageView mTimeOutIv;
    private boolean isCountDowning = false;// 是否正在倒计时
    Animation animation;
    Animation animation2;
    Animation animation3;
    ValueAnimator animator;
    private CountDownTimer timerTask = new CountDownTimer();
    Subscription subTimer;
    boolean isInitial = false;

    private FinishCallBack mFinishCallBack;
    private ContestMediaHelper mMediaHelper;

    public TimeCounterView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.time_counter_view, this);
        imgSupportView = (BaseImageView) view.findViewById(R.id.support_widget_view_imgSupport);
        timerCircle = (TimeCounterCircle) view.findViewById(R.id.support_widget_view_timer);
        txtTime = (TextView) view.findViewById(R.id.support_widget_view_txtWaitingTime);
        mTimeOutIv = (ImageView) view.findViewById(R.id.support_widget_view_txtTimeEnd);

        setOnTouchListener(this);
        animation = AnimationUtils.loadAnimation(mContext, R.anim.scale_1_to_15_to_1);
        animation2 = AnimationUtils.loadAnimation(mContext, R.anim.alpha_1_to0_to1);
        animation3 = AnimationUtils.loadAnimation(mContext, R.anim.scale_0_to_1);

        mMediaHelper = new ContestMediaHelper(mContext);
    }

    public TimeCounterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public TimeCounterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    @MainThread
    public void showWaiting() {
        mTimeOutIv.setVisibility(GONE);

        isInitial = true;

        txtTime.setVisibility(VISIBLE);

        start();

    }

    /**
     * 倒计时已完成
     */
    private void showSupport() {
        txtTime.setVisibility(GONE);
        mTimeOutIv.setVisibility(VISIBLE);
        mTimeOutIv.startAnimation(animation3);
        animation3.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mFinishCallBack != null) {
                    mFinishCallBack.onFinish();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void start() {

        if (timerCircle == null || txtTime == null) {
            return;
        }
        int smoothness = 360;// 进度变化平滑度，值越大越平滑
        timerCircle.setMax(smoothness);
        timerCircle.setProgress(0);

        txtTime.setText(String.valueOf(VISIBLE_TIME));
        txtTime.setVisibility(VISIBLE);

        if (animator != null) {
            animator.removeAllUpdateListeners();
            animator.cancel();
            animator = null;
        }

        animator = ValueAnimator.ofInt(0, smoothness);
        animator.setDuration(VISIBLE_TIME * 1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (Integer) animation.getAnimatedValue();
                timerCircle.setProgress(animatedValue);
            }
        });
        animator.start();

        currentLeftTime = VISIBLE_TIME;
        isCountDowning = true;

        if (subTimer != null) {
            subTimer.unsubscribe();
            subTimer = null;
        }

        mMediaHelper.playRawSource(R.raw.contest_begin_tip);
        subTimer = Observable
                .interval(1, TimeUnit.SECONDS)
                .take(currentLeftTime + 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long l) {
                        long index = currentLeftTime - l - 1;
                        if (index > 0) {
                            txtTime.setText(index + "");
                            if (index <= VISIBLE_TIME) {
                                TimeCounterView.this.setVisibility(VISIBLE);
                            }

                            if (index <= 5) {
                                txtTime.clearAnimation();
                                txtTime.setTextColor(Color.parseColor("#f9a825"));
                                txtTime.startAnimation(animation);
                                imgSupportView.clearAnimation();
                                imgSupportView.setVisibility(VISIBLE);
                                imgSupportView.startAnimation(animation2);
                            }

                            if (index == 3) {
                                mMediaHelper.playRawSource(R.raw.contest_countdown_3);
                            }
                        } else {
                            showSupport();
                            stop();
                        }
                    }
                });

    }

    /**
     * 停止更新环形进度条和倒计时数字
     */
    public void stop() {
        txtTime.setTextColor(Color.BLACK);
        imgSupportView.setVisibility(GONE);
        isCountDowning = false;
        txtTime.setText("");
        if (timerCircle != null) {
            timerCircle.setProgress(0);
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
            timerTask = new CountDownTimer();
        }
        if (animator != null) {
            animator.cancel();
            animator.removeAllUpdateListeners();
            animator = null;
        }
        if (subTimer != null) {
            subTimer.unsubscribe();
            subTimer = null;
        }
    }

    public boolean isCountDowning() {
        return isCountDowning;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return false;
    }

    @Override
    public void onActivityDestroy() {
        destroy();
    }

    @Override
    public void onActivityCreate() {

    }

    private class CountDownTimer extends TimerTask {
        @Override
        public void run() {
            Message msg = handler.obtainMessage(currentLeftTime);
            handler.sendMessage(msg);
            currentLeftTime--;
            if (currentLeftTime < 0) {
                stop();
            }
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what > 0) {
                txtTime.setText(msg.what + "s");
                if (msg.what <= VISIBLE_TIME && animator != null && !animator.isStarted()) {
                    TimeCounterView.this.setVisibility(VISIBLE);
                    animator.start();
                }
            } else {
                showSupport();
            }
        }
    };

    /**
     * 显示水波扩散的动画
     */
    private void startAnim() {
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }

    private void destroy() {
        handler.removeCallbacksAndMessages(null);
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }

        if (animator != null) {
            animator.removeAllUpdateListeners();
            animator.cancel();
            animator = null;
        }

        if (subTimer != null) {
            subTimer.unsubscribe();
            subTimer = null;
        }

        if (mMediaHelper != null) {
            mMediaHelper.destroy();
        }
        isInitial = false;
    }

    public boolean hasInitial() {
        return isInitial;
    }

    public void cleanInitial() {
        isInitial = false;
    }

    public void setOnFinishListener(FinishCallBack callBack) {
        this.mFinishCallBack = callBack;
    }

    public interface FinishCallBack {
        public void onFinish();
    }
}
