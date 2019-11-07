package com.zq.engine.avstatistics.logservice;


import com.zq.engine.avstatistics.datastruct.ILogItem;

import org.json.JSONObject;

/**
 * @author gongjun@skr.net 2019.10.18
 * @brief SLogServiceBase is the basic definition of the Log-Service. Log-Service is initialized by app, and used by {@link com.zq.engine.avstatistics.SDataManager}
 * refer to {@link com.zq.engine.avstatistics.SDataManager#setLogServices(SLogServiceBase)}, {@link com.zq.engine.avstatistics.SDataManager#flush(int)} and {@link SLogServiceBase#init(Object)}
 * for more info.
 *
 */
public abstract class SLogServiceBase {

    public final static int OP_LOCK_LOGGROUP = 1;

    /**
     * @param param usually there are some parameter related to app, so this API is usually called by app-context
     */
    public abstract void init(Object param) throws Exception;
    public abstract void uninit();

    public abstract void appendLog(String key, String value);
    /**
     * @param key is identifier of log-value
     * @param jsonObject is the JSON Object
     */
    public abstract void appendLog(String key, JSONObject jsonObject);

    /**
     * @brief Some additional infomation will be added by this API, such as Skr.UserID, etc.
     *          You can refer to {@link SLogServiceAgent.AliYunSLInitParam} and other similar init-param class for more info
     * @param item
     */
    public abstract void appendLog(ILogItem item);

    /**
     * @param isSync every implementation of the log-services and decide the sync-style by themself
     */
    public abstract void flushLog(boolean isSync);
}

