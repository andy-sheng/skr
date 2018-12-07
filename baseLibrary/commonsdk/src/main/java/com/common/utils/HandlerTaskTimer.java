package com.common.utils;

import android.os.Handler;
import android.os.Message;

import io.reactivex.Observer;

/**
 * Handler 定时器 用于倒计时 、延迟执行、循环执行 任务
 */
public final class HandlerTaskTimer {
    public static final int MSG_EXECUTE = 100;
    long initialDelay = 0;
    // 发送的间隔 -1为不循环发送
    long inerval = -1;
    // 最多发送几次，-1为无限
    int take = -1;
    int times = 0;
    boolean hasCancel = false;
    Observer<Integer> consumer;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_EXECUTE:
                    times++;
                    if (consumer != null) {
                        consumer.onNext(times);
                    }
                    // 如果已经取消了
                    if (hasCancel) {
                        if (consumer != null) {
                            consumer.onComplete();
                        }
                        return;
                    }
                    if (take >= 0) {
                        // 有次数限制
                        if (times >= take) {
                            // 达到次数限制了
                            if (consumer != null) {
                                consumer.onComplete();
                            }
                        } else {
                            // 还能继续发送
                            if (inerval > 0) {
                                // 有时间间隔,继续发
                                mHandler.sendEmptyMessageDelayed(MSG_EXECUTE, inerval);
                            } else {
                                // 没有设置时间间隔
                                consumer.onComplete();
                            }
                        }
                    } else {
                        // 没有次数限制
                        // 还能继续发送
                        if (inerval > 0) {
                            // 有时间间隔,继续发
                            mHandler.sendEmptyMessageDelayed(MSG_EXECUTE, inerval);
                        } else {
                            // 没有设置时间间隔
                            consumer.onComplete();
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
            throw new IllegalStateException("HandlerTaskTimer can not subscibe after dispose");
        }
        mHandler.removeMessages(MSG_EXECUTE);
        mHandler.sendEmptyMessageDelayed(MSG_EXECUTE, initialDelay);
    }

    public void dispose() {
        hasCancel = true;
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        consumer = null;
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

        public HandlerTaskTimer start(Observer<Integer> consumer) {
            mHandlerTaskTimer.subscribe(consumer);
            return mHandlerTaskTimer;
        }
    }
}
