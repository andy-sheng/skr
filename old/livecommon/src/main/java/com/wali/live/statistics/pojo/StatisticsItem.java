package com.wali.live.statistics.pojo;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.wali.live.common.statistics.pojo.BaseStatisticsItem;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lifeel on 16-7-13.
 */
public class StatisticsItem extends BaseStatisticsItem {
    private static final String TAG = StatisticsItem.class.getSimpleName();

    private static final String JSON_KEY_AC = "ac";
    private static final String JSON_KEY_KEY = "key";
    private static final String JSON_KEY_VALUE = "times";
    private static final String JSON_KEY_DATE = "date";
    private static final String JSON_KEY_TS = "ts";

    private String mAc;
    private String mActionKey;
    private long mValue;
    private String mDate;
    private long mTimeStamp;

    public StatisticsItem(String acIn, String actionTagIn, String date, long value, long ts) {
        mAc = acIn;
        mActionKey = actionTagIn;
        mDate = date;
        mValue = value;
        mTimeStamp = ts;
    }

    public StatisticsItem(String jsonString) {
        try {
            fromJSONObject(jsonString);
        } catch (Exception e) {
            MyLog.e(TAG + " init ailed jstr=" + jsonString);
        }
    }

    @Override
    public BaseStatisticsItem fromJSONObject(String jsonStr) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonStr);
        setAc(jsonObject.optString(JSON_KEY_AC, ""));
        setActionKey(jsonObject.optString(JSON_KEY_KEY, ""));
        setValue(jsonObject.optLong(JSON_KEY_VALUE, 0L));
        setDate(jsonObject.optString(JSON_KEY_DATE, ""));
        setTimeStamp(jsonObject.optLong(JSON_KEY_TS, 0));
        return this;
    }

    @Override
    public String getLogKey() {
        return mActionKey;
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_KEY_AC, mAc);
        json.put(JSON_KEY_KEY, mActionKey);
        json.put(JSON_KEY_VALUE, mValue);
        json.put(JSON_KEY_DATE, mDate);
        json.put(JSON_KEY_TS, mTimeStamp);
        return json;
    }

    public boolean isLegal() {
        return !(TextUtils.isEmpty(mAc) || TextUtils.isEmpty(mActionKey));
    }

    public String getAc() {
        return mAc;
    }

    public void setAc(String ac) {
        this.mAc = ac;
    }

    public String getActionKey() {
        return mActionKey;
    }

    public void setActionKey(String actionKey) {
        this.mActionKey = actionKey;
    }

    public long getValue() {
        return mValue;
    }

    public void setValue(long value) {
        this.mValue = value;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public long getTimeStamp() {
        return this.mTimeStamp;
    }

    public void setTimeStamp(long ts) {
        this.mTimeStamp = ts;
    }
}
