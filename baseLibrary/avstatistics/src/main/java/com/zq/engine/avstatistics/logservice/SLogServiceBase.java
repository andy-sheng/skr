package com.zq.engine.avstatistics.logservice;


import com.zq.engine.avstatistics.datastruct.ILogItem;

import org.json.JSONObject;

/**
 * @author gongjun@skr.net 2019.10.18
 * @brief SLogServiceBase is the basic definition of the Log-Service.
 *
 */
public abstract class SLogServiceBase {

    //public final static int OP_LOCK_LOGGROUP = 1;//not in used yet

    /**
     * When set this prop via {@link this#setProp(int, Object)}, the prop data-type should be {@link Long}.
     */
    public final static int PROP_USER_ID     = 2;


    /**
     * It's only for getProp. Prop data-type is {@link Boolean};
     *
     */
    public final static int PROP_IS_INITIALIZED = 3; //only for getProp

    /**
     * For now, only aliyun-sls will need this.
     * The prop data-type is {@link com.zq.engine.avstatistics.sts.SSTSCredentialHolder}
     */
    public final static int PROP_STS_CREDENTIAL_HOLDER= 4;


    /**
     *  Enable or disable log-services. It is only for setProp, and the prop data-type is {@link Boolean}
     */
    public final static int PROP_ENABLE_SERVICES = 5;


    /**
     *  True means use main-log-project(used in release version), false means use test-log-project(used by dev/test)</br>
     *  Prop data-type is {@link Boolean}. Default is test-log-project
     */
    public final static int PROP_USE_MAIN_LOG_PROJECT = 6;

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
     *          You can refer to {@link SLogServiceAgent.SAliYunSLParam} and other similar init-param class for more info
     * @param item
     */
    public abstract void appendLog(ILogItem item);

    /**
     * @param isSync every implementation of the log-services and decide the sync-style by themself
     */
    public abstract void flushLog(boolean isSync);


    public abstract void setProp(int propID, Object prop) throws Exception;

    public abstract Object getProp(int propID);
}

