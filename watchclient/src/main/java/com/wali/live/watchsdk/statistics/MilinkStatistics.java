package com.wali.live.watchsdk.statistics;

import com.base.log.MyLog;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.StatisticsProto;
import com.wali.live.watchsdk.statistics.item.MilinkStatisticsItem;
import com.wali.live.watchsdk.statistics.item.SimpleStatisticsItem;
import com.wali.live.watchsdk.statistics.request.LiveRecvRequest;

import org.json.JSONException;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 2017/6/28.
 */
public class MilinkStatistics {
    private static final String TAG = MilinkStatistics.class.getSimpleName();

    private static MilinkStatistics sInstance = null;

    private MilinkStatistics() {
    }

    public synchronized static MilinkStatistics getInstance() {
        if (sInstance == null) {
            sInstance = new MilinkStatistics();
        }
        return sInstance;
    }

    public void statisticsGameActive(String key, long time) {
        long date = System.currentTimeMillis();
        try {
            MilinkStatisticsItem item = new SimpleStatisticsItem(date,
                    MilinkStatisticsItem.LIVE_SDK_TYPE,
                    SimpleStatisticsItem.GAME_ACTIVE_BIZTYPE,
                    key, time);
            statisticsInternal(item);
        } catch (JSONException e) {
            MyLog.e(TAG, e);
        }
    }

    private void statisticsInternal(final MilinkStatisticsItem item) {
        Observable.just(0)
                .map(new Func1<Object, Integer>() {
                    @Override
                    public Integer call(Object o) {
                        StatisticsProto.LiveRecvFlagRsp rsp = new LiveRecvRequest(item).syncRsp();
                        if (rsp != null) {
                            return rsp.getRetCode();
                        }
                        return ErrorCode.CODE_ERROR_NORMAL;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "statistics failure", e);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        MyLog.w(TAG, "statistics onNext integer=" + integer);
                        if (integer != null && integer == 0) {
                            return;
                        }
                    }
                });
    }
}
