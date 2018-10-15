package com.wali.live.common.statistics.pojo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by queda on 16-7-14.
 */

/**
 * 这个抽象类用来统一打点数据规范
 */
public abstract class BaseStatisticsItem {

    public abstract JSONObject toJSONObject() throws JSONException;

    public abstract BaseStatisticsItem fromJSONObject(String jsonStr) throws JSONException;

    public String toString() {
        try {
            JSONObject object = toJSONObject();
            if (object != null) {
                return object.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //打log用 看看打的什么点
    public abstract String getLogKey();
}
