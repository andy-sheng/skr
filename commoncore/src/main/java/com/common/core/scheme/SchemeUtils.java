package com.common.core.scheme;

import android.net.Uri;
import android.text.TextUtils;

import com.common.log.MyLog;

/**
 * Created by lan on 16/10/26.
 *
 * @module scheme
 * @description 该类中的方法与具体业务无关，只是提供简化的代码逻辑
 */
public class SchemeUtils {
    public static long getLong(Uri uri, String key, long defaultValue) {
        try {
            return Long.valueOf(uri.getQueryParameter(key));
        } catch (Exception e) {
            MyLog.e(e);
        }
        return defaultValue;
    }

    public static int getInt(Uri uri, String key, int defaultValue) {
        try {
            return Integer.valueOf(uri.getQueryParameter(key));
        } catch (Exception e) {
            MyLog.e(e);
        }
        return defaultValue;
    }

    public static String getString(Uri uri, String key) {
        try {
            return uri.getQueryParameter(key);
        } catch (Exception e) {
            MyLog.e(e);
        }
        return "";
    }

}
