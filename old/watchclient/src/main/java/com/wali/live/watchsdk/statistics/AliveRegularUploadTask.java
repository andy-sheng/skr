package com.wali.live.watchsdk.statistics;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.base.log.MyLog;
import com.mi.live.data.account.MyUserInfoManager;
import com.wali.live.watchsdk.statistics.item.AliveStatisticItem;

/**
 * Created by liuting on 18-8-3.
 */

public enum AliveRegularUploadTask {
    sInstance;

    private final static String TAG = AliveRegularUploadTask.class.getSimpleName();
    public static final long REGULAR_TIME = 60 * 1000; // 每隔60s上传一次打点

    private HandlerThread mHandlerThread;
    private Handler mRegularHandler;
    private Runnable mUploadRunnable;

    public void startUpload() {
        MyLog.d(TAG, "create HandlerThread   alive-regular-upload");
        mHandlerThread = new HandlerThread("alive-regular-upload");

        MyLog.d(TAG, "thread start");
        mHandlerThread.start();

        mRegularHandler = new Handler(mHandlerThread.getLooper());

        if (mUploadRunnable == null) {
            mUploadRunnable = getRegularRunnable();
        }

        mRegularHandler.postDelayed(mUploadRunnable, REGULAR_TIME);
    }

    public void stopUpload() {
        if (mRegularHandler != null && mUploadRunnable != null) {
            MyLog.d(TAG, "remove callbacks");
            mRegularHandler.removeCallbacks(mUploadRunnable);
        }
        if (mHandlerThread != null) {
            MyLog.d(TAG, "thread quit");
            mHandlerThread.quit();
        }
    }

    private Runnable getRegularRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                MyLog.d(TAG, "upload aliveTime");
                MilinkStatistics.getInstance().statisticAlive(MyUserInfoManager.getInstance().getUuid(),
                        REGULAR_TIME, AliveStatisticItem.ALIVE_BIZ_TYPE_ALL);

                if (mRegularHandler != null && mUploadRunnable != null) {
                    mRegularHandler.postDelayed(mUploadRunnable, REGULAR_TIME);
                }
            }
        };
    }
}
