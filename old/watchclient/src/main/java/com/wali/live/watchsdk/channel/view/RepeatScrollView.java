package com.wali.live.watchsdk.channel.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.base.log.MyLog;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by lan on 16/9/8.
 *
 * @module 频道
 * @description item持续向上滚动动画
 */
public class RepeatScrollView extends RelativeLayout {
    private static final String TAG = RepeatScrollView.class.getSimpleName();

    private int mChildLayoutId;

    private ViewGroup mChildView1;
    private ViewGroup mChildView2;

    private int mTranslationY = 0;

    private IScrollListener mListener;
    private int mIndex;

    private ValueAnimator mAnimator;
    private Subscription mTimerSubscription;

    private boolean mNeedScroll = false;
    private boolean mIsOnWindow;

    public RepeatScrollView(Context context) {
        super(context);
    }

    public RepeatScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListener(IScrollListener listener) {
        mListener = listener;
    }

    public int getIndex() {
        return mIndex;
    }

    public RepeatScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(int childLayoutId, int translationY) {
        setChildLayoutId(childLayoutId);
        setTranslationY(translationY);
    }

    private void setChildLayoutId(int childLayoutId) {
        mChildLayoutId = childLayoutId;

        mChildView1 = addChildView();
        mChildView2 = addChildView();
    }

    private void setTranslationY(int translationY) {
        mTranslationY = translationY;

        mChildView2.setTranslationY(mTranslationY);
    }

    public ViewGroup getChildView(int index) {
        if (index == 0) {
            return mChildView1;
        } else {
            return mChildView2;
        }
    }

    private ViewGroup addChildView() {
        if (mChildLayoutId == 0) {
            return null;
        }
        ViewGroup view = (ViewGroup) inflate(getContext(), mChildLayoutId, null);
        addView(view);
        return view;
    }

    private void startTimer() {
        if (mTimerSubscription != null && !mTimerSubscription.isUnsubscribed()) {
            return;
        }
        MyLog.d(TAG, "startTimer");
        mTimerSubscription = Observable.interval(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        MyLog.d(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, "onError " + e);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (mAnimator != null && !mAnimator.isRunning()) {
                            mAnimator.start();
                        }
                    }
                });
    }

    public void stopTimer() {
        MyLog.d(TAG, "stopTimer");
        if (mTimerSubscription != null) {
            mTimerSubscription.unsubscribe();
            mTimerSubscription = null;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        MyLog.e(TAG, "onAttachedToWindow");
        mIsOnWindow = true;
        if (mNeedScroll) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MyLog.e(TAG, "onDetachedFromWindow");
        mIsOnWindow = false;
        if (mNeedScroll) {
            unregisterEventBus();
            stopTimer();
            stopAnimator();
        }
    }

    private void initAnimator() {
        if (mAnimator == null) {
            mAnimator = ValueAnimator.ofFloat(0, 1);
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.addUpdateListener(
                    new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            if (mChildView1 != null && mChildView2 != null) {
                                mChildView1.setTranslationY(-mTranslationY * (float) animation.getAnimatedValue());
                                mChildView2.setTranslationY(mTranslationY * (1 - (float) animation.getAnimatedValue()));
                            }
                        }
                    });
            mAnimator.setDuration(600);
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mChildView1 != null && mChildView2 != null) {
                        mChildView1.setTranslationY(0);
                        mChildView2.setTranslationY(mTranslationY);
                        if (mListener != null) {
                            mIndex++;
                            mListener.onIndexChanged(mIndex);
                        }
                    }
                }
            });
        }
    }

    public void stopAnimator() {
        if (mAnimator != null) {
            mAnimator.end();
            mAnimator = null;
        }
    }

    private void unregisterEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void enterSingleMode() {
        mNeedScroll = false;
        mIndex = 0;

        mChildView1.setVisibility(View.VISIBLE);
        mChildView2.setVisibility(View.GONE);

        stopTimer();
        stopAnimator();
        unregisterEventBus();

        if (mListener != null) {
            mListener.onFirstIndexed();
        }
    }

    public void enterScrollMode() {
        mNeedScroll = true;
        mIndex = 0;

        mChildView1.setVisibility(View.VISIBLE);
        mChildView2.setVisibility(View.VISIBLE);
        mChildView1.setTranslationY(0);
        mChildView2.setTranslationY(mTranslationY);

        initAnimator();
        startTimer();

        if (mListener != null) {
            mListener.onIndexChanged(0);
        }
    }

    public void enterNullMode() {
        mNeedScroll = false;
        mIndex = 0;

        mChildView1.setVisibility(View.GONE);
        mChildView2.setVisibility(View.GONE);

        stopTimer();
        stopAnimator();
        unregisterEventBus();
    }
}
