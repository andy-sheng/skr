package com.wali.live.watchsdk.scheme;

import android.net.Uri;
import android.text.TextUtils;

import com.base.log.MyLog;

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
//            MyLog.e(e);
        }
        return defaultValue;
    }

    public static int getInt(Uri uri, String key, int defaultValue) {
        try {
            return Integer.valueOf(uri.getQueryParameter(key));
        } catch (Exception e) {
//            MyLog.e(e);
        }
        return defaultValue;
    }

    public static String getRecommendTag(String uriStr) {
        if (TextUtils.isEmpty(uriStr)) {
            return "";
        }
        Uri uri = Uri.parse(uriStr);
        return uri.getQueryParameter(SchemeConstants.PARAM_RECOMMEND_TAG);
    }

}
