package com.wali.live.statistics;

import android.support.annotation.NonNull;

import org.json.JSONException;

/**
 * Created by rongzhisheng on 16-7-16.
 *
 * @param <T> 统计数据的类型
 */
public interface IScribeWorker<T> {
    /**
     * 同步打点
     */
    void scribeSync(@NonNull T statisticsItem) throws JSONException;

    /**
     * 异步打点
     */
    void scribeAsync(@NonNull T statisticsItem) throws JSONException;
}
