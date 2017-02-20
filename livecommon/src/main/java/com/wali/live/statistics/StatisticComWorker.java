package com.wali.live.statistics;

import com.base.log.MyLog;

/**
 * Created by qianyuan on 16/3/22.
 */
public class StatisticComWorker {

    private final String TAG = StatisticComWorker.class.getSimpleName();

    private static StatisticComWorker sInstance = null;
    private volatile long mVideoStartRequestTime = 0;
    private volatile long mVideoFristFrameComeback = 0;
    private volatile long delay = 0;

    public synchronized static StatisticComWorker getsInstance() {
        if (null == sInstance) {
            sInstance = new StatisticComWorker();
        }
        return sInstance;
    }

    private StatisticComWorker() {
    }

    public void setVideoRequestTime(long time) {

        mVideoStartRequestTime = time;
    }

    public void setVideoFirstFrameComeback(long time) {
        mVideoFristFrameComeback = time;
    }

    public long getFirstFrameDelay() {
        delay = mVideoFristFrameComeback - mVideoStartRequestTime;
        if (delay <= 0) {
            MyLog.d(TAG, "data is not correct");
            return 0;
        }
        MyLog.d(TAG, "delay time is :" + delay + "ms");
        return delay;
    }

    public long getVideoStartRequestTime() {
        return mVideoStartRequestTime;
    }

}
