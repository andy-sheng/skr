package com.wali.live.watchsdk.schema;

import android.net.Uri;

import com.wali.live.michannel.ChannelParam;

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

    public static long getChannelIdFromParam(ChannelParam param) {
        if (param != null) {
            return param.getChannelId();
        }
        return 0;
    }

    public static long getSubListIdFromParam(ChannelParam param) {
        if (param != null) {
            return param.getSubListId();
        }
        return 0;
    }

    public static String getSectionTitleFromParam(ChannelParam param) {
        if (param != null) {
            return param.getSectionTitle();
        }
        return String.valueOf(0);
    }
}
