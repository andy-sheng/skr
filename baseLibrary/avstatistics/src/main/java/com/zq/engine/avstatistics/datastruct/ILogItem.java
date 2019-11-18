package com.zq.engine.avstatistics.datastruct;


import org.json.JSONObject;

public interface ILogItem
{
    String toString();
    JSONObject toJSONObject();

    /**
     * Just now this API is used to return class name. and this class name will be used as
     * "key" in JSON process. </br>
     * </br>
     * For example: when upload log to Aliyun-SLS platform, log-key is get from this {@link this#getKey()},
     * and the log-content is JSON-String which is related to {@link this#toJSONObject()}
     */
    String getKey();

}