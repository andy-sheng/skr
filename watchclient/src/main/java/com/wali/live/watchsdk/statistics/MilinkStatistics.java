package com.wali.live.watchsdk.statistics;

import com.base.log.MyLog;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.proto.StatisticsProto;
import com.wali.live.watchsdk.statistics.item.AliveStatisticItem;
import com.wali.live.watchsdk.statistics.item.ChannelChangeStatisticsItem;
import com.wali.live.watchsdk.statistics.item.ChannelStatisticsItem;
import com.wali.live.watchsdk.statistics.item.MilinkStatisticsItem;
import com.wali.live.watchsdk.statistics.item.SimpleStatisticsItem;
import com.wali.live.watchsdk.statistics.item.StayExposureStatisticItem;
import com.wali.live.watchsdk.statistics.request.LiveRecvRequest;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

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

    private static final int DEFAULT_COUNT = 20;

    private static MilinkStatistics sInstance = null;

    private List<StatisticsProto.LiveRecvFlagItem> mItemList = new ArrayList<>(DEFAULT_COUNT);

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
                    SimpleStatisticsItem.LIVE_SDK_TYPE,
                    SimpleStatisticsItem.GAME_ACTIVE_BIZTYPE,
                    key, time, 50010);
            upload(item);
        } catch (JSONException e) {
            MyLog.e(TAG, e);
        }
    }

    public void statisticsOtherActive(String key, long time, int channelId) {
        long date = System.currentTimeMillis();
        try {
            MilinkStatisticsItem item = new SimpleStatisticsItem(date,
                    SimpleStatisticsItem.LIVE_SDK_TYPE,
                    SimpleStatisticsItem.OTHER_ACTIVE_BIZTYPE,
                    key, time, channelId);
            upload(item);
        } catch (JSONException e) {
            MyLog.e(TAG, e);
        }
    }

    public void statisticsChannelExposure(String recommend) {
        long date = System.currentTimeMillis();
        MilinkStatisticsItem item = new ChannelStatisticsItem(date,
                ChannelStatisticsItem.CHANNEL_TYPE_EXPOSURE,
                recommend);
        uploadDelay(item);
    }

    public void statisticsChannelClick(String recommend) {
        long date = System.currentTimeMillis();
        MilinkStatisticsItem item = new ChannelStatisticsItem(date,
                ChannelStatisticsItem.CHANNEL_TYPE_CLICK,
                recommend);
        upload(item);
    }

    public void statisticChannelChange(int channelId) {
        int type = ChannelChangeStatisticsItem.getTypeByChannel();
        if (type == -1) {
            return;
        }
        long date = System.currentTimeMillis();
        MilinkStatisticsItem item = new ChannelChangeStatisticsItem(date, type, channelId);
        upload(item);
    }

    public void statisticStayExposure(long userId, String recommend) {
        MyLog.d(TAG, "StayExposure tag = " + recommend);
        int bizType = StayExposureStatisticItem.getBizTypeByChannel();
        if (bizType == -1) {
            return;
        }
        long date = System.currentTimeMillis();
        MilinkStatisticsItem item = new StayExposureStatisticItem(date, userId, recommend);
        uploadDelay(item);
    }

    public void statisticAlive(long userId, long times) {
        statisticAlive(userId, times, 0);
    }

    public void statisticAlive(long userId, long times, long channelId) {
        int bizType = AliveStatisticItem.getBizTypeByChannel();
        if (bizType == -1) {
            return;
        }
        MyLog.d(TAG, "statisticAlive times=" + times + " channelId=" + channelId);
        long date = System.currentTimeMillis();
        MilinkStatisticsItem item = new AliveStatisticItem(date, userId, times, channelId);
        upload(item);
    }

    private void upload(final MilinkStatisticsItem item) {
        Observable.just(0)
                .map(new Func1<Object, Integer>() {
                    @Override
                    public Integer call(Object o) {
                        StatisticsProto.LiveRecvFlagRsp rsp = new LiveRecvRequest(item.build()).syncRsp();
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
                        MyLog.e(TAG, "upload failure", e);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        MyLog.w(TAG, "upload onNext integer=" + integer);
                        if (integer != null && integer == 0) {
                            return;
                        }
                    }
                });
    }

    private void uploadDelay(final MilinkStatisticsItem item) {
        mItemList.add(item.build());
        if (mItemList.size() > DEFAULT_COUNT) {
            List<StatisticsProto.LiveRecvFlagItem> list = new ArrayList<>(mItemList);
            mItemList.clear();
            Observable.just(list)
                    .map(new Func1<List<StatisticsProto.LiveRecvFlagItem>, Integer>() {
                        @Override
                        public Integer call(List<StatisticsProto.LiveRecvFlagItem> list) {
                            StatisticsProto.LiveRecvFlagRsp rsp = new LiveRecvRequest(list).syncRsp();
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
                            MyLog.e(TAG, "uploadDelay failure", e);
                        }

                        @Override
                        public void onNext(Integer integer) {
                            MyLog.w(TAG, "uploadDelay onNext integer=" + integer);
                            if (integer != null && integer == 0) {
                                return;
                            }
                        }
                    });
        }
    }
}
