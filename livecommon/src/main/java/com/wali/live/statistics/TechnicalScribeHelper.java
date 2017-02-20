package com.wali.live.statistics;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.wali.live.statistics.pojo.TechnicalStatisticsItem;

/**
 * Created by rongzhisheng on 16-7-16.
 * 一个用来进行技术打点的代理工具类
 */
public class TechnicalScribeHelper implements IScribeWorker<TechnicalStatisticsItem> {
    private static final String TAG = TechnicalScribeHelper.class.getSimpleName();
    // new一个实现了IScribeWorker接口的类
    private static final IScribeWorker SCRIBE_WORKER = new MiLinkMonitorScribeWorker();
    private static final TechnicalScribeHelper INSTANCE = new TechnicalScribeHelper();

    private TechnicalScribeHelper() {}

    public static TechnicalScribeHelper getInstance() {
        return INSTANCE;
    }

    @Override
    public void scribeSync(@NonNull TechnicalStatisticsItem statisticsItem) {
        try {
            SCRIBE_WORKER.scribeSync(statisticsItem);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    @Override
    public void scribeAsync(@NonNull TechnicalStatisticsItem statisticsItem) {
        try {
            SCRIBE_WORKER.scribeAsync(statisticsItem);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    public void scribeAsync(@NonNull String category, @NonNull String action, int code, @Nullable String msg, @Nullable String ext) {
        scribeAsync(new TechnicalStatisticsItem(category, action, code, msg, ext));
    }

}
