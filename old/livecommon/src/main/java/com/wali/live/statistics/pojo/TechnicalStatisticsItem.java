package com.wali.live.statistics.pojo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.wali.live.common.statistics.pojo.BaseStatisticsItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rongzhisheng on 16-7-18.
 */
public class TechnicalStatisticsItem extends BaseStatisticsItem {
    private static final String TAG = TechnicalStatisticsItem.class.getSimpleName();

    private static final String JSON_KEY_CATEGORY = "category";
    private static final String JSON_KEY_ACTION = "action";
    private static final String JSON_KEY_CODE = "code";
    private static final String JSON_KEY_MSG = "msg";
    private static final String JSON_KEY_EXT = "ext";
    private static final String JSON_KEY_TS = "ts";

    private String category;
    private String action;
    private int code;
    private String msg;
    private String ext;
    private long ts;

    public TechnicalStatisticsItem(@NonNull String category, @NonNull String action, int code, @Nullable String msg, @Nullable String ext) {
        this.category = category;
        this.action = action;
        this.code = code;
        this.msg = msg;
        this.ext = ext;
    }

    @Override
    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        synchronized (this) {
            json.put(JSON_KEY_CATEGORY, category);
            json.put(JSON_KEY_ACTION, action);
            json.put(JSON_KEY_CODE, code);
            json.put(JSON_KEY_TS, ts);
            try {
                if (!TextUtils.isEmpty(msg)) {
                    json.put(JSON_KEY_MSG, URLEncoder.encode(msg, "UTF-8"));
                }
                if (!TextUtils.isEmpty(ext)) {
                    json.put(JSON_KEY_EXT, URLEncoder.encode(ext, "UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                MyLog.e(TAG, e);
            }
        }
        return json;
    }

    @Override
    public TechnicalStatisticsItem fromJSONObject(String jsonStr) throws JSONException {
        JSONObject json = new JSONObject(jsonStr);
        synchronized (this) {
            category = json.optString(JSON_KEY_CATEGORY);
            action = json.optString(JSON_KEY_ACTION);
            code = json.optInt(JSON_KEY_CODE);
            msg = json.optString(JSON_KEY_MSG);
            ext = json.optString(JSON_KEY_EXT);
            ts = json.optLong(JSON_KEY_TS);
        }
        return this;
    }

    @Override
    public String getLogKey() {
        return "TechnicalStatisticsItem " + code;
    }

    public String getCategory() {
        return category;
    }

    public static class Ext {
        private Map<String, String> map = new HashMap<>();

        private Ext() {
        }

        public static Ext create() {
            return new Ext();
        }

        public Ext add(@NonNull String key, @NonNull String value) {
            map.put(key, value);
            return this;
        }

        public String toJsonString() {
            if (map.isEmpty()) {
                return null;
            }
            JSONObject json = new JSONObject();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                try {
                    json.put(entry.getKey(), entry.getValue() != null ? entry.getValue() : "<null>");
                } catch (JSONException e) {
                    MyLog.e(TAG, e);
                }
            }
            return json.length() == 0 ? null : json.toString();
        }
    }

    public interface Code {
        int UNKNOWN = -1;
        int OK = 0;
        /**
         * 响应中有明确包含返回码,就是知道为什么错了,错误原因放到msg或ext里
         */
        int KNOWN_ERROR = 1;
        int PARAM_ERROR = 2;
        /**
         * 发生异常,msg设为getMessage()
         */
        int EXCEPTION = 3;
    }

}
