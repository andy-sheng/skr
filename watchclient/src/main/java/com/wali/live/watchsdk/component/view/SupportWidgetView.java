package com.wali.live.watchsdk.component.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.MainThread;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;

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
public class SupportWidgetView extends FrameLayout implements View.OnTouchListener {
    private static final String TAG = "SupportWidgetView";
    private static final int VISIBLE_TIME = 60; // 当剩余时间小于等于该值时，显示本控件，单位：秒

    private Timer timer;

    private BaseImageView imgSupportView;
    private TimingCircleView timerCircle;
    private int totalTime;
    private volatile int currentLeftTime;// 当前剩余时间
    private String waitingPic;
    private String supportPic;
    private TextView txtTime;// 剩余时间
    private ImageView imgAnim;// 水波纹1
    private ImageView imgAnim2;// 水波纹2
    private ImageView imgAnim3;// 水波纹3
    private boolean isCountDowning = false;// 是否正在倒计时

    private Animation mAnimation;
    private Animation mAnimation2;
    private Animation mAnimation3;
    private ValueAnimator mAnimator;

    private CountDownTimer timerTask = new CountDownTimer();
    private Subscription subTimer;
    private boolean isInitial = false;

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

        imgSupportView = (BaseImageView) findViewById(R.id.support_widget_view_imgSupport);
        timerCircle = (TimingCircleView) findViewById(R.id.support_widget_view_timer);
        txtTime = (TextView) findViewById(R.id.support_widget_view_txtWaitingTime);
        imgAnim = (ImageView) findViewById(R.id.support_widget_view_imgAnim);
        imgAnim2 = (ImageView) findViewById(R.id.support_widget_view_imgAnim2);
        imgAnim3 = (ImageView) findViewById(R.id.support_widget_view_imgAnim3);

        mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.count_down_press);
        mAnimation2 = AnimationUtils.loadAnimation(getContext(), R.anim.count_down_press_2);
        mAnimation3 = AnimationUtils.loadAnimation(getContext(), R.anim.count_down_press_3);
    }

    public void setTotalTime(int timeSecond) {
        totalTime = timeSecond;
    }

    public void setPic(String waitingPic, String supportPic) {
        this.waitingPic = waitingPic;
        this.supportPic = supportPic;
    }

    @MainThread
    public void showWaiting() {
        isInitial = true;

        setVisibility(GONE);
        if (imgSupportView != null) {
            AvatarUtils.loadCoverByUrl(imgSupportView, waitingPic, false, 0, imgSupportView.getWidth(), imgSupportView.getHeight());
        }
        start();
        stopAnim();
    }

    /**
     * 倒计时已完成
     */
    private void showSupport() {
        txtTime.setVisibility(GONE);
        if (null == supportPic || supportPic.equals("")) {
            MyLog.e(TAG, "supportPic is null");
            return;
        }
        if (imgSupportView != null) {
            AvatarUtils.loadCoverByUrl(imgSupportView, supportPic, false, 0, imgSupportView.getWidth(), imgSupportView.getHeight());
        }
        startAnim();
    }

    private void start() {
        if (totalTime <= 0) {
            MyLog.e(TAG, "waiting time is under 0");
            return;
        }

        if (timerCircle == null || txtTime == null) {
            return;
        }
        int smoothness = 360;// 进度变化平滑度，值越大越平滑
        int visibleInitCountDownSecond = totalTime > VISIBLE_TIME ? VISIBLE_TIME : totalTime;// 当该控件可见时剩余的秒数
        timerCircle.setMax(smoothness);
        timerCircle.setProgress(0);
        txtTime.setText(String.valueOf(visibleInitCountDownSecond));
        txtTime.setVisibility(VISIBLE);

        if (mAnimator != null) {
            mAnimator.removeAllUpdateListeners();
            mAnimator.cancel();
            mAnimator = null;
        }

        //这里用的是nineold的属性动画向下兼容包
        mAnimator = ValueAnimator.ofInt(0, smoothness);
        mAnimator.setDuration(visibleInitCountDownSecond * 1000);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (Integer) animation.getAnimatedValue();
                timerCircle.setProgress(animatedValue);
            }
        });
        //mAnimator.start();

        currentLeftTime = totalTime;
        isCountDowning = true;

        if (subTimer != null && !subTimer.isUnsubscribed()) {
            subTimer.unsubscribe();
            subTimer = null;
        }
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
                        long index = currentLeftTime - l;
                        if (index > 0) {
                            txtTime.setText(index + "s");
                            if (index <= VISIBLE_TIME && mAnimator != null && !mAnimator.isStarted()) {
                                SupportWidgetView.this.setVisibility(VISIBLE);
                                mAnimator.start();
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
        isCountDowning = false;
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
        if (mAnimator != null) {
            mAnimator.cancel();
            mAnimator.removeAllUpdateListeners();
            mAnimator = null;
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
                if (msg.what <= VISIBLE_TIME && mAnimator != null && !mAnimator.isStarted()) {
                    SupportWidgetView.this.setVisibility(VISIBLE);
                    mAnimator.start();
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
        imgAnim.setVisibility(VISIBLE);
        imgAnim2.setVisibility(VISIBLE);
        imgAnim3.setVisibility(VISIBLE);

        imgAnim.startAnimation(mAnimation);
        imgAnim2.postDelayed(runnable1, 800);
        imgAnim3.postDelayed(runnable2, 1600);
    }

    private Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            if (imgAnim2.getVisibility() == VISIBLE)
                imgAnim2.startAnimation(mAnimation2);
        }
    };

    private Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            if (imgAnim3.getVisibility() == VISIBLE)
                imgAnim3.startAnimation(mAnimation3);
        }
    };

    /**
     * 停止水波扩散效果
     */
    public void stopAnim() {
        imgAnim.setVisibility(GONE);
        imgAnim2.setVisibility(GONE);
        imgAnim3.setVisibility(GONE);

        imgAnim.clearAnimation();
        imgAnim2.clearAnimation();
        imgAnim3.clearAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destory();
    }

    private void destory() {
        imgAnim2.removeCallbacks(runnable1);
        imgAnim3.removeCallbacks(runnable2);
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

        if (mAnimator != null) {
            mAnimator.removeAllUpdateListeners();
            mAnimator.cancel();
            mAnimator = null;
        }

        if (subTimer != null) {
            subTimer.unsubscribe();
            subTimer = null;
        }

        isInitial = false;
    }

    public boolean hasInitial() {
        return isInitial;
    }

    public void cleanInitial() {
        isInitial = false;
    }
}
