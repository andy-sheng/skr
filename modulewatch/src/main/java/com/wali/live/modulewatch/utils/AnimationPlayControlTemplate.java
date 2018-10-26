package com.wali.live.modulewatch.utils;

import com.common.base.BaseActivity;
import com.common.log.MyLog;
import com.trello.rxlifecycle2.android.ActivityEvent;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 循环播放动画时的控制模板类，封装点亮、背景、大礼物等播放的基本队列逻辑
 * Created by chengsimin on 16/6/17.
 */
public abstract class AnimationPlayControlTemplate<T> {
    public static final String TAG = "AnimationPlayControlTemplate";
    public static final int SIZE = 100;
    private WeakReference<BaseActivity> baseActivity;
    private boolean mOverByTimer = true;

    public AnimationPlayControlTemplate(BaseActivity baseActivity, boolean overByTimer) {
        this.baseActivity = new WeakReference<>(baseActivity);
        this.mOverByTimer = overByTimer;
    }

    public AnimationPlayControlTemplate(BaseActivity baseActivity, boolean overByTimer, int maxConsumerNumber) {
        this.baseActivity = new WeakReference<>(baseActivity);
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
                    MyLog.w(TAG, "rejectedExecution discard runnable");
                }
            });

    private int maxConsumerNumber = 1;

    private int mCurConsumerNumber = 0;
    /**
     * 播放动画队列
     */
    private LinkedList<T> mQueue = new LinkedList<>();

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

        Observable observable = Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<T> emitter) throws Exception {
                if (mCurConsumerNumber >= maxConsumerNumber) {
                    emitter.onComplete();
                    return;
                }
                T mCur = null;
                synchronized (mQueue) {
                    mCur = mQueue.poll();
                }
                if (mCur == null) {
                    emitter.onComplete();
                    return;
                }
                processInBackGround(mCur);
                emitter.onNext(mCur);
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.from(mSingleThread))
                .observeOn(AndroidSchedulers.mainThread());

        if (baseActivity != null && baseActivity.get() != null && !baseActivity.get().isFinishing()) {
            observable.compose(baseActivity.get().bindUntilEvent(ActivityEvent.DESTROY));
        } else {
            return;
        }
        observable.subscribe(new Observer<T>() {
            @Override
            public void onError(Throwable e) {
                MyLog.d(TAG, e);
            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(T model) {
                if (model != null) {
                    onStartInside(model);
                }
            }
        });
    }

    private Disposable mEndDelayDisposable;

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
                if (mEndDelayDisposable != null) {
                    return;
                }

                Observable observable = Observable.timer(mEndDelayTime, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread());

                if (baseActivity != null && baseActivity.get() != null && !baseActivity.get().isFinishing()) {
                    observable.compose(baseActivity.get().bindUntilEvent(ActivityEvent.DESTROY));
                } else {
                    return;
                }
                observable.subscribe(new Observer() {

                    @Override
                    public void onSubscribe(Disposable d) {
                        mEndDelayDisposable = d;
                    }

                    @Override
                    public void onNext(Object o) {
                        onEndInSide(model);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

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
        mEndDelayDisposable.dispose();
        mEndDelayDisposable = null;
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
            mSingleThread.shutdownNow();
            mSingleThread = null;
        }
        if (baseActivity != null) {
            baseActivity.clear();
            baseActivity = null;
        }
    }

    // 手动结束
    public void endCurrent(T model) {
        if (mEndDelayDisposable != null) {
            mEndDelayDisposable.dispose();
            mEndDelayDisposable = null;
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
