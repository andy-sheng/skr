package com.wali.live.common.gift.utils;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.trello.rxlifecycle.ActivityEvent;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 循环播放动画时的控制模板类，封装点亮、背景、大礼物等播放的基本队列逻辑
 * Created by chengsimin on 16/6/17.
 */
public abstract class AnimationPlayControlTemplate<T> {
    public static final String TAG = "AnimationPlayControlTemplate";
    public static final int SIZE = 100;
    private RxActivity rxActivity;
    boolean mOverByTimer = true;

    public AnimationPlayControlTemplate(RxActivity rxActivity, boolean overByTimer) {
        this.rxActivity = rxActivity;
        this.mOverByTimer = overByTimer;
    }

    public AnimationPlayControlTemplate(RxActivity rxActivity, boolean overByTimer, int maxConsumerNumber) {
        this.rxActivity = rxActivity;
        this.mOverByTimer = overByTimer;
        this.maxConsumerNumber = maxConsumerNumber;
    }

    private ExecutorService mSingleThread = new ThreadPoolExecutor(1, 1, 15L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(10),
            new ThreadFactory() {
                int count = 0;

                @Override
                public Thread newThread(Runnable r) {
                    count++;
                    Thread thread = new Thread(r, "gift-queue-pool-" + count);
                    thread.setPriority((Thread.NORM_PRIORITY) / 2);
                    return thread;
                }
            },
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
//                    MyLog.w(TAG, "rejectedExecution discard runnable");
                }
            });

    int maxConsumerNumber = 1;

    int mCurConsumerNumber = 0;
    /**
     * 播放动画队列
     */
    private LinkedList<T> mQueue = new LinkedList();

    public void add(T model, boolean must) {
        synchronized (mQueue) {
            if (mQueue.size() < SIZE || must) {
                mQueue.offer(model);
            }
        }
        play();
    }

    private void play() {
        if (mCurConsumerNumber >= maxConsumerNumber) {
            return;
        }
        Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                if (mCurConsumerNumber >= maxConsumerNumber) {
                    subscriber.onCompleted();
                    return;
                }
                T mCur = null;
                synchronized (mQueue) {
                    mCur = mQueue.poll();
                }
                if (mCur == null) {
                    subscriber.onCompleted();
                    return;
                }
                processInBackGround(mCur);
                subscriber.onNext(mCur);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.from(mSingleThread))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(rxActivity.<T>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Observer<T>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d("AnimationPlayControlTemplate", e);
                    }

                    @Override
                    public void onNext(T model) {
                        if (model != null) {
                            onStartInside(model);
                        }
                    }
                });
    }

    private Subscription mEndDelaySubscription;

    private void onStartInside(final T model) {
        MyLog.d(TAG, "onStartInside model:" + model);
        if (++mCurConsumerNumber > maxConsumerNumber) {
            mCurConsumerNumber = maxConsumerNumber;
            synchronized (mQueue) {
                mQueue.offerFirst(model);
            }
            return;
        }
        onStart(model);
        // 定时结束的模式
        if (mOverByTimer) {
            // 自动延迟结束
            if (mEndDelayTime > 0) {
                if (mEndDelaySubscription != null && !mEndDelaySubscription.isUnsubscribed()) {
                    return;
                }
                mEndDelaySubscription = Observable.timer(mEndDelayTime, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .compose(rxActivity.bindUntilEvent(ActivityEvent.DESTROY))
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                onEndInSide(model);
                            }
                        });
            } else {
                onEndInSide(model);
            }
        }
    }

    private void onEndInSide(T model) {
        MyLog.d(TAG, "onEndInSide model:" + model);
        mEndDelayTime = 0;
        onEnd(model);
        if (--mCurConsumerNumber < 0) {
            mCurConsumerNumber = 0;
        }
        play();
    }

    /**
     * 复位
     */
    public synchronized void reset() {
        mCurConsumerNumber = 0;
        mQueue.clear();
    }

    /**
     * 复位
     */
    public synchronized void destroy() {
        mCurConsumerNumber = 0;
        mQueue.clear();
        if (mSingleThread != null) {
            mSingleThread.shutdown();
        }
    }

    // 手动结束
    public void endCurrent(T model) {
        if (mEndDelaySubscription != null) {
            mEndDelaySubscription.unsubscribe();
        }
        onEndInSide(model);
    }

    private long mEndDelayTime;

    /**
     * 动画延迟结束的时间
     *
     * @param ts
     */
    public void setEndDelayTime(long ts) {
        this.mEndDelayTime = ts;
    }

    /**
     * 某次动画开始时执行
     *
     * @param model
     */
    public abstract void onStart(T model);

    /**
     * 某次动画结束了执行
     *
     * @param model
     */
    protected abstract void onEnd(T model);

    public synchronized boolean hasMore() {
        return !mQueue.isEmpty();
    }

    public boolean isFull() {
        return mCurConsumerNumber >= maxConsumerNumber;
    }

    protected void processInBackGround(T model) {
    }

}
