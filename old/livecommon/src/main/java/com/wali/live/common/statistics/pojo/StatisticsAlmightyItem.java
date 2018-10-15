package com.wali.live.common.statistics.pojo;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.wali.live.common.statistics.BaseStatisticsWorker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by yurui on 8/4/16.
 */
public class StatisticsAlmightyItem extends BaseStatisticsItem {
    private static final String TAG = StatisticsAlmightyItem.class.getSimpleName();

    public static final String KEY_AC = "ac";
    private static final String KEY_JSON_KEY = "json_key";
    private static final String KEY_TS = "ts";
    private static final String KEY_DATE = "date";
    private static final String KEY_PUSH_TYPE = "key_push_type";

    public static final int VALUE_KEY_WAKE_UP = 1; //唤醒的wakeup 0为不是唤醒 1是唤醒

    public static boolean sAppStartByWakeup = false;
    private String mAc;
    private String mValue;
    private long mTimeStamp;
    private String mDate;
    private int mKeyPushType = 0;

    public StatisticsAlmightyItem(String action, String date, long ts, String... extParams) {
        this.mAc = action;
        this.mTimeStamp = ts;
        this.mDate = date;
        //对应自动以key 和 value 必须成对出现
        if (extParams.length > 0 && extParams.length % 2 == 0) {
            try {
                JSONObject object = new JSONObject();
                for (int i = 0; i < extParams.length; i += 2) {
                    object.put(extParams[i], extParams[i + 1]);
                }
                mValue = object.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(sAppStartByWakeup){
            mKeyPushType = VALUE_KEY_WAKE_UP;
        }else {
            mKeyPushType = 0;
        }
    }

    public StatisticsAlmightyItem(String jsonString) {
        try {
            fromJSONObject(jsonString);
        } catch (Exception e) {
            MyLog.e(TAG + " init failed jstr=" + jsonString);
        }
    }


    //从本地文件中读取
    @Override
    public BaseStatisticsItem fromJSONObject(String jsonStr) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonStr);
        mAc = jsonObject.optString(KEY_AC, "");
        mValue = jsonObject.optString(KEY_JSON_KEY, "");
        mTimeStamp = jsonObject.optLong(KEY_TS, System.currentTimeMillis());
        mDate = jsonObject.optString(KEY_DATE, BaseStatisticsWorker.getDateYYYYMMDD());
        mKeyPushType = jsonObject.optInt(KEY_PUSH_TYPE,0);
        return this;
    }

    //本地写文件时候使用
    public JSONObject toJSONObjectLocal() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(KEY_AC, mAc);
        json.put(KEY_TS, mTimeStamp);
        json.put(KEY_DATE, mDate);
        json.put(KEY_JSON_KEY, mValue);
        json.put(KEY_PUSH_TYPE,mKeyPushType);
        return json;
    }

    //上传时使用
    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(KEY_AC, mAc);
        json.put(KEY_TS, mTimeStamp);
        json.put(KEY_DATE, mDate);
        json.put(KEY_PUSH_TYPE,mKeyPushType);
        if (!TextUtils.isEmpty(mValue)) {
            JSONObject valueStr = new JSONObject(mValue);
            Iterator<String> keys = valueStr.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = valueStr.getString(key);
                json.put(key, value);
            }
        }
        return json;
    }

    @Override
    public String getLogKey() {
        return mAc;
    }

    public boolean isLegal() {
        return !TextUtils.isEmpty(mAc) && mTimeStamp > 0 && !TextUtils.isEmpty(mDate);
    }
}
