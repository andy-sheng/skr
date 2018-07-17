package view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.annotation.MainThread;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.log.MyLog;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.view.TimeCounterCircle;

import media.ContestMediaHelper;

/**
 * Created by jiyangli on 16-11-30.
 */
public class CountDownView extends RelativeLayout {
    private static final String TAG = "CountDownView";

    public final int COUNT_DOWN_TOTAL_NUM = 10 * 1000;//答题倒计时总时长
    public final int COUNT_DOWN_INTERVAL = 1 * 1000;//倒计时间隔

    private TextView mCountDownLeftTimeTv;// 剩余时间
    private ImageView mTimeOutIv;//超时后图标
    private TimeCounterCircle mTimerCircle;//倒计时动画的圈

    private FinishCallBack mFinishCallBack;
    private ContestMediaHelper mMediaHelper;

    private Animation animation;
    private Animation animation3;
    private ValueAnimator animator;

    private CountDownTimer mCountDownTimer = new CountDownTimer(COUNT_DOWN_TOTAL_NUM, COUNT_DOWN_INTERVAL) {
        @Override
        public void onTick(long millisUntilFinished) {
            MyLog.w(TAG, "countDownNow mills = " + millisUntilFinished);
            long index = millisUntilFinished / 1000;
            if (index > 0) {
                mCountDownLeftTimeTv.setText(String.valueOf(index));
            }
            if (index <= 3) {
                mCountDownLeftTimeTv.clearAnimation();
                mCountDownLeftTimeTv.setTextColor(getResources().getColor(R.color.color_ff2966));
                mCountDownLeftTimeTv.startAnimation(animation);
            }

            if (index == 3) {
                mMediaHelper.playRawSource(R.raw.contest_countdown_3);
            }
        }

        @Override
        public void onFinish() {
            MyLog.w(TAG, "timer finish");
            if (mFinishCallBack != null) {
                mFinishCallBack.onFinish();
            }
            showSupport();
            stop();
        }
    };

    public CountDownView(Context context) {
        super(context);
        init(context);
    }

    public CountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CountDownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.time_counter_view, this);
        mTimerCircle = (TimeCounterCircle) view.findViewById(R.id.count_down_anim_circle);
        mCountDownLeftTimeTv = (TextView) view.findViewById(R.id.count_down_left_time_tv);
        mTimeOutIv = (ImageView) view.findViewById(R.id.time_end_iv);

        animation = AnimationUtils.loadAnimation(context, R.anim.scale_1_to_15_to_1);
        animation3 = AnimationUtils.loadAnimation(context, R.anim.scale_0_to_1);

        mMediaHelper = new ContestMediaHelper(context);
    }

    @MainThread
    public void showWaiting() {
        mTimeOutIv.setVisibility(GONE);
        mCountDownLeftTimeTv.setVisibility(VISIBLE);

        start();
    }

    /**
     * 倒计时已完成
     */
    private void showSupport() {
        mCountDownLeftTimeTv.setVisibility(GONE);
        mTimeOutIv.setVisibility(VISIBLE);
        mTimeOutIv.startAnimation(animation3);
        animation3.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void start() {
        if (mTimerCircle == null || mCountDownLeftTimeTv == null) {
            return;
        }

        int smoothness = 360;// 进度变化平滑度，值越大越平滑
        mTimerCircle.setMax(smoothness);
        mTimerCircle.setProgress(0);

        if (animator != null) {
            animator.removeAllUpdateListeners();
            animator.cancel();
            animator = null;
        }

        animator = ValueAnimator.ofInt(0, smoothness);
        animator.setDuration(COUNT_DOWN_TOTAL_NUM);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (Integer) animation.getAnimatedValue();
                mTimerCircle.setProgress(animatedValue);
            }
        });
        animator.start();

        mCountDownLeftTimeTv.setText(String.valueOf(COUNT_DOWN_TOTAL_NUM / 1000));
        mCountDownTimer.start();
    }

    /**
     * 停止更新环形进度条和倒计时数字
     */
    public void stop() {
        mCountDownLeftTimeTv.setTextColor(Color.BLACK);
        mCountDownLeftTimeTv.setText("");
        if (mTimerCircle != null) {
            mTimerCircle.setProgress(0);
        }
        if (animator != null) {
            animator.cancel();
            animator.removeAllUpdateListeners();
            animator = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }

    private void destroy() {
        mCountDownTimer.cancel();
        if (animator != null) {
            animator.removeAllUpdateListeners();
            animator.cancel();
            animator = null;
        }

        if (mMediaHelper != null) {
            mMediaHelper.destroy();
        }
    }

    public void setOnFinishListener(FinishCallBack callBack) {
        this.mFinishCallBack = callBack;
    }

    public interface FinishCallBack {
        public void onFinish();
    }
}
