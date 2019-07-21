package com.common.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.log.MyLog;
import com.common.mvp.PresenterEvent;
import com.common.mvp.RxLifeCyclePresenter;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.android.FragmentEvent;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Handler 定时器 用于倒计时 、延迟执行、循环执行 任务
 */
public final class HandlerTaskTimer {
    public final String TAG = "HandlerTaskTimer";

    public static final int MSG_EXECUTE = 100;
    long initialDelay = 0;
    // 发送的间隔 -1为不循环发送
    long inerval = -1;
    // 最多发送几次，-1为无限
    int take = -1;
    int times = 0;
    boolean hasCancel = false;
    Observer<Integer> consumer;

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_EXECUTE:
                    times++;
                    if (consumer != null) {
                        try {
                            consumer.onNext(times);
                        } catch (Exception e) {
                            MyLog.e("HandlerTaskTimer", e);
                        }
                    }
                    // 如果已经取消了
                    if (hasCancel) {
                        if (consumer != null) {
                            try {
                                consumer.onComplete();
                            } catch (Exception e) {
                                MyLog.e("HandlerTaskTimer", e);
                            }
                        }
                        return;
                    }
                    if (take >= 0) {
                        // 有次数限制
                        if (times >= take) {
                            // 达到次数限制了
                            if (consumer != null) {
                                try {
                                    consumer.onComplete();
                                } catch (Exception e) {
                                    MyLog.e("HandlerTaskTimer", e);
                                }
                            }
                        } else {
                            // 还能继续发送
                            if (inerval > 0) {
                                // 有时间间隔,继续发
                                mHandler.removeMessages(MSG_EXECUTE);
                                mHandler.sendEmptyMessageDelayed(MSG_EXECUTE, inerval);
                            } else {
                                // 没有设置时间间隔
                                try {
                                    consumer.onComplete();
                                } catch (Exception e) {
                                    MyLog.e("HandlerTaskTimer", e);
                                }
                            }
                        }
                    } else {
                        // 没有次数限制
                        // 还能继续发送
                        if (inerval > 0) {
                            // 有时间间隔,继续发
                            mHandler.removeMessages(MSG_EXECUTE);
                            mHandler.sendEmptyMessageDelayed(MSG_EXECUTE, inerval);
                        } else {
                            // 没有设置时间间隔
                            try {
                                consumer.onComplete();
                            } catch (Exception e) {
                                MyLog.e("HandlerTaskTimer", e);
                            }
                        }
                    }
                    break;
            }
        }
    };

    private HandlerTaskTimer() {

    }

    public void subscribe(Observer<Integer> consumer) {
        this.consumer = consumer;
        if (mHandler == null) {
            MyLog.d(TAG, "mHandler == null");
            return;
//            throw new IllegalStateException("HandlerTaskTimer can not subscibe after dispose");
        }
        mHandler.removeMessages(MSG_EXECUTE);
        mHandler.sendEmptyMessageDelayed(MSG_EXECUTE, initialDelay);
    }

    public void dispose() {
        hasCancel = true;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        consumer = null;
    }

    public boolean isDisposed() {
        return hasCancel;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        HandlerTaskTimer mHandlerTaskTimer;

        Builder() {
            mHandlerTaskTimer = new HandlerTaskTimer();
        }

        public Builder delay(long initialDelay) {
            mHandlerTaskTimer.initialDelay = initialDelay;
            return this;
        }

        public Builder interval(long inerval) {
            mHandlerTaskTimer.inerval = inerval;
            return this;
        }

        public Builder take(int take) {
            mHandlerTaskTimer.take = take;
            return this;
        }

        public Builder compose(BaseActivity baseActivity) {
            baseActivity.provideLifecycleSubject().subscribe(new Consumer<ActivityEvent>() {
                @Override
                public void accept(ActivityEvent activityEvent) throws Exception {
                    if (activityEvent == ActivityEvent.DESTROY) {
                        mHandlerTaskTimer.dispose();
                    }
                }
            });
            return this;
        }

        public Builder compose(BaseFragment baseFragment) {
            baseFragment.provideLifecycleSubject().subscribe(new Consumer<FragmentEvent>() {
                @Override
                public void accept(FragmentEvent fragmentEvent) throws Exception {
                    if (fragmentEvent == FragmentEvent.DESTROY) {
                        mHandlerTaskTimer.dispose();
                    }
                }
            });
            return this;
        }

        public Builder compose(RxLifeCyclePresenter presenter) {
            presenter.provideLifecycleSubject().subscribe(new Consumer<PresenterEvent>() {
                @Override
                public void accept(PresenterEvent fragmentEvent) throws Exception {
                    if (fragmentEvent == PresenterEvent.DESTROY) {
                        mHandlerTaskTimer.dispose();
                    }
                }
            });
            return this;
        }

        public HandlerTaskTimer start(Observer<Integer> consumer) {
            mHandlerTaskTimer.subscribe(consumer);
            return mHandlerTaskTimer;
        }
    }

    public static abstract class ObserverW implements Observer<Integer> {

        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onComplete() {

        }
    }
}
