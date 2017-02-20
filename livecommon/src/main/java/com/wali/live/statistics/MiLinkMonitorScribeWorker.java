package com.wali.live.statistics;

import android.support.annotation.NonNull;

import com.mi.milink.sdk.debug.MiLinkMonitor;
import com.wali.live.statistics.pojo.TechnicalStatisticsItem;

import org.json.JSONException;

/**
 * Created by rongzhisheng on 16-7-16.
 */
public class MiLinkMonitorScribeWorker implements IScribeWorker<TechnicalStatisticsItem> {
    @Override
    public void scribeSync(@NonNull TechnicalStatisticsItem statisticsItem) {
        throw new UnsupportedOperationException("milink monitor unsupport sync scribe");
    }

    @Override
    public void scribeAsync(@NonNull TechnicalStatisticsItem statisticsItem) throws JSONException {
        trace(statisticsItem);
    }

    private void trace(@NonNull TechnicalStatisticsItem statisticsItem) throws JSONException {
        MiLinkMonitor.getInstance().trace(statisticsItem.toJSONObject().toString(), 0, statisticsItem.getCategory(), 0, 0, 0, 0, 0, 0);
    }
}
